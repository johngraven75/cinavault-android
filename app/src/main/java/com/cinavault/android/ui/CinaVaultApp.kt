package com.cinavault.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Cast
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.LiveTv
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.mediarouter.app.MediaRouteButton
import com.cinavault.android.data.AppDestination
import com.cinavault.android.data.CinaVaultUiState
import com.cinavault.android.data.MediaItem
import com.cinavault.android.ui.theme.CinaVaultCyan
import com.cinavault.android.ui.theme.CinaVaultEmerald
import com.cinavault.android.ui.theme.CinaVaultMagenta
import com.cinavault.android.ui.theme.CinaVaultMuted
import com.cinavault.android.ui.theme.CinaVaultOrchid
import com.cinavault.android.ui.theme.CinaVaultPanel
import com.cinavault.android.ui.theme.CinaVaultText
import com.google.android.gms.cast.framework.CastButtonFactory

private val primaryDestinations = listOf(
    AppDestination.Library,
    AppDestination.Sources,
    AppDestination.Downloads,
    AppDestination.LiveTv,
    AppDestination.Server,
    AppDestination.Security,
    AppDestination.Remote,
    AppDestination.Advanced,
    AppDestination.CloudNas,
    AppDestination.Extensions,
    AppDestination.Intelligence,
    AppDestination.Settings,
)

private val compactDestinations = listOf(
    AppDestination.Library,
    AppDestination.Sources,
    AppDestination.Remote,
    AppDestination.Intelligence,
    AppDestination.Settings,
)

@Composable
fun CinaVaultApp(
    state: CinaVaultUiState,
    onPasswordLogin: (String, String, String) -> Unit,
    onAccessKeyLogin: (String, String) -> Unit,
    onLogout: () -> Unit,
    onNavigate: (AppDestination) -> Unit,
    onSearch: (String) -> Unit,
    onOpenMedia: (MediaItem) -> Unit,
    onRefresh: () -> Unit,
    onControlAction: (String) -> Unit,
    onToggleAutopilot: (Boolean) -> Unit,
    onRunAutopilot: () -> Unit,
    onDismissError: () -> Unit,
    absoluteMediaUrl: (String) -> String?,
    sessionToken: () -> String?,
) {
    val session = state.session
    if (session == null) {
        LoginScreen(
            loading = state.loading,
            statusMessage = state.statusMessage,
            onPasswordLogin = onPasswordLogin,
            onAccessKeyLogin = onAccessKeyLogin,
        )
        state.errorMessage?.let { message -> ErrorDialog(message, onDismissError) }
        return
    }

    var commandPaletteOpen by rememberSaveable { mutableStateOf(false) }
    BackHandler(enabled = commandPaletteOpen) { commandPaletteOpen = false }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF02040A))
            .onPreviewKeyEvent { event ->
                val commandPressed = event.isCtrlPressed || event.isMetaPressed
                if (event.type == KeyEventType.KeyDown && commandPressed && event.key == Key.K) {
                    commandPaletteOpen = !commandPaletteOpen
                    true
                } else {
                    false
                }
            },
    ) {
        val wide = maxWidth >= 820.dp
        Box(Modifier.fillMaxSize().background(Color(0xFF02040A))) {
            ExperienceBackdrop()
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(if (wide) 12.dp else 0.dp),
            ) {
                if (wide) {
                    SpatialNavigationRail(
                        selected = state.destination,
                        onNavigate = onNavigate,
                        onLogout = onLogout,
                    )
                    Spacer(Modifier.width(10.dp))
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .then(
                            if (wide) {
                                Modifier.border(
                                    1.dp,
                                    Color.White.copy(alpha = 0.1f),
                                    RoundedCornerShape(28.dp),
                                )
                            } else {
                                Modifier
                            },
                        ),
                    color = CinaVaultPanel.copy(alpha = 0.94f),
                    shape = if (wide) RoundedCornerShape(28.dp) else RoundedCornerShape(0.dp),
                    tonalElevation = 0.dp,
                ) {
                    Scaffold(
                        containerColor = Color(0xFF050918),
                        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
                        topBar = {
                            SpatialCommandBar(
                                state = state,
                                onSearch = onSearch,
                                onRefresh = onRefresh,
                                onOpenCommands = { commandPaletteOpen = true },
                            )
                        },
                        bottomBar = {
                            if (!wide) {
                                SpatialBottomNavigation(
                                    selected = state.destination,
                                    onNavigate = onNavigate,
                                    onOpenCommands = { commandPaletteOpen = true },
                                )
                            }
                        },
                    ) { padding ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding)
                                .padding(horizontal = if (wide) 14.dp else 10.dp),
                        ) {
                            SpatialContextStage(state)
                            Spacer(Modifier.height(10.dp))
                            AnimatedContent(
                                targetState = state.destination,
                                transitionSpec = {
                                    (slideInHorizontally { it / 10 } + fadeIn())
                                        .togetherWith(slideOutHorizontally { -it / 12 } + fadeOut())
                                },
                                label = "destination-transition-safe",
                                modifier = Modifier.weight(1f),
                            ) { destination ->
                                DestinationContent(
                                    destination = destination,
                                    state = state,
                                    onRefresh = onRefresh,
                                    onOpenMedia = onOpenMedia,
                                    onControlAction = onControlAction,
                                    onToggleAutopilot = onToggleAutopilot,
                                    onRunAutopilot = onRunAutopilot,
                                    onLogout = onLogout,
                                    absoluteMediaUrl = absoluteMediaUrl,
                                    sessionToken = sessionToken,
                                )
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = state.loading,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.Center),
            ) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFF050918), RoundedCornerShape(20.dp))
                        .border(1.dp, CinaVaultCyan.copy(alpha = 0.24f), RoundedCornerShape(20.dp))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = CinaVaultCyan)
                }
            }

            if (commandPaletteOpen) {
                CommandPaletteOverlay(
                    selected = state.destination,
                    onNavigate = { destination ->
                        commandPaletteOpen = false
                        onNavigate(destination)
                    },
                    onDismiss = { commandPaletteOpen = false },
                )
            }
        }
    }

    state.errorMessage?.let { message -> ErrorDialog(message, onDismissError) }
}

