package com.cinavault.android.ui

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CastConnected
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.EnhancedEncryption
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.mediarouter.app.MediaRouteButton
import com.cinavault.android.BuildConfig
import com.cinavault.android.data.MediaItem
import com.cinavault.android.data.RemoteSession
import com.cinavault.android.data.ServerInfo
import com.cinavault.android.ui.theme.CinaVaultCyan
import com.cinavault.android.ui.theme.CinaVaultEmerald
import com.cinavault.android.ui.theme.CinaVaultMagenta
import com.cinavault.android.ui.theme.CinaVaultMuted
import com.cinavault.android.ui.theme.CinaVaultOrchid
import com.cinavault.android.ui.theme.CinaVaultPanel
import com.cinavault.android.ui.theme.CinaVaultSolar
import com.cinavault.android.ui.theme.CinaVaultText
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext

@Composable
fun RemoteScreen(session: RemoteSession, serverInfo: ServerInfo?, statusMessage: String, onRefresh: () -> Unit) {
    ScreenColumn {
        ScreenHero("Remote Orbit", "Encrypted account-scoped access through the CinaVault HTTPS relay.", Icons.Rounded.CloudDone, CinaVaultCyan)
        GlassPanel {
            DetailRow(Icons.Rounded.Link, "Endpoint", session.endpoint)
            DetailRow(Icons.Rounded.Person, "Account", session.email)
            DetailRow(Icons.Rounded.EnhancedEncryption, "Transport", serverInfo?.remoteTransport ?: "HTTPS relay")
            DetailRow(Icons.Rounded.Memory, "Server build", serverInfo?.let { "${it.version} · ${it.build}" } ?: "Unavailable")
        }
        FilledTonalButton(onClick = onRefresh) { Icon(Icons.Rounded.Refresh, null); Text("  Refresh remote state") }
        Text(statusMessage, color = CinaVaultMuted, fontSize = 11.sp)
    }
}

@Composable
fun CastingScreen(selectedMedia: MediaItem?) {
    val context = LocalContext.current
    val castContext = remember { runCatching { CastContext.getSharedInstance(context) }.getOrNull() }
    var device by remember { mutableStateOf(castContext?.sessionManager?.currentCastSession?.castDevice?.friendlyName) }
    ScreenColumn {
        ScreenHero("Casting Center", "Automatic Cast discovery and secure playback handoff.", Icons.Rounded.CastConnected, CinaVaultMagenta)
        GlassPanel {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AndroidView(factory = { viewContext -> MediaRouteButton(viewContext).also { CastButtonFactory.setUpMediaRouteButton(viewContext, it) } }, modifier = Modifier.size(58.dp))
                Column(Modifier.padding(start = 14.dp)) {
                    Text(device ?: "Choose a Cast device", color = CinaVaultText, fontSize = 19.sp, fontWeight = FontWeight.Black)
                    Text(selectedMedia?.title ?: "No media selected", color = CinaVaultMuted, fontSize = 12.sp)
                }
            }
        }
        FilledTonalButton(onClick = { device = castContext?.sessionManager?.currentCastSession?.castDevice?.friendlyName }) { Icon(Icons.Rounded.Refresh, null); Text("  Refresh Cast session") }
    }
}

@Composable
fun IntelligenceScreen(
    library: List<MediaItem>,
    enabled: Boolean,
    lastRefreshEpochMillis: Long?,
    hfTokenStatus: String,
    providerStatus: String,
    onToggle: (Boolean) -> Unit,
    onRunNow: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    var catalogQuery by remember { mutableStateOf("") }
    val missingArtwork = library.count { it.artworkUrl.isNullOrBlank() }
    val unverified = library.count { !it.verified }

    ScreenColumn {
        ScreenHero("AI Autopilot", "Manage visible AI stop controls and select public Hugging Face models.", Icons.Rounded.AutoAwesome, CinaVaultMagenta)
        GlassPanel {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Autonomous media management", color = CinaVaultText, fontWeight = FontWeight.Black)
                    Text(if (enabled) "AI scanning and reconciliation enabled" else "AI scanner stopped", color = CinaVaultMuted, fontSize = 12.sp)
                }
                Switch(checked = enabled, onCheckedChange = onToggle)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onRunNow, enabled = enabled, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = CinaVaultMagenta)) {
                    Icon(Icons.Rounded.AutoAwesome, null); Text("  Run scanner")
                }
                Button(onClick = { onToggle(false) }, enabled = enabled, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8A2948))) {
                    Icon(Icons.Rounded.Stop, null); Text("  Stop scanner")
                }
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TelemetryCard(Modifier.weight(1f), missingArtwork.toString(), "Artwork needs", CinaVaultMagenta)
            TelemetryCard(Modifier.weight(1f), unverified.toString(), "Unverified", CinaVaultSolar)
            TelemetryCard(Modifier.weight(1f), library.size.toString(), "Media records", CinaVaultEmerald)
        }

        GlassPanel {
            Text("Hugging Face Model Catalog", color = CinaVaultText, fontWeight = FontWeight.Black)
            Text("Search public models from the Android user interface.", color = CinaVaultMuted, fontSize = 12.sp)
            OutlinedTextField(value = catalogQuery, onValueChange = { catalogQuery = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, leadingIcon = { Icon(Icons.Rounded.Search, null) }, label = { Text("Model or publisher") })
            Button(onClick = {
                val query = Uri.encode(catalogQuery.trim())
                val url = if (query.isBlank()) "https://huggingface.co/models?pipeline_tag=text-generation&sort=trending" else "https://huggingface.co/models?search=$query&pipeline_tag=text-generation"
                uriHandler.openUri(url)
            }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Rounded.Search, null); Text("  Open model catalog") }
            Text("HF token: $hfTokenStatus", color = CinaVaultText, fontSize = 11.sp)
            Text("Providers: $providerStatus", color = CinaVaultMuted, fontSize = 11.sp)
        }

        Text(lastRefreshEpochMillis?.let { "Last synchronized ${android.text.format.DateUtils.getRelativeTimeSpanString(it)}" } ?: "Autopilot has not synchronized yet.", color = CinaVaultMuted, fontSize = 11.sp)
    }
}

