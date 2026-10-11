package com.cinavault.android.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.provider.Settings
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cinavault.android.agent.AgentMood
import com.cinavault.android.agent.PortraitCalibration
import com.cinavault.android.agent.PortraitMath
import com.cinavault.android.agent.PortraitMesh
import com.cinavault.android.data.AgentUiState
import com.cinavault.android.ui.theme.CinaVaultCyan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.cinavault.android.ui.theme.CinaVaultInk
import com.cinavault.android.ui.theme.CinaVaultMagenta
import com.cinavault.android.ui.theme.CinaVaultMuted
import com.cinavault.android.ui.theme.CinaVaultPanel
import com.cinavault.android.ui.theme.CinaVaultText

/**
 * The CinaVault agent on the phone: the owner's photo face (or the hologram
 * orb when there is none) above a short chat with the server's agent.
 */
@Composable
fun AgentPanel(
    agent: AgentUiState,
    onSend: (String) -> Unit,
    onReset: () -> Unit,
) {
    var draft by remember { mutableStateOf("") }
    val now = rememberClockMillis(agent.speakingUntil)
    val mood = when {
        agent.sending -> AgentMood.Thinking
        now < agent.speakingUntil -> AgentMood.Speaking
        agent.error != null -> AgentMood.Error
        draft.isNotBlank() -> AgentMood.Listening
        else -> AgentMood.Idle
    }
    val reason = agent.unavailableReason

    Column(
        Modifier
            .fillMaxWidth()
            .background(CinaVaultPanel.copy(alpha = .88f), RoundedCornerShape(22.dp))
            .border(1.dp, CinaVaultCyan.copy(alpha = .18f), RoundedCornerShape(22.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("CinaVault agent", color = CinaVaultText, fontWeight = FontWeight.Black)
                Text(moodLabel(mood, reason), color = CinaVaultMuted, fontSize = 12.sp)
            }
            IconButton(onClick = onReset, enabled = agent.messages.isNotEmpty() && !agent.sending) {
                Icon(Icons.Rounded.RestartAlt, contentDescription = "Start a new conversation", tint = CinaVaultMuted)
            }
        }

        Box(
            Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Brush.verticalGradient(listOf(CinaVaultInk, Color(0xFF071A2A))))
                .semantics { contentDescription = "Agent head, ${moodLabel(mood, reason)}" },
            contentAlignment = Alignment.Center,
        ) {
            val face = agent.face
            val bitmap = produceState<Bitmap?>(null, face) { value = face?.let { decodeFace(it.photo) } }.value
            if (face != null && bitmap != null) {
                PortraitHead(bitmap, face.calibration, mood, Modifier.fillMaxWidth().height(240.dp))
            } else {
                HologramOrb(mood, Modifier.size(170.dp))
            }
        }

        agent.messages.forEach { message ->
            val shape = RoundedCornerShape(16.dp)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = if (message.fromAgent) Arrangement.Start else Arrangement.End) {
                Text(
                    message.text,
                    color = if (message.fromAgent) CinaVaultText else CinaVaultInk,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .widthIn(max = 300.dp)
                        .background(if (message.fromAgent) Color.White.copy(alpha = .07f) else CinaVaultCyan, shape)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                )
            }
        }
        agent.error?.let { Text(it, color = Color(0xFFFF8FA8), fontSize = 12.sp) }

        val canSend = reason == null && !agent.sending && draft.isNotBlank()
        val send = {
            if (canSend) {
                onSend(draft)
                draft = ""
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it.take(2_000) },
                modifier = Modifier.weight(1f),
                enabled = reason == null,
                placeholder = { Text("Ask about your library") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { send() }),
                maxLines = 4,
            )
            if (agent.sending) {
                CircularProgressIndicator(Modifier.size(40.dp).padding(8.dp), color = CinaVaultCyan, strokeWidth = 2.dp)
            } else {
                FilledIconButton(onClick = send, enabled = canSend) {
                    Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = "Send")
                }
            }
        }
        agent.status?.takeIf { reason == null }?.let {
            Text(
                "Answers come from your CinaVault server. From the phone the agent can look things up but not change anything. Up to ${it.messagesPerHour} messages an hour.",
                color = CinaVaultMuted,
                fontSize = 10.sp,
            )
        }
    }
}

private fun moodLabel(mood: AgentMood, unavailable: String?): String = unavailable ?: when (mood) {
    AgentMood.Thinking -> "Thinking…"
    AgentMood.Speaking -> "Answering"
    AgentMood.Listening -> "Listening"
    AgentMood.Error -> "Something went wrong"
    AgentMood.Idle -> "Ready"
}