@Composable
private fun DestinationContent(
    destination: AppDestination,
    state: CinaVaultUiState,
    onRefresh: () -> Unit,
    onOpenMedia: (MediaItem) -> Unit,
    onControlAction: (String) -> Unit,
    onToggleAutopilot: (Boolean) -> Unit,
    onRunAutopilot: () -> Unit,
    onLogout: () -> Unit,
    absoluteMediaUrl: (String) -> String?,
    sessionToken: () -> String?,
) {
    val session = state.session ?: return
    when (destination) {
        AppDestination.Library -> LibraryScreen(
            items = state.filteredLibrary,
            refreshing = state.refreshing,
            absoluteMediaUrl = absoluteMediaUrl,
            sessionToken = sessionToken(),
            onRefresh = onRefresh,
            onOpenMedia = onOpenMedia,
        )
        AppDestination.Player -> PlayerScreen(
            media = state.selectedMedia,
            streamUrl = state.selectedMedia?.streamUrl?.let(absoluteMediaUrl),
            artworkUrl = state.selectedMedia?.artworkUrl?.let(absoluteMediaUrl),
            token = sessionToken(),
        )
        AppDestination.Remote -> RemoteScreen(
            session = session,
            serverInfo = state.serverInfo,
            statusMessage = state.statusMessage,
            onRefresh = onRefresh,
        )
        AppDestination.Casting -> CastingScreen(state.selectedMedia)
        AppDestination.Intelligence -> IntelligenceScreen(
            library = state.library,
            enabled = state.autopilotEnabled,
            lastRefreshEpochMillis = state.lastRefreshEpochMillis,
            onToggle = onToggleAutopilot,
            onRunNow = onRunAutopilot,
        )
        AppDestination.Settings -> SettingsScreen(
            session = session,
            serverInfo = state.serverInfo,
            onLogout = onLogout,
        )
        AppDestination.Sources,
        AppDestination.Downloads,
        AppDestination.LiveTv,
        AppDestination.Server,
        AppDestination.Security,
        AppDestination.Advanced,
        AppDestination.CloudNas,
        AppDestination.Extensions,
        -> PlatformControlScreen(
            destination = destination,
            snapshot = state.controlSnapshot,
            runningAction = state.runningControlAction,
            onAction = onControlAction,
        )
    }
}

