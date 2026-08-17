package com.cinavault.android.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cinavault.android.data.MediaItem
import com.cinavault.android.ui.theme.CinaVaultCyan
import com.cinavault.android.ui.theme.CinaVaultEmerald
import com.cinavault.android.ui.theme.CinaVaultMagenta
import com.cinavault.android.ui.theme.CinaVaultMuted
import com.cinavault.android.ui.theme.CinaVaultPanel
import com.cinavault.android.ui.theme.CinaVaultSolar
import com.cinavault.android.ui.theme.CinaVaultText

@Composable
fun LibraryScreen(
    items: List<MediaItem>,
    refreshing: Boolean,
    lastRefreshEpochMillis: Long?,
    absoluteMediaUrl: (String) -> String?,
    sessionToken: String?,
    onRefresh: () -> Unit,
    onOpenMedia: (MediaItem) -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = CinaVaultMagenta,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = "  AI-MANAGED LIBRARY",
                        color = CinaVaultCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.6.sp,
                    )
                }
                Text(
                    text = "${items.size} media cards",
                    color = CinaVaultText,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Black,
                )
            }
            FilledTonalIconButton(onClick = onRefresh, enabled = !refreshing) {
                if (refreshing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(19.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Icon(Icons.Rounded.Refresh, contentDescription = "Refresh library")
                }
            }
        }

        if (items.isEmpty() && !refreshing) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = CinaVaultCyan.copy(alpha = 0.55f),
                        modifier = Modifier.size(52.dp),
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "No media matched this view",
                        color = CinaVaultText,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "Add sources on the desktop server or clear the search.",
                        color = CinaVaultMuted,
                        fontSize = 12.sp,
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 112.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(4.dp, 4.dp, 4.dp, 28.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(items, key = { it.mediaKey }) { item ->
                    MediaCard(
                        item = item,
                        artworkUrl = item.artworkUrl?.let(absoluteMediaUrl),
                        sessionToken = sessionToken,
                        refreshEpochMillis = lastRefreshEpochMillis,
                        onOpen = { onOpenMedia(item) },
                    )
                }
            }
        }
    }
}

@Composable
private fun MediaCard(
    item: MediaItem,
    artworkUrl: String?,
    sessionToken: String?,
    refreshEpochMillis: Long?,
    onOpen: () -> Unit,
) {
    val shape = RoundedCornerShape(17.dp)
    Column(
        modifier = Modifier
            .animateContentSize()
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(
                        if (item.favorite) CinaVaultSolar.copy(alpha = 0.48f) else CinaVaultCyan.copy(alpha = 0.24f),
                        CinaVaultMagenta.copy(alpha = 0.16f),
                        Color.White.copy(alpha = 0.05f),
                    ),
                ),
                shape = shape,
            )
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.075f),
                        CinaVaultPanel.copy(alpha = 0.94f),
                    ),
                ),
                shape,
            )
            .clip(shape)
            .clickable(onClick = onOpen),
    ) {
        Box {
            RemoteArtwork(
                absoluteUrl = artworkUrl,
                token = sessionToken,
                title = item.title,
                refreshEpochMillis = refreshEpochMillis,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f),
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Transparent, Color.Black.copy(alpha = 0.74f)),
                        ),
                    ),
            )
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(7.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (item.favorite) {
                    Icon(
                        Icons.Rounded.Favorite,
                        contentDescription = "Favorite",
                        tint = CinaVaultSolar,
                        modifier = Modifier.size(15.dp),
                    )
                }
                if (item.verified) {
                    Icon(
                        Icons.Rounded.Verified,
                        contentDescription = "Verified metadata",
                        tint = CinaVaultEmerald,
                        modifier = Modifier.size(15.dp),
                    )
                }
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .background(CinaVaultCyan, RoundedCornerShape(50))
                    .padding(5.dp),
            ) {
                Icon(
                    Icons.Rounded.PlayArrow,
                    contentDescription = "Play",
                    tint = Color(0xFF02040A),
                    modifier = Modifier.size(16.dp),
                )
            }
        }

        Column(Modifier.padding(horizontal = 9.dp, vertical = 8.dp)) {
            Text(
                text = item.title,
                color = CinaVaultText,
                fontSize = 12.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(5.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                item.year?.let { value -> MetadataChip(value.toString(), CinaVaultCyan) }
                item.resolution?.let { value -> MetadataChip(value, CinaVaultMagenta) }
            }
            item.genre?.let { genre ->
                Text(
                    text = genre,
                    color = CinaVaultMuted,
                    fontSize = 9.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 5.dp),
                )
            }
        }
    }
}

@Composable
private fun MetadataChip(label: String, color: Color) {
    Text(
        text = label,
        color = color,
        fontSize = 8.sp,
        fontWeight = FontWeight.Black,
        modifier = Modifier
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(50))
            .border(0.5.dp, color.copy(alpha = 0.25f), RoundedCornerShape(50))
            .padding(horizontal = 5.dp, vertical = 2.dp),
        maxLines = 1,
    )
}
