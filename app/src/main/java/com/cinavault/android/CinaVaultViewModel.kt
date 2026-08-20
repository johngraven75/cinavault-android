package com.cinavault.android

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cinavault.android.data.AppDestination
import com.cinavault.android.data.CinaVaultUiState
import com.cinavault.android.data.ControlSnapshot
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
            _state.update {
                it.copy(
                    session = session,
                    loading = true,
                    statusMessage = "Restoring secure session",
                )
            }
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

    fun refreshLumaSift() {
        val session = _state.value.session ?: return
        viewModelScope.launch {
            runCatching {
                val progress = async { api.loadLumaSiftProgress(session) }
                val plan = async { api.loadLumaSiftPlan(session) }
                progress.await() to plan.await()
            }.onSuccess { (progress, plan) ->
                _state.update {
                    it.copy(
                        lumaSiftProgress = progress,
                        lumaSiftPlan = plan,
                        statusMessage = progress.message,
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        errorMessage = error.message ?: "LumaSift status could not be synchronized",
                        statusMessage = "LumaSift needs attention",
                    )
                }
            }
        }
    }

    fun startLumaSift(selectedTypes: List<String>) {
        val session = _state.value.session ?: return
        if (selectedTypes.isEmpty()) {
            _state.update { it.copy(errorMessage = "Choose at least one LumaSift file type before starting a scan.") }
            return
        }
        if (_state.value.runningControlAction != null) return
        viewModelScope.launch {
            _state.update {
                it.copy(
                    runningControlAction = "lumasift.start",
                    errorMessage = null,
                    statusMessage = "LumaSift is preparing a read-only exact-duplicate plan",
                )
            }
            runCatching { api.startLumaSift(session, selectedTypes) }
                .onSuccess { message ->
                    _state.update { it.copy(runningControlAction = null, statusMessage = message) }
                    refreshLumaSift()
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            runningControlAction = null,
                            errorMessage = error.message ?: "LumaSift could not start",
                            statusMessage = "LumaSift needs attention",
                        )
                    }
                }
        }
    }

    fun applyLumaSiftPlan(planId: String) {
        val session = _state.value.session ?: return
        if (planId.isBlank() || _state.value.runningControlAction != null) return
        viewModelScope.launch {
            _state.update {
                it.copy(
                    runningControlAction = "lumasift.apply",
                    errorMessage = null,
                    statusMessage = "LumaSift is revalidating and moving approved files to quarantine",
                )
            }
            runCatching { api.applyLumaSiftPlan(session, planId) }
                .onSuccess { message ->
                    _state.update { it.copy(runningControlAction = null, statusMessage = message) }
                    refreshLumaSift()
                    refreshLibrary()
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            runningControlAction = null,
                            errorMessage = error.message ?: "LumaSift quarantine plan could not be applied",
                            statusMessage = "LumaSift needs attention",
                        )
                    }
                }
        }
    }

    fun runControlAction(actionId: String) {
        val session = _state.value.session ?: return
        if (actionId.isBlank() || _state.value.runningControlAction != null) return

        viewModelScope.launch {
            _state.update {
                it.copy(
                    runningControlAction = actionId,
                    errorMessage = null,
                    statusMessage = "Running secure control action",
                )
            }
            runCatching { api.runControlAction(session, actionId) }
                .onSuccess { message ->
                    _state.update {
                        it.copy(
                            runningControlAction = null,
                            statusMessage = message,
                        )
                    }
                    refresh(session)
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            runningControlAction = null,
                            errorMessage = error.message ?: "Control action failed",
                            statusMessage = "Control action needs attention",
                        )
                    }
                }
        }
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
        val remoteAction = _state.value.controlSnapshot
            .section("ai-autopilot")
            ?.actions
            ?.firstOrNull { it.id == "ai.run-now" && it.enabled }
        if (remoteAction != null) {
            runControlAction(remoteAction.id)
            return
        }

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
                it.copy(
                    refreshing = true,
                    errorMessage = null,
                    statusMessage = "Synchronizing encrypted library and controls",
                )
            }
            runCatching {
                val info = async { api.loadServerInfo(session) }
                val library = async { api.loadLibrary(session) }
                val controls = async {
                    runCatching { api.loadControlSnapshot(session) }
                        .getOrElse { error ->
                            ControlSnapshot.unavailable(
                                error.message
                                    ?: "The server has not enabled mobile control endpoints yet.",
                            )
                        }
                }
                Triple(info.await(), library.await(), controls.await())
            }.onSuccess { (serverInfo, library, controls) ->
                val sorted = if (_state.value.autopilotEnabled) {
                    smartSort(library)
                } else {
                    library
                }
                _state.update { current ->
                    val refreshedSelection = current.selectedMedia?.let { selected ->
                        sorted.firstOrNull { item -> item.mediaKey == selected.mediaKey } ?: selected
                    }
                    current.copy(
                        session = session,
                        serverInfo = serverInfo,
                        library = sorted,
                        selectedMedia = refreshedSelection,
                        controlSnapshot = controls,
                        loading = false,
                        refreshing = false,
                        statusMessage = if (controls.available) {
                            "${sorted.size} encrypted media records and controls synchronized"
                        } else {
                            "${sorted.size} encrypted media records synchronized; ${controls.message}"
                        },
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