@Composable
private fun SpatialCommandBar(
    state: CinaVaultUiState,
    onSearch: (String) -> Unit,
    onRefresh: () -> Unit,
    onOpenCommands: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF050918))
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(
                    Brush.linearGradient(listOf(CinaVaultCyan, CinaVaultOrchid)),
                    RoundedCornerShape(14.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Rounded.Movie, null, tint = Color(0xFF02040A))
        }
        Column(Modifier.weight(0.34f)) {
            Text(
                text = "CINAVAULT",
                color = CinaVaultCyan,
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.8.sp,
            )
            Text(
                text = state.destination.label,
                color = CinaVaultText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = onSearch,
            modifier = Modifier.weight(0.66f),
            singleLine = true,
            leadingIcon = { Icon(Icons.Rounded.Search, null) },
            placeholder = { Text("Search the vault") },
            shape = RoundedCornerShape(15.dp),
        )
        IconButton(onClick = onOpenCommands) {
            Icon(Icons.Rounded.Apps, contentDescription = "Open command palette")
        }
        AndroidView(
            factory = { context ->
                MediaRouteButton(context).also { button ->
                    CastButtonFactory.setUpMediaRouteButton(context, button)
                }
            },
            modifier = Modifier.size(42.dp),
        )
        IconButton(onClick = onRefresh) {
            if (state.refreshing) {
                CircularProgressIndicator(modifier = Modifier.size(19.dp), strokeWidth = 2.dp)
            } else {
                Icon(Icons.Rounded.Refresh, contentDescription = "Refresh")
            }
        }
    }
}

@Composable
private fun SpatialContextStage(state: CinaVaultUiState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    listOf(
                        CinaVaultCyan.copy(alpha = 0.1f),
                        CinaVaultOrchid.copy(alpha = 0.12f),
                        CinaVaultMagenta.copy(alpha = 0.08f),
                    ),
                ),
                RoundedCornerShape(23.dp),
            )
            .border(1.dp, CinaVaultCyan.copy(alpha = 0.14f), RoundedCornerShape(23.dp))
            .padding(horizontal = 17.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = state.destination.eyebrow(),
                color = CinaVaultCyan,
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.8.sp,
            )
            Text(
                text = state.destination.stageTitle(state.selectedMedia?.title),
                color = CinaVaultText,
                fontSize = 23.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = state.statusMessage,
                color = CinaVaultMuted,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MiniTelemetry(Icons.Rounded.GridView, state.library.size.toString(), "Items", CinaVaultCyan)
            MiniTelemetry(
                Icons.Rounded.Security,
                if (state.serverInfo?.localPathsExposed == true) "Check" else "Safe",
                "Privacy",
                CinaVaultEmerald,
            )
            MiniTelemetry(
                Icons.Rounded.AutoAwesome,
                if (state.autopilotEnabled) "Live" else "Paused",
                "AI",
                CinaVaultMagenta,
            )
        }
    }
}

