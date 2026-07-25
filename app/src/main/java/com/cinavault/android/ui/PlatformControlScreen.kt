package com.cinavault.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.LiveTv
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cinavault.android.data.AppDestination
import com.cinavault.android.data.ControlAction
import com.cinavault.android.data.ControlMetric
import com.cinavault.android.data.ControlSnapshot
import com.cinavault.android.ui.theme.CinaVaultCyan
import com.cinavault.android.ui.theme.CinaVaultEmerald
import com.cinavault.android.ui.theme.CinaVaultMagenta
import com.cinavault.android.ui.theme.CinaVaultMuted
import com.cinavault.android.ui.theme.CinaVaultOrchid
import com.cinavault.android.ui.theme.CinaVaultPanel
import com.cinavault.android.ui.theme.CinaVaultSolar
import com.cinavault.android.ui.theme.CinaVaultText

private data class ControlPresentation(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val accent: Color,
)

@Composable
fun PlatformControlScreen(
    destination: AppDestination,
    snapshot: ControlSnapshot,
    runningAction: String?,
    onAction: (String) -> Unit,
) {
    val presentation = destination.presentation()
    val section = snapshot.section(destination.parityId)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            ControlHero(
                presentation = presentation,
                available = snapshot.available && section != null,
            )
        }

        if (!snapshot.available || section == null) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CinaVaultPanel, RoundedCornerShape(22.dp))
                        .border(
                            width = 1.dp,
                            color = CinaVaultSolar.copy(alpha = 0.32f),
                            shape = RoundedCornerShape(22.dp),
                        )
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "CONTROL ENDPOINT PENDING",
                        color = CinaVaultSolar,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.6.sp,
                    )
                    Text(
                        text = snapshot.message,
                        color = CinaVaultText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "The screen is present for platform parity, but no action is shown as available until the authenticated Windows control API confirms support.",
                        color = CinaVaultMuted,
                        fontSize = 12.sp,
                    )
                }
            }
            return@LazyColumn
        }

        item {
            Text(
                text = section.subtitle.ifBlank { presentation.subtitle },
                color = CinaVaultMuted,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 6.dp),
            )
        }

        if (section.metrics.isNotEmpty()) {
            item {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    section.metrics.forEach { metric ->
                        ControlMetricCard(metric, presentation.accent)
                    }
                }
            }
        }

        if (section.actions.isEmpty()) {
            item {
                Text(
                    text = "No remote actions are currently exposed for this section.",
                    color = CinaVaultMuted,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(12.dp),
                )
            }
        } else {
            items(section.actions.size) { index ->
                val action = section.actions[index]
                ControlActionCard(
                    action = action,
                    running = runningAction == action.id,
                    accent = presentation.accent,
                    onAction = onAction,
                )
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun ControlHero(
    presentation: ControlPresentation,
    available: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CinaVaultPanel, RoundedCornerShape(24.dp))
            .border(
                width = 1.dp,
                color = presentation.accent.copy(alpha = 0.24f),
                shape = RoundedCornerShape(24.dp),
            )
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(
            modifier = Modifier
                .size(58.dp)
                .background(
                    presentation.accent.copy(alpha = 0.13f),
                    RoundedCornerShape(18.dp),
                )
                .border(
                    1.dp,
                    presentation.accent.copy(alpha = 0.32f),
                    RoundedCornerShape(18.dp),
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = presentation.icon,
                contentDescription = null,
                tint = presentation.accent,
                modifier = Modifier.size(28.dp),
            )
        }
        Column(Modifier.weight(1f)) {
            Text(
                text = presentation.title,
                color = CinaVaultText,
                fontSize = 23.sp,
                fontWeight = FontWeight.Black,
            )
            Text(
                text = presentation.subtitle,
                color = CinaVaultMuted,
                fontSize = 12.sp,
            )
        }
        Text(
            text = if (available) "LIVE" else "PENDING",
            color = if (available) CinaVaultEmerald else CinaVaultSolar,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.2.sp,
            modifier = Modifier
                .background(
                    (if (available) CinaVaultEmerald else CinaVaultSolar).copy(alpha = 0.1f),
                    RoundedCornerShape(50),
                )
                .padding(horizontal = 9.dp, vertical = 5.dp),
        )
    }
}

