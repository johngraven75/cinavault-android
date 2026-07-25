package com.cinavault.android

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cinavault.android.data.AppDestination
import com.cinavault.android.data.CinaVaultUiState
import com.cinavault.android.data.MediaItem
import com.cinavault.android.data.RemoteSession
import com.cinavault.android.network.CinaVaultApi
import com.cinavault.android.security.SecureSessionStore
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CinaVaultViewModel(application: Application) : AndroidViewModel(application) {
    private val api = CinaVaultApi()
    private val secureSessionStore = SecureSessionStore(application)
    private val _state = MutableStateFlow(CinaVaultUiState())
    val state: StateFlow<CinaVaultUiState> = _state.asStateFlow()

    init {
        secureSessionStore.load()?.let { session ->
            _state.update { it.copy(session = session, loading = true, statusMessage = "Restoring secure session") }
            refresh(session)
        }
    }

    fun loginWithPassword(endpoint: String, email: String, password: String) {
        runLogin("Signing in") {
            api.loginWithPassword(endpoint, email, password)
        }
    }

    fun loginWithAccessKey(endpoint: String, accessKey: String) {
        runLogin("Validating access key") {
            api.loginWithAccessKey(endpoint, accessKey)
        }
    }

    fun logout() {
        secureSessionStore.clear()
        _state.value = CinaVaultUiState(statusMessage = "Signed out")
    }

    fun navigate(destination: AppDestination) {
        _state.update { current ->
            current.copy(destination = destination, errorMessage = null)
        }
    }

    fun setSearchQuery(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }

    fun openMedia(item: MediaItem) {
        _state.update {
            it.copy(
                selectedMedia = item,
                destination = AppDestination.Player,
                statusMessage = "Opening ${item.title}",
            )
        }
    }

    fun refreshLibrary() {
        val session = _state.value.session ?: return
        refresh(session)
    }

    fun toggleAutopilot(enabled: Boolean) {
        _state.update {
            it.copy(
                autopilotEnabled = enabled,
                statusMessage = if (enabled) {
                    "AI Autopilot enabled: library refresh and repair insights are automatic"
                } else {
                    "AI Autopilot paused on this device"
                },
            )
        }
    }

    fun runAutopilotNow() {
        _state.update {
            it.copy(statusMessage = "AI Autopilot is reconciling remote library state")
        }
        refreshLibrary()
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }

    fun absoluteMediaUrl(path: String): String? {
        val session = _state.value.session ?: return null
        return api.absoluteUrl(session, path)
    }

    fun sessionToken(): String? = _state.value.session?.token

    private fun runLogin(
        status: String,
        authenticate: suspend () -> RemoteSession,
    ) {
        viewModelScope.launch {
            _state.update {
                it.copy(loading = true, errorMessage = null, statusMessage = status)
            }
            runCatching { authenticate() }
                .onSuccess { session ->
                    secureSessionStore.save(session)
                    _state.update {
                        it.copy(
                            session = session,
                            loading = false,
                            statusMessage = "Secure account session established",
                        )
                    }
                    refresh(session)
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            loading = false,
                            errorMessage = error.message ?: "Unable to sign in",
                            statusMessage = "Authentication failed",
                        )
                    }
                }
        }
    }

    private fun refresh(session: RemoteSession) {
        viewModelScope.launch {
            _state.update {
                it.copy(refreshing = true, errorMessage = null, statusMessage = "Synchronizing encrypted library")
            }
            runCatching {
                val info = async { api.loadServerInfo(session) }
                val library = async { api.loadLibrary(session) }
                info.await() to library.await()
            }.onSuccess { (serverInfo, library) ->
                val sorted = if (_state.value.autopilotEnabled) {
                    smartSort(library)
                } else {
                    library
                }
                _state.update {
                    it.copy(
                        session = session,
                        serverInfo = serverInfo,
                        library = sorted,
                        loading = false,
                        refreshing = false,
                        statusMessage = "${sorted.size} encrypted media records synchronized",
                        lastRefreshEpochMillis = System.currentTimeMillis(),
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        loading = false,
                        refreshing = false,
                        errorMessage = error.message ?: "Library synchronization failed",
                        statusMessage = "Remote synchronization needs attention",
                    )
                }
            }
        }
    }

    private fun smartSort(items: List<MediaItem>): List<MediaItem> =
        items.sortedWith(
            compareByDescending<MediaItem> { it.favorite }
                .thenByDescending { it.verified }
                .thenByDescending { it.dateAdded }
                .thenBy { it.title.lowercase() },
        )
}