@Composable
private fun MiniTelemetry(icon: ImageVector, value: String, label: String, accent: Color) {
    Column(
        modifier = Modifier
            .background(Color(0xFF080D1C), RoundedCornerShape(14.dp))
            .padding(horizontal = 10.dp, vertical = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, null, tint = accent, modifier = Modifier.size(15.dp))
        Text(value, color = CinaVaultText, fontSize = 11.sp, fontWeight = FontWeight.Black)
        Text(label.uppercase(), color = CinaVaultMuted, fontSize = 7.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SpatialNavigationRail(
    selected: AppDestination,
    onNavigate: (AppDestination) -> Unit,
    onLogout: () -> Unit,
) {
    Column(
        modifier = Modifier
            .width(96.dp)
            .fillMaxHeight()
            .background(CinaVaultPanel, RoundedCornerShape(28.dp))
            .border(1.dp, Color.White.copy(alpha = 0.09f), RoundedCornerShape(28.dp))
            .padding(horizontal = 7.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .background(
                    Brush.linearGradient(listOf(CinaVaultCyan, CinaVaultMagenta)),
                    RoundedCornerShape(18.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Rounded.Movie, null, tint = Color(0xFF02040A))
        }
        Spacer(Modifier.height(8.dp))
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            items(primaryDestinations) { destination ->
                val active = selected == destination
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigate(destination) }
                        .background(
                            if (active) CinaVaultCyan.copy(alpha = 0.13f) else Color.Transparent,
                            RoundedCornerShape(13.dp),
                        )
                        .padding(vertical = 7.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        destination.icon(),
                        destination.label,
                        tint = if (active) CinaVaultCyan else CinaVaultMuted,
                        modifier = Modifier.size(19.dp),
                    )
                    Text(
                        destination.shortLabel(),
                        color = if (active) CinaVaultText else CinaVaultMuted,
                        fontSize = 7.sp,
                        maxLines = 1,
                    )
                }
            }
        }
        IconButton(onClick = onLogout) {
            Icon(Icons.Rounded.Logout, contentDescription = "Sign out")
        }
    }
}

@Composable
private fun SpatialBottomNavigation(
    selected: AppDestination,
    onNavigate: (AppDestination) -> Unit,
    onOpenCommands: () -> Unit,
) {
    NavigationBar(
        containerColor = Color(0xFF050918),
        tonalElevation = 0.dp,
    ) {
        compactDestinations.forEach { destination ->
            NavigationBarItem(
                selected = selected == destination,
                onClick = { onNavigate(destination) },
                icon = { Icon(destination.icon(), destination.label) },
                label = { Text(destination.shortLabel(), fontSize = 8.sp) },
                alwaysShowLabel = false,
            )
        }
        NavigationBarItem(
            selected = selected !in compactDestinations,
            onClick = onOpenCommands,
            icon = { Icon(Icons.Rounded.Apps, "All destinations") },
            label = { Text("More", fontSize = 8.sp) },
            alwaysShowLabel = false,
        )
    }
}

@Composable
private fun CommandPaletteOverlay(
    selected: AppDestination,
    onNavigate: (AppDestination) -> Unit,
    onDismiss: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(query) {
        val normalized = query.trim().lowercase()
        if (normalized.isEmpty()) primaryDestinations
        else primaryDestinations.filter {
            it.label.lowercase().contains(normalized) || it.parityId.contains(normalized)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFA02040D))
            .clickable(onClick = onDismiss)
            .padding(horizontal = 20.dp, vertical = 44.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = false) {}
                .background(Color(0xFF070B1B), RoundedCornerShape(24.dp))
                .border(1.dp, CinaVaultCyan.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                .padding(14.dp),
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Rounded.Search, null) },
                placeholder = { Text("Go anywhere in CinaVault") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
            )
            Spacer(Modifier.height(10.dp))
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.76f),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                items(filtered) { destination ->
                    val active = selected == destination
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigate(destination) }
                            .background(
                                if (active) CinaVaultCyan.copy(alpha = 0.12f) else Color(0xFF0A1022),
                                RoundedCornerShape(15.dp),
                            )
                            .padding(13.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Icon(
                            destination.icon(),
                            contentDescription = null,
                            tint = if (active) CinaVaultCyan else CinaVaultOrchid,
                        )
                        Column(Modifier.weight(1f)) {
                            Text(destination.label, color = CinaVaultText, fontWeight = FontWeight.Black)
                            Text(destination.eyebrow(), color = CinaVaultMuted, fontSize = 10.sp)
                        }
                        if (active) {
                            Text("ACTIVE", color = CinaVaultCyan, fontSize = 8.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
            Text(
                text = "Ctrl/Command+K toggles this surface on hardware keyboards",
                color = CinaVaultMuted,
                fontSize = 9.sp,
                modifier = Modifier.padding(top = 10.dp),
            )
        }
    }
}

