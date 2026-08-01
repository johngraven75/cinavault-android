package com.cinavault.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
fun RemoteScreen(
    session: RemoteSession,
    serverInfo: ServerInfo?,
    statusMessage: String,
    onRefresh: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ScreenHero(
            title = "Remote Orbit",
            subtitle = "Encrypted account-scoped access through the CinaVault HTTPS relay.",
            icon = Icons.Rounded.CloudDone,
            accent = CinaVaultCyan,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            TelemetryCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Rounded.Lock,
                value = "HTTPS",
                label = "Transport",
                accent = CinaVaultEmerald,
            )
            TelemetryCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Rounded.Key,
                value = "Opaque",
                label = "Media keys",
                accent = CinaVaultMagenta,
            )
            TelemetryCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Rounded.Security,
                value = "Hidden",
                label = "Local paths",
                accent = CinaVaultSolar,
            )
        }

        GlassPanel {
            DetailRow(Icons.Rounded.Link, "Endpoint", session.endpoint)
            DetailRow(Icons.Rounded.Person, "Account", session.email)
            DetailRow(
                Icons.Rounded.EnhancedEncryption,
                "Server transport",
                serverInfo?.remoteTransport ?: "HTTPS relay",
            )
            DetailRow(
                Icons.Rounded.Memory,
                "Server build",
                "${serverInfo?.version ?: "2.0.2"} · ${serverInfo?.build ?: "v2 Build 2"}",
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Permissions",
                color = CinaVaultMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.4.sp,
            )
            FlowRow(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                session.permissions.forEach { permission ->
                    Text(
                        text = permission,
                        color = CinaVaultCyan,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(CinaVaultCyan.copy(alpha = 0.08f), RoundedCornerShape(50))
                            .border(0.5.dp, CinaVaultCyan.copy(alpha = 0.22f), RoundedCornerShape(50))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }
        }

        FilledTonalButton(onClick = onRefresh) {
            Icon(Icons.Rounded.Refresh, null)
            Text("  Refresh remote state")
        }
        Text(statusMessage, color = CinaVaultMuted, fontSize = 11.sp)
    }
}

@Composable
fun CastingScreen(selectedMedia: MediaItem?) {
    val context = LocalContext.current
    val castContext = remember {
        runCatching { CastContext.getSharedInstance(context) }.getOrNull()
    }
    var connectedDevice by remember {
        mutableStateOf(castContext?.sessionManager?.currentCastSession?.castDevice?.friendlyName)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        ScreenHero(
            title = "Casting Center",
            subtitle = "Automatic Cast discovery, reconnection, volume, and remote playback control.",
            icon = Icons.Rounded.CastConnected,
            accent = CinaVaultMagenta,
        )

        GlassPanel {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(
                            Brush.linearGradient(listOf(CinaVaultMagenta, CinaVaultOrchid)),
                            RoundedCornerShape(22.dp),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    AndroidView(
                        factory = { viewContext ->
                            MediaRouteButton(viewContext).also { button ->
                                CastButtonFactory.setUpMediaRouteButton(viewContext, button)
                            }
                        },
                        modifier = Modifier.size(54.dp),
                    )
                }
                Column(Modifier.padding(start = 16.dp)) {
                    Text(
                        text = connectedDevice ?: "Choose a Cast device",
                        color = CinaVaultText,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                    )
                    Text(
                        text = if (connectedDevice == null) {
                            "Tap the Cast icon to discover nearby receivers."
                        } else {
                            "Connected and ready for secure stream handoff."
                        },
                        color = CinaVaultMuted,
                        fontSize = 12.sp,
                    )
                }
            }
        }

        GlassPanel {
            Text(
                text = "Selected media",
                color = CinaVaultCyan,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp,
            )
            Text(
                text = selectedMedia?.title ?: "No media selected",
                color = CinaVaultText,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(top = 8.dp),
            )
            Text(
                text = selectedMedia?.let {
                    listOfNotNull(it.year?.toString(), it.genre, it.resolution).joinToString(" · ")
                } ?: "Open a library card, then use Cast now from the player.",
                color = CinaVaultMuted,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        FilledTonalButton(
            onClick = {
                connectedDevice = castContext
                    ?.sessionManager
                    ?.currentCastSession
                    ?.castDevice
                    ?.friendlyName
            },
        ) {
            Icon(Icons.Rounded.Refresh, null)
            Text("  Refresh Cast session")
        }
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
    val missingArtwork = library.count { it.artworkUrl.isNullOrBlank() }
    val unverified = library.count { !it.verified }
    val favorites = library.count { it.favorite }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ScreenHero(
            title = "AI Autopilot",
            subtitle = "Continuous library reconciliation, smart ordering, artwork health, and metadata insights.",
            icon = Icons.Rounded.AutoAwesome,
            accent = CinaVaultMagenta,
        )

        GlassPanel {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Autonomous media management", color = CinaVaultText, fontWeight = FontWeight.Black)
                    Text(
                        "Automatically refresh and prioritize the encrypted remote library.",
                        color = CinaVaultMuted,
                        fontSize = 12.sp,
                    )
                }
                Switch(checked = enabled, onCheckedChange = onToggle)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            TelemetryCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Rounded.AutoAwesome,
                value = missingArtwork.toString(),
                label = "Artwork needs",
                accent = CinaVaultMagenta,
            )
            TelemetryCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Rounded.VerifiedUser,
                value = unverified.toString(),
                label = "Unverified",
                accent = CinaVaultSolar,
            )
            TelemetryCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Rounded.CloudDone,
                value = favorites.toString(),
                label = "Priority titles",
                accent = CinaVaultEmerald,
            )
        }

        GlassPanel {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("HF token: $hfTokenStatus", color = CinaVaultText, fontWeight = FontWeight.Bold)
                Text("Metadata providers: $providerStatus", color = CinaVaultMuted, fontSize = 12.sp)
                Text(
                    "Provider secrets remain in the Windows secure store; Android receives readiness only over the authenticated HTTPS session.",
                    color = CinaVaultMuted,
                    fontSize = 10.sp,
                )
            }
        }

        Button(
            onClick = onRunNow,
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = CinaVaultMagenta,
                contentColor = Color.White,
            ),
            shape = RoundedCornerShape(16.dp),
        ) {
            Icon(Icons.Rounded.AutoAwesome, null)
            Text("  Run Autopilot now", fontWeight = FontWeight.Black)
        }

        Text(
            text = lastRefreshEpochMillis?.let {
                "Last synchronized ${android.text.format.DateUtils.getRelativeTimeSpanString(it)}"
            } ?: "Autopilot has not synchronized yet.",
            color = CinaVaultMuted,
            fontSize = 11.sp,
        )
    }
}

