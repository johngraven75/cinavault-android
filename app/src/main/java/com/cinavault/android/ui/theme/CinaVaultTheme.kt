package com.cinavault.android.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val CinaVaultInk = Color(0xFF02040A)
val CinaVaultPanel = Color(0xFF080D1C)
val CinaVaultPanelElevated = Color(0xFF101A35)
val CinaVaultCyan = Color(0xFF69F7FF)
val CinaVaultBlue = Color(0xFF4E7CFF)
val CinaVaultOrchid = Color(0xFFB85CFF)
val CinaVaultMagenta = Color(0xFFFF4FCF)
val CinaVaultSolar = Color(0xFFFFC857)
val CinaVaultEmerald = Color(0xFF62FFC2)
val CinaVaultText = Color(0xFFF7FAFF)
val CinaVaultMuted = Color(0xFF9AA8C5)

private val CinaVaultColorScheme = darkColorScheme(
    primary = CinaVaultCyan,
    onPrimary = CinaVaultInk,
    primaryContainer = CinaVaultBlue.copy(alpha = 0.28f),
    onPrimaryContainer = CinaVaultText,
    secondary = CinaVaultOrchid,
    onSecondary = CinaVaultText,
    secondaryContainer = CinaVaultOrchid.copy(alpha = 0.25f),
    onSecondaryContainer = CinaVaultText,
    tertiary = CinaVaultMagenta,
    onTertiary = CinaVaultText,
    background = CinaVaultInk,
    onBackground = CinaVaultText,
    surface = CinaVaultPanel,
    onSurface = CinaVaultText,
    surfaceVariant = CinaVaultPanelElevated,
    onSurfaceVariant = CinaVaultMuted,
    outline = CinaVaultCyan.copy(alpha = 0.28f),
    error = Color(0xFFFF6D88),
)

@Composable
fun CinaVaultTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CinaVaultColorScheme,
        typography = Typography(),
        content = content,
    )
}
