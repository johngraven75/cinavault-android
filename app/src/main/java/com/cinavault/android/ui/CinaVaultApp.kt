package com.cinavault.android.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Cast
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.LiveTv
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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

private val destinations = listOf(
    AppDestination.Library,
    AppDestination.Player,
    AppDestination.Remote,
    AppDestination.Casting,
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
        state.errorMessage?.let { message ->
            ErrorDialog(message, onDismissError)
        }
        return
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val wide = maxWidth >= 820.dp
        Box(Modifier.fillMaxSize()) {
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
                    color = CinaVaultPanel.copy(alpha = 0.76f),
                    shape = if (wide) RoundedCornerShape(28.dp) else RoundedCornerShape(0.dp),
                    tonalElevation = 0.dp,
                ) {
                    Scaffold(
                        containerColor = Color.Transparent,
                        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
                        topBar = {
                            SpatialCommandBar(
                                state = state,
                                onSearch = onSearch,
                                onRefresh = onRefresh,
                            )
                        },
                        bottomBar = {
                            if (!wide) {
                                SpatialBottomNavigation(
                                    selected = state.destination,
                                    onNavigate = onNavigate,
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
                                    (slideInHorizontally { it / 8 } + fadeIn() + scaleIn(initialScale = 0.985f))
                                        .togetherWith(
                                            slideOutHorizontally { -it / 10 } + fadeOut() + scaleOut(targetScale = 0.99f),
                                        )
                                },
                                label = "destination-transition",
                                modifier = Modifier.weight(1f),
                            ) { destination ->
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
                                }
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
                        .background(Color.Black.copy(alpha = 0.72f), RoundedCornerShape(20.dp))
                        .border(1.dp, CinaVaultCyan.copy(alpha = 0.24f), RoundedCornerShape(20.dp))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = CinaVaultCyan)
                }
            }
        }
    }

    state.errorMessage?.let { message -> ErrorDialog(message, onDismissError) }
}

@Composable
private fun SpatialCommandBar(
    state: CinaVaultUiState,
    onSearch: (String) -> Unit,
    onRefresh: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .background(Color.Black.copy(alpha = 0.22f), RoundedCornerShape(20.dp))
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
        Column(Modifier.weight(0.35f)) {
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
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = onSearch,
            modifier = Modifier.weight(0.65f),
            singleLine = true,
            leadingIcon = { Icon(Icons.Rounded.Search, null) },
            placeholder = { Text("Search the vault") },
            shape = RoundedCornerShape(15.dp),
        )
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
                text = when (state.destination) {
                    AppDestination.Library -> "CINEMATIC LIBRARY"
                    AppDestination.Player -> "SECURE PLAYBACK"
                    AppDestination.Remote -> "ANYWHERE ACCESS"
                    AppDestination.Casting -> "DEVICE ORBIT"
                    AppDestination.Intelligence -> "AUTONOMOUS INTELLIGENCE"
                    AppDestination.Settings -> "EXPERIENCE CONTROL"
                },
                color = CinaVaultCyan,
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.8.sp,
            )
            Text(
                text = when (state.destination) {
                    AppDestination.Library -> "The Vault"
                    AppDestination.Player -> state.selectedMedia?.title ?: "Now Playing"
                    AppDestination.Remote -> "Remote Orbit"
                    AppDestination.Casting -> "Casting Center"
                    AppDestination.Intelligence -> "AI Autopilot"
                    AppDestination.Settings -> "Personalize CinaVault"
                },
                color = CinaVaultText,
                fontSize = 24.sp,
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
                Icons.Rounded.Shield,
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
            .background(Color.Black.copy(alpha = 0.22f), RoundedCornerShape(14.dp))
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
    NavigationRail(
        modifier = Modifier
            .width(86.dp)
            .fillMaxHeight()
            .background(CinaVaultPanel.copy(alpha = 0.88f), RoundedCornerShape(28.dp))
            .border(1.dp, Color.White.copy(alpha = 0.09f), RoundedCornerShape(28.dp)),
        containerColor = Color.Transparent,
        header = {
            Box(
                modifier = Modifier
                    .padding(vertical = 14.dp)
                    .size(50.dp)
                    .background(
                        Brush.linearGradient(listOf(CinaVaultCyan, CinaVaultMagenta)),
                        RoundedCornerShape(18.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.Movie, null, tint = Color(0xFF02040A))
            }
        },
    ) {
        destinations.forEach { destination ->
            NavigationRailItem(
                selected = selected == destination,
                onClick = { onNavigate(destination) },
                icon = { Icon(destination.icon(), destination.label) },
                label = { Text(destination.shortLabel(), fontSize = 8.sp) },
                alwaysShowLabel = false,
            )
        }
        Spacer(Modifier.weight(1f))
        NavigationRailItem(
            selected = false,
            onClick = onLogout,
            icon = { Icon(Icons.Rounded.Logout, "Sign out") },
        )
        Spacer(Modifier.height(10.dp))
    }
}

@Composable
private fun SpatialBottomNavigation(
    selected: AppDestination,
    onNavigate: (AppDestination) -> Unit,
) {
    NavigationBar(
        containerColor = CinaVaultPanel.copy(alpha = 0.97f),
        tonalElevation = 0.dp,
    ) {
        destinations.forEach { destination ->
            NavigationBarItem(
                selected = selected == destination,
                onClick = { onNavigate(destination) },
                icon = { Icon(destination.icon(), destination.label) },
                label = { Text(destination.shortLabel(), fontSize = 8.sp) },
                alwaysShowLabel = false,
            )
        }
    }
}

private fun AppDestination.icon(): ImageVector = when (this) {
    AppDestination.Library -> Icons.Rounded.GridView
    AppDestination.Player -> Icons.Rounded.PlayCircle
    AppDestination.Remote -> Icons.Rounded.Cloud
    AppDestination.Casting -> Icons.Rounded.Cast
    AppDestination.Intelligence -> Icons.Rounded.AutoAwesome
    AppDestination.Settings -> Icons.Rounded.Settings
}

private fun AppDestination.shortLabel(): String = when (this) {
    AppDestination.Library -> "Vault"
    AppDestination.Player -> "Play"
    AppDestination.Remote -> "Remote"
    AppDestination.Casting -> "Cast"
    AppDestination.Intelligence -> "AI"
    AppDestination.Settings -> "Setup"
}

@Composable
private fun ErrorDialog(message: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("CinaVault needs attention") },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        },
    )
}