@Composable
fun SettingsScreen(session: RemoteSession, serverInfo: ServerInfo?, onLogout: () -> Unit) {
    ScreenColumn {
        ScreenHero("Experience Control", "Account, security, build identity, and application behavior.", Icons.Rounded.Security, CinaVaultCyan)
        GlassPanel {
            DetailRow(Icons.Rounded.Person, "Signed in", session.email)
            DetailRow(Icons.Rounded.Lock, "Endpoint", session.endpoint)
            DetailRow(Icons.Rounded.Memory, "Android build", BuildConfig.CINAVAULT_BUILD)
            DetailRow(Icons.Rounded.CloudDone, "Server", "${serverInfo?.name ?: "CinaVault Premium"} ${serverInfo?.version ?: ""}")
            DetailRow(Icons.Rounded.EnhancedEncryption, "Session storage", "Android Keystore AES-GCM")
        }
        Button(onClick = onLogout, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C2340))) { Icon(Icons.Rounded.Logout, null); Text("  Sign out and erase session") }
    }
}

@Composable
fun HfModelsScreen(hfTokenStatus: String) {
    val uriHandler = LocalUriHandler.current
    var query by remember { mutableStateOf("") }
    val models = listOf(
        "Qwen/Qwen3-4B-Instruct-2507",
        "HuggingFaceTB/SmolLM3-3B",
        "deepseek-ai/DeepSeek-R1-Distill-Qwen-7B",
        "microsoft/Phi-3.5-mini-instruct",
        "katanemo/Arch-Router-1.5B:hf-inference",
    ).filter { it.contains(query, ignoreCase = true) }
    ScreenColumn {
        ScreenHero("Hugging Face Models", "Free, public, ungated model selection for CinaVault AI.", Icons.Rounded.Psychology, CinaVaultSolar)
        GlassPanel {
            Text("HF token: $hfTokenStatus", color = CinaVaultText, fontWeight = FontWeight.Bold)
            OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth(), label = { Text("Search models") })
        }
        models.forEach { model ->
            GlassPanel {
                Text(model, color = CinaVaultText, fontWeight = FontWeight.Black)
                Text("Public · Ungated · Reasoning-ready", color = CinaVaultMuted, fontSize = 11.sp)
                Button(onClick = { uriHandler.openUri("https://huggingface.co/$model") }, modifier = Modifier.fillMaxWidth()) {
                    Text("Select / inspect model")
                }
            }
        }
    }
}

@Composable private fun ScreenColumn(content: @Composable Column.() -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(4.dp), verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
}

@Composable private fun ScreenHero(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, accent: Color) {
    Row(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(accent.copy(alpha = .18f), CinaVaultPanel.copy(alpha = .9f))), RoundedCornerShape(24.dp)).border(1.dp, accent.copy(alpha = .22f), RoundedCornerShape(24.dp)).padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(52.dp).background(accent.copy(alpha = .15f), RoundedCornerShape(17.dp)), contentAlignment = Alignment.Center) { Icon(icon, null, tint = accent) }
        Column(Modifier.padding(start = 14.dp)) { Text(title, color = CinaVaultText, fontSize = 24.sp, fontWeight = FontWeight.Black); Text(subtitle, color = CinaVaultMuted, fontSize = 12.sp) }
    }
}

@Composable private fun GlassPanel(content: @Composable Column.() -> Unit) {
    Column(Modifier.fillMaxWidth().background(CinaVaultPanel.copy(alpha = .88f), RoundedCornerShape(22.dp)).border(1.dp, Color.White.copy(alpha = .08f), RoundedCornerShape(22.dp)).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
}

@Composable private fun TelemetryCard(modifier: Modifier, value: String, label: String, accent: Color) {
    Column(modifier.background(CinaVaultPanel.copy(alpha = .9f), RoundedCornerShape(18.dp)).border(1.dp, accent.copy(alpha = .2f), RoundedCornerShape(18.dp)).padding(12.dp)) { Text(value, color = accent, fontSize = 21.sp, fontWeight = FontWeight.Black); Text(label, color = CinaVaultMuted, fontSize = 9.sp) }
}

@Composable private fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = CinaVaultCyan, modifier = Modifier.size(16.dp)); Column(Modifier.padding(start = 10.dp)) { Text(label, color = CinaVaultMuted, fontSize = 9.sp); Text(value, color = CinaVaultText, fontSize = 12.sp, fontWeight = FontWeight.Bold) } }
}
