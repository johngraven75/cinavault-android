package com.cinavault.android.data

import com.cinavault.android.agent.PortraitCalibration

/** What the server says about its agent (GET /api/agent/status). */
data class AgentStatus(
    val enabled: Boolean,
    val configured: Boolean,
    val hasFace: Boolean,
    val messagesPerHour: Int,
)

/** The owner's agent face: a JPEG photo plus its eye and mouth markers. */
class AgentFace(
    val photo: ByteArray,
    val calibration: PortraitCalibration,
)

data class AgentChatMessage(
    val fromAgent: Boolean,
    val text: String,
)

data class AgentUiState(
    val status: AgentStatus? = null,
    val face: AgentFace? = null,
    val messages: List<AgentChatMessage> = emptyList(),
    val sending: Boolean = false,
    val error: String? = null,
    /** Until when the head animates the newest reply, in epoch millis. */
    val speakingUntil: Long = 0,
) {
    /** Why the agent can't be used right now, or null when it can. */
    val unavailableReason: String?
        get() = when {
            status == null -> "Connecting to the agent…"
            !status.enabled -> "The agent is turned off for phones on this server."
            !status.configured -> "The agent isn't set up yet. Add an Anthropic API key in CinaVault on your computer."
            else -> null
        }

    companion object {
        /** How long the head "speaks" a reply: about reading pace, within limits. */
        fun speakingMillis(reply: String): Long = (reply.length * 45L).coerceIn(1_200L, 9_000L)

        /** The base64 part of a `data:image/...;base64,` URL, or null when it isn't one. */
        fun dataUrlPayload(dataUrl: String): String? {
            val marker = ";base64,"
            if (!dataUrl.startsWith("data:image/")) return null
            val index = dataUrl.indexOf(marker)
            if (index < 0) return null
            return dataUrl.substring(index + marker.length).takeIf(String::isNotBlank)
        }
    }
}