@Composable
private fun ControlMetricCard(metric: ControlMetric, accent: Color) {
    val statusColor = when (metric.status.lowercase()) {
        "warning" -> CinaVaultSolar
        "error", "critical" -> Color(0xFFFF6D88)
        "success", "healthy" -> CinaVaultEmerald
        else -> accent
    }
    Column(
        modifier = Modifier
            .background(CinaVaultPanel, RoundedCornerShape(16.dp))
            .border(1.dp, statusColor.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 11.dp),
    ) {
        Text(metric.value, color = CinaVaultText, fontWeight = FontWeight.Black, fontSize = 16.sp)
        Text(
            metric.label.uppercase(),
            color = statusColor,
            fontWeight = FontWeight.Bold,
            fontSize = 8.sp,
            letterSpacing = 1.sp,
        )
    }
}

@Composable
private fun ControlActionCard(
    action: ControlAction,
    running: Boolean,
    accent: Color,
    onAction: (String) -> Unit,
) {
    val actionColor = if (action.dangerous) Color(0xFFFF6D88) else accent
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CinaVaultPanel, RoundedCornerShape(18.dp))
            .border(1.dp, actionColor.copy(alpha = 0.18f), RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text(action.label, color = CinaVaultText, fontWeight = FontWeight.Black, fontSize = 14.sp)
            Text(action.description, color = CinaVaultMuted, fontSize = 11.sp)
        }
        Button(
            onClick = { onAction(action.id) },
            enabled = action.enabled && !running,
            colors = ButtonDefaults.buttonColors(
                containerColor = actionColor,
                contentColor = if (action.dangerous) Color.White else Color(0xFF02040A),
                disabledContainerColor = actionColor.copy(alpha = 0.16f),
            ),
            shape = RoundedCornerShape(14.dp),
        ) {
            if (running) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = Color.White,
                )
            } else {
                Text("Run", fontWeight = FontWeight.Black)
            }
        }
    }
}

private fun AppDestination.presentation(): ControlPresentation = when (this) {
    AppDestination.Sources -> ControlPresentation(
        "Source Constellation",
        "Add, scan, enrich, and reconcile authorized media sources.",
        Icons.Rounded.Folder,
        CinaVaultCyan,
    )
    AppDestination.Downloads -> ControlPresentation(
        "Incoming Media",
        "Observe downloads, imports, and automated library handoff.",
        Icons.Rounded.Download,
        CinaVaultOrchid,
    )
    AppDestination.LiveTv -> ControlPresentation(
        "Live Signal",
        "Navigate live channels, guide status, and stream availability.",
        Icons.Rounded.LiveTv,
        CinaVaultMagenta,
    )
    AppDestination.Server -> ControlPresentation(
        "Server Nexus",
        "Inspect embedded services, clients, streaming health, and runtime state.",
        Icons.Rounded.Memory,
        CinaVaultCyan,
    )
    AppDestination.Security -> ControlPresentation(
        "Security Matrix",
        "Review identity, encryption, privacy boundaries, and threat state.",
        Icons.Rounded.Security,
        CinaVaultEmerald,
    )
    AppDestination.Advanced -> ControlPresentation(
        "Control Lab",
        "Run diagnostics, repairs, and expert operational tasks.",
        Icons.Rounded.Tune,
        CinaVaultSolar,
    )
    AppDestination.CloudNas -> ControlPresentation(
        "Cloud Mesh",
        "Manage cloud storage, NAS connectivity, sync, and distributed media state.",
        Icons.Rounded.Storage,
        CinaVaultOrchid,
    )
    AppDestination.Extensions -> ControlPresentation(
        "Extension Forge",
        "Inspect metadata providers, playback tools, and permanent extensions.",
        Icons.Rounded.Extension,
        CinaVaultMagenta,
    )
    AppDestination.Intelligence -> ControlPresentation(
        "AI Autopilot",
        "Guide automated scanning, identification, artwork repair, and optimization.",
        Icons.Rounded.AutoAwesome,
        CinaVaultMagenta,
    )
    AppDestination.Settings -> ControlPresentation(
        "Experience Control",
        "Manage persistent behavior, automation policy, and user-facing options.",
        Icons.Rounded.Settings,
        CinaVaultCyan,
    )
    AppDestination.Remote -> ControlPresentation(
        "Remote Orbit",
        "Inspect NAT traversal, encrypted relay, sessions, and reachability.",
        Icons.Rounded.Cloud,
        CinaVaultCyan,
    )
    AppDestination.Library,
    AppDestination.Casting,
    AppDestination.Player,
    -> ControlPresentation(
        label,
        "CinaVault platform experience",
        Icons.Rounded.Settings,
        CinaVaultCyan,
    )
}
