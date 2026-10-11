package com.cinavault.android

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cinavault.android.data.AgentChatMessage
import com.cinavault.android.data.AgentUiState
import com.cinavault.android.data.AppDestination
import com.cinavault.android.data.CinaVaultUiState
import com.cinavault.android.data.ControlSnapshot
import com.cinavault.android.data.MediaItem
import com.cinavault.android.data.RemoteSession
import com.cinavault.android.network.CinaVaultApi
import com.cinavault.android.network.CinaVaultHttpException
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
        if (destination == AppDestination.Intelligence) loadAgent()
    }

    /** Checks whether the server's agent is available and fetches its face. */
    fun loadAgent() {
        val session = _state.value.session ?: return
        viewModelScope.launch {
            runCatching {
                val status = api.loadAgentStatus(session)
                val face = if (status.hasFace) {
                    runCatching { api.loadAgentFace(session) }.getOrNull()
                } else {
                    null
                }
                status to face
            }.onSuccess { (status, face) ->
                updateAgent(session) { it.copy(status = status, face = face ?: it.face.takeIf { status.hasFace }, error = null) }
            }.onFailure { error ->
                updateAgent(session) { it.copy(error = agentError(error, "The agent is not available on this server yet.")) }
            }
        }
    }

    fun sendAgentMessage(text: String) {
        val session = _state.value.session ?: return
        val message = text.trim()
        if (message.isEmpty() || _state.value.agent.sending) return
        updateAgent {
            it.copy(
                messages = (it.messages + AgentChatMessage(fromAgent = false, text = message)).takeLast(MAX_AGENT_MESSAGES),
                sending = true,
                error = null,
            )
        }
        viewModelScope.launch {
            runCatching { api.sendAgentMessage(session, message) }
                .onSuccess { reply ->
                    updateAgent(session) {
                        it.copy(
                            messages = (it.messages + AgentChatMessage(fromAgent = true, text = reply)).takeLast(MAX_AGENT_MESSAGES),
                            sending = false,
                            speakingUntil = System.currentTimeMillis() + AgentUiState.speakingMillis(reply),
                        )
                    }
                }
                .onFailure { error ->
                    updateAgent(session) { it.copy(sending = false, error = agentError(error, "The agent could not answer")) }
                }
        }
    }

    fun resetAgentConversation() {
        val session = _state.value.session ?: return
        if (_state.value.agent.sending) return
        // Holding the composer until the server has reset keeps the next message in the new conversation.
        updateAgent { it.copy(messages = emptyList(), error = null, speakingUntil = 0, sending = true) }
        viewModelScope.launch {
            runCatching { api.resetAgent(session) }
            updateAgent(session) { it.copy(sending = false) }
        }
    }

    private fun updateAgent(transform: (AgentUiState) -> AgentUiState) {
        _state.update { it.copy(agent = transform(it.agent)) }
    }

    /** Applies a late network result only while the same account is still signed in. */
    private fun updateAgent(session: RemoteSession, transform: (AgentUiState) -> AgentUiState) {
        _state.update { if (it.session != session) it else it.copy(agent = transform(it.agent)) }
    }

    private fun agentError(error: Throwable, fallback: String): String =
        (error as? CinaVaultHttpException)?.serverMessage?.takeIf(String::isNotBlank)
            ?: error.message
            ?: fallback

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

    private companion object {
        const val MAX_AGENT_MESSAGES = 40
    }

    private fun smartSort(items: List<MediaItem>): List<MediaItem> =
        items.sortedWith(
            compareByDescending<MediaItem> { it.favorite }
                .thenByDescending { it.verified }
                .thenByDescending { it.dateAdded }
                .thenBy { it.title.lowercase() },
        )
}