@Composable
fun SettingsScreen(
    session: RemoteSession,
    serverInfo: ServerInfo?,
    onLogout: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ScreenHero(
            title = "Experience Control",
            subtitle = "Account, security, build identity, and application behavior.",
            icon = Icons.Rounded.Security,
            accent = CinaVaultCyan,
        )
        GlassPanel {
            DetailRow(Icons.Rounded.Person, "Signed in", session.email)
            DetailRow(Icons.Rounded.Lock, "Endpoint", session.endpoint)
            DetailRow(Icons.Rounded.Memory, "Android build", BuildConfig.CINAVAULT_BUILD)
            DetailRow(
                Icons.Rounded.CloudDone,
                "Server",
                "${serverInfo?.name ?: "CinaVault Premium"} ${serverInfo?.version ?: ""}",
            )
            DetailRow(Icons.Rounded.EnhancedEncryption, "Session storage", "Android Keystore AES-GCM")
            DetailRow(Icons.Rounded.Security, "Backup policy", "Account data excluded")
        }
        Button(
            onClick = onLogout,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF7C2340),
                contentColor = Color.White,
            ),
            shape = RoundedCornerShape(16.dp),
        ) {
            Icon(Icons.Rounded.Logout, null)
            Text("  Sign out and erase session", fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun ScreenHero(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    listOf(accent.copy(alpha = 0.18f), CinaVaultPanel.copy(alpha = 0.9f)),
                ),
                RoundedCornerShape(24.dp),
            )
            .border(1.dp, accent.copy(alpha = 0.22f), RoundedCornerShape(24.dp))
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(accent.copy(alpha = 0.15f), RoundedCornerShape(17.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = accent)
        }
        Column(Modifier.padding(start = 14.dp)) {
            Text(title, color = CinaVaultText, fontSize = 24.sp, fontWeight = FontWeight.Black)
            Text(subtitle, color = CinaVaultMuted, fontSize = 12.sp, lineHeight = 17.sp)
        }
    }
}

@Composable
private fun GlassPanel(content: @Composable Column.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CinaVaultPanel.copy(alpha = 0.88f), RoundedCornerShape(22.dp))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(22.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        content = content,
    )
}

@Composable
private fun TelemetryCard(
    modifier: Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    accent: Color,
) {
    Column(
        modifier = modifier
            .background(CinaVaultPanel.copy(alpha = 0.9f), RoundedCornerShape(18.dp))
            .border(1.dp, accent.copy(alpha = 0.18f), RoundedCornerShape(18.dp))
            .padding(12.dp),
    ) {
        Icon(icon, null, tint = accent, modifier = Modifier.size(18.dp))
        Text(
            text = value,
            color = CinaVaultText,
            fontSize = 17.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 8.dp),
        )
        Text(
            text = label.uppercase(),
            color = CinaVaultMuted,
            fontSize = 8.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
        )
    }
}

@Composable
private fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(icon, null, tint = CinaVaultCyan, modifier = Modifier.size(17.dp))
        Column(Modifier.padding(start = 10.dp)) {
            Text(label.uppercase(), color = CinaVaultMuted, fontSize = 8.sp, fontWeight = FontWeight.Black)
            Text(
                value,
                color = CinaVaultText,
                fontSize = 12.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
