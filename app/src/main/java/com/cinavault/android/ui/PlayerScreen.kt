package com.cinavault.android.ui

import android.widget.Toast
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cast
import androidx.compose.material.icons.rounded.HighQuality
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
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
import androidx.media3.common.MediaItem as Media3Item
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import com.cinavault.android.data.MediaItem
import com.cinavault.android.ui.theme.CinaVaultCyan
import com.cinavault.android.ui.theme.CinaVaultMagenta
import com.cinavault.android.ui.theme.CinaVaultMuted
import com.cinavault.android.ui.theme.CinaVaultPanel
import com.cinavault.android.ui.theme.CinaVaultText
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaLoadRequestData
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.framework.CastContext

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    media: MediaItem?,
    streamUrl: String?,
    artworkUrl: String?,
    token: String?,
) {
    val context = LocalContext.current
    if (media == null || streamUrl.isNullOrBlank() || token.isNullOrBlank()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Rounded.PlayArrow, null, tint = CinaVaultCyan)
                Spacer(Modifier.height(8.dp))
                Text("Select a media card to begin playback", color = CinaVaultText)
            }
        }
        return
    }

    val player = remember(streamUrl, token) {
        val dataSourceFactory = DefaultHttpDataSource.Factory()
            .setDefaultRequestProperties(
                mapOf(
                    "Authorization" to "Bearer $token",
                    "Cache-Control" to "no-store",
                ),
            )
        ExoPlayer.Builder(context)
            .setMediaSourceFactory(DefaultMediaSourceFactory(dataSourceFactory))
            .build()
            .apply {
                setMediaItem(Media3Item.fromUri(streamUrl))
                prepare()
                playWhenReady = true
            }
    }

    DisposableEffect(player) {
        onDispose { player.release() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(4.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 1200.dp)
                .aspectRatio(16f / 9f)
                .background(Color.Black, RoundedCornerShape(22.dp)),
        ) {
            AndroidView(
                factory = { viewContext ->
                    PlayerView(viewContext).apply {
                        useController = true
                        this.player = player
                    }
                },
                update = { view -> view.player = player },
                modifier = Modifier.fillMaxSize(),
            )
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                StatusPill(Icons.Rounded.Lock, "HTTPS")
                media.resolution?.let { StatusPill(Icons.Rounded.HighQuality, it) }
            }
        }

        Spacer(Modifier.height(14.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            CinaVaultPanel.copy(alpha = 0.94f),
                            Color(0xFF160B2A).copy(alpha = 0.86f),
                        ),
                    ),
                    RoundedCornerShape(22.dp),
                )
                .padding(17.dp),
        ) {
            Row(verticalAlignment = Alignment.Top) {
                RemoteArtwork(
                    absoluteUrl = artworkUrl,
                    token = token,
                    title = media.title,
                    modifier = Modifier
                        .weight(0.3f)
                        .aspectRatio(2f / 3f),
                )
                Column(
                    modifier = Modifier
                        .weight(0.7f)
                        .padding(start = 15.dp),
                ) {
                    Text(
                        text = media.title,
                        color = CinaVaultText,
                        fontSize = 25.sp,
                        lineHeight = 28.sp,
                        fontWeight = FontWeight.Black,
                    )
                    Text(
                        text = listOfNotNull(
                            media.year?.toString(),
                            media.genre,
                            media.resolution,
                            media.codec,
                        ).joinToString("  ·  "),
                        color = CinaVaultCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(11.dp))
                    Text(
                        text = media.overview ?: "CinaVault AI is still enriching this title.",
                        color = CinaVaultMuted,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        maxLines = 7,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val castSession = runCatching {
                                CastContext.getSharedInstance(context)
                                    .sessionManager
                                    .currentCastSession
                            }.getOrNull()
                            val remoteClient = castSession?.remoteMediaClient
                            if (remoteClient == null) {
                                Toast.makeText(
                                    context,
                                    "Choose a Cast device from the Cast button first.",
                                    Toast.LENGTH_LONG,
                                ).show()
                            } else {
                                val metadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE).apply {
                                    putString(MediaMetadata.KEY_TITLE, media.title)
                                }
                                val mediaInfo = MediaInfo.Builder(streamUrl)
                                    .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                                    .setContentType("video/mp4")
                                    .setMetadata(metadata)
                                    .build()
                                remoteClient.load(
                                    MediaLoadRequestData.Builder()
                                        .setMediaInfo(mediaInfo)
                                        .setAutoplay(true)
                                        .build(),
                                )
                                Toast.makeText(context, "Casting ${media.title}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CinaVaultMagenta,
                            contentColor = Color.White,
                        ),
                        shape = RoundedCornerShape(15.dp),
                    ) {
                        Icon(Icons.Rounded.Cast, null)
                        Text("  Cast now", fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
) {
    Row(
        modifier = Modifier
            .background(Color.Black.copy(alpha = 0.62f), RoundedCornerShape(50))
            .padding(horizontal = 8.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = CinaVaultCyan, modifier = Modifier.padding(end = 4.dp))
        Text(label, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
    }
}