/** True when the user has turned animations off in system settings. */
@Composable
private fun rememberReduceMotion(): Boolean {
    val context = LocalContext.current
    return remember {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }
}

/** Wall-clock millis that tick while a reply is being "spoken", so the mood can end. */
@Composable
private fun rememberClockMillis(until: Long): Long {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(until) {
        now = System.currentTimeMillis()
        while (now < until) {
            withFrameNanos { }
            now = System.currentTimeMillis()
        }
    }
    return now
}

/** Seconds since the head appeared, advanced every frame unless motion is reduced. */
@Composable
private fun rememberAnimationSeconds(reduceMotion: Boolean): Double {
    var seconds by remember { mutableDoubleStateOf(0.0) }
    LaunchedEffect(reduceMotion) {
        if (reduceMotion) return@LaunchedEffect
        val start = withFrameNanos { it }
        while (true) {
            withFrameNanos { frame -> seconds = (frame - start) / 1e9 }
        }
    }
    return seconds
}

/** The owner's photo, warped every frame into the agent's expression for the mood. */
@Composable
fun PortraitHead(
    bitmap: Bitmap,
    calibration: PortraitCalibration,
    mood: AgentMood,
    modifier: Modifier = Modifier,
) {
    val aspect = bitmap.width.toDouble() / bitmap.height.coerceAtLeast(1)
    val mesh = remember(calibration, aspect) { PortraitMesh(calibration, aspect) }
    val view = remember(calibration, aspect) { PortraitMath.view(calibration, aspect) }
    val vertices = remember(mesh) { FloatArray(mesh.vertexCount * 2) }
    val colors = remember(mesh) { IntArray(mesh.vertexCount) }
    val paint = remember { Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG) }
    val reduceMotion = rememberReduceMotion()
    val seconds = rememberAnimationSeconds(reduceMotion)

    Canvas(modifier) {
        val mouth = if (mood == AgentMood.Speaking) PortraitMath.speechMouth(seconds) else 0.0
        val pose = PortraitMath.pose(mood, seconds, mouth, reduceMotion)
        mesh.vertices(pose, bitmap.width.toFloat(), bitmap.height.toFloat(), vertices)
        mesh.colors(pose, colors)
        val scale = size.height / (view.height * bitmap.height).toFloat()
        drawIntoCanvas { canvas ->
            val native = canvas.nativeCanvas
            native.save()
            native.translate(size.width / 2, size.height / 2)
            native.scale(scale, scale)
            native.translate(-(view.center.x * bitmap.width).toFloat(), -(view.center.y * bitmap.height).toFloat())
            native.drawBitmapMesh(bitmap, mesh.cols, mesh.rows, vertices, 0, colors, 0, paint)
            native.restore()
        }
        // Faint scanlines keep it reading as a hologram.
        val tint = when (mood) {
            AgentMood.Error -> CinaVaultMagenta
            else -> CinaVaultCyan
        }
        var y = (seconds * 18 % 6).toFloat()
        while (y < size.height) {
            drawLine(tint.copy(alpha = .05f), Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
            y += 6f
        }
    }
}

/** The hologram orb shown until the owner sets a face on the desktop. */
@Composable
private fun HologramOrb(mood: AgentMood, modifier: Modifier = Modifier) {
    val reduceMotion = rememberReduceMotion()
    val seconds = rememberAnimationSeconds(reduceMotion)
    Canvas(modifier.clip(CircleShape)) {
        val pulse = when (mood) {
            AgentMood.Speaking -> PortraitMath.speechMouth(seconds).toFloat()
            AgentMood.Thinking -> ((kotlin.math.sin(seconds * 3) + 1) / 2).toFloat()
            else -> ((kotlin.math.sin(seconds * 1.2) + 1) / 4).toFloat()
        }
        val tint = if (mood == AgentMood.Error) CinaVaultMagenta else CinaVaultCyan
        drawCircle(Brush.radialGradient(listOf(tint.copy(alpha = .55f + .3f * pulse), Color.Transparent)), radius = size.minDimension / 2)
        drawCircle(tint.copy(alpha = .9f), radius = size.minDimension * (.16f + .05f * pulse))
    }
}

private const val MAX_FACE_SIDE = 720

/** Decodes the face off the main thread, scaled down so a large photo can't exhaust memory. */
private suspend fun decodeFace(photo: ByteArray): Bitmap? = withContext(Dispatchers.Default) {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(photo, 0, photo.size, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@withContext null
    val options = BitmapFactory.Options().apply {
        inSampleSize = PortraitMath.decodeSampleSize(bounds.outWidth, bounds.outHeight, MAX_FACE_SIDE)
    }
    BitmapFactory.decodeByteArray(photo, 0, photo.size, options)
}
