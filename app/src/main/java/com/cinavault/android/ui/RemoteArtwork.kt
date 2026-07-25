package com.cinavault.android.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.cinavault.android.ui.theme.CinaVaultBlue
import com.cinavault.android.ui.theme.CinaVaultCyan
import com.cinavault.android.ui.theme.CinaVaultMagenta
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import javax.net.ssl.HttpsURLConnection

@Composable
fun RemoteArtwork(
    absoluteUrl: String?,
    token: String?,
    title: String,
    modifier: Modifier = Modifier,
) {
    val bitmap by produceState<android.graphics.Bitmap?>(
        initialValue = null,
        key1 = absoluteUrl,
        key2 = token,
    ) {
        value = if (absoluteUrl.isNullOrBlank() || token.isNullOrBlank()) {
            null
        } else {
            withContext(Dispatchers.IO) {
                runCatching {
                    val url = URL(absoluteUrl)
                    require(url.protocol == "https")
                    val connection = url.openConnection() as HttpsURLConnection
                    connection.connectTimeout = 12_000
                    connection.readTimeout = 20_000
                    connection.useCaches = false
                    connection.setRequestProperty("Authorization", "Bearer $token")
                    connection.setRequestProperty("Cache-Control", "no-store")
                    connection.inputStream.use(BitmapFactory::decodeStream)
                }.getOrNull()
            }
        }
    }

    Box(
        modifier = modifier.background(
            Brush.linearGradient(
                listOf(
                    CinaVaultBlue.copy(alpha = 0.55f),
                    CinaVaultMagenta.copy(alpha = 0.42f),
                    Color(0xFF071321),
                ),
            ),
        ),
        contentAlignment = Alignment.Center,
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = "$title poster",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Icon(
                imageVector = Icons.Rounded.Movie,
                contentDescription = null,
                tint = CinaVaultCyan.copy(alpha = 0.8f),
                modifier = Modifier.fillMaxSize(0.28f),
            )
        }
    }
}