private fun AppDestination.icon(): ImageVector = when (this) {
    AppDestination.Library -> Icons.Rounded.GridView
    AppDestination.Sources -> Icons.Rounded.Folder
    AppDestination.Downloads -> Icons.Rounded.Download
    AppDestination.LiveTv -> Icons.Rounded.LiveTv
    AppDestination.Server -> Icons.Rounded.Memory
    AppDestination.Security -> Icons.Rounded.Security
    AppDestination.Remote -> Icons.Rounded.Cloud
    AppDestination.Advanced -> Icons.Rounded.Tune
    AppDestination.CloudNas -> Icons.Rounded.Storage
    AppDestination.Extensions -> Icons.Rounded.Extension
    AppDestination.Intelligence -> Icons.Rounded.AutoAwesome
    AppDestination.Settings -> Icons.Rounded.Settings
    AppDestination.Casting -> Icons.Rounded.Cast
    AppDestination.Player -> Icons.Rounded.PlayCircle
}

private fun AppDestination.shortLabel(): String = when (this) {
    AppDestination.Library -> "Vault"
    AppDestination.Sources -> "Sources"
    AppDestination.Downloads -> "Queue"
    AppDestination.LiveTv -> "Live"
    AppDestination.Server -> "Server"
    AppDestination.Security -> "Guard"
    AppDestination.Remote -> "Remote"
    AppDestination.Advanced -> "Tools"
    AppDestination.CloudNas -> "Cloud"
    AppDestination.Extensions -> "Extend"
    AppDestination.Intelligence -> "AI"
    AppDestination.Settings -> "Setup"
    AppDestination.Casting -> "Cast"
    AppDestination.Player -> "Play"
}

private fun AppDestination.eyebrow(): String = when (this) {
    AppDestination.Library -> "CINEMATIC LIBRARY"
    AppDestination.Sources -> "AUTONOMOUS INGESTION"
    AppDestination.Downloads -> "ACQUISITION STREAM"
    AppDestination.LiveTv -> "BROADCAST FABRIC"
    AppDestination.Server -> "EMBEDDED MEDIA CORE"
    AppDestination.Security -> "TRUSTED COMPUTE"
    AppDestination.Remote -> "ANYWHERE ACCESS"
    AppDestination.Advanced -> "EXPERT SYSTEMS"
    AppDestination.CloudNas -> "STORAGE FABRIC"
    AppDestination.Extensions -> "CAPABILITY LAYER"
    AppDestination.Intelligence -> "AUTONOMOUS INTELLIGENCE"
    AppDestination.Settings -> "EXPERIENCE DESIGN"
    AppDestination.Casting -> "DEVICE ORBIT"
    AppDestination.Player -> "SECURE PLAYBACK"
}

private fun AppDestination.stageTitle(selectedTitle: String?): String = when (this) {
    AppDestination.Library -> "The Vault"
    AppDestination.Sources -> "Source Constellation"
    AppDestination.Downloads -> "Incoming Media"
    AppDestination.LiveTv -> "Live Signal"
    AppDestination.Server -> "Server Nexus"
    AppDestination.Security -> "Security Matrix"
    AppDestination.Remote -> "Remote Orbit"
    AppDestination.Advanced -> "Control Lab"
    AppDestination.CloudNas -> "Cloud Mesh"
    AppDestination.Extensions -> "Extension Forge"
    AppDestination.Intelligence -> "AI Autopilot"
    AppDestination.Settings -> "Personalize CinaVault"
    AppDestination.Casting -> "Casting Center"
    AppDestination.Player -> selectedTitle ?: "Now Playing"
}

@Composable
private fun ErrorDialog(message: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF080D1C),
        titleContentColor = CinaVaultText,
        textContentColor = CinaVaultMuted,
        title = { Text("CinaVault needs attention") },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        },
    )
}
