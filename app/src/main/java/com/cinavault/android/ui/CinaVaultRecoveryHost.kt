package com.cinavault.android.ui

import android.app.Activity
import android.app.ActivityManager
import android.app.ApplicationExitInfo
import android.content.Context
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cinavault.android.ui.theme.CinaVaultCyan
import com.cinavault.android.ui.theme.CinaVaultMagenta
import com.cinavault.android.ui.theme.CinaVaultMuted
import com.cinavault.android.ui.theme.CinaVaultPanel
import com.cinavault.android.ui.theme.CinaVaultText

private const val RECOVERY_PREFERENCES = "cinavault_recovery"
private const val LAST_HANDLED_EXIT_KEY = "last_handled_exit_timestamp"

fun detectPreviousAbnormalExit(context: Context): String? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return null

    val activityManager = context.getSystemService(ActivityManager::class.java) ?: return null
    val exit = activityManager
        .getHistoricalProcessExitReasons(context.packageName, 0, 5)
        .firstOrNull { info ->
            info.reason == ApplicationExitInfo.REASON_CRASH ||
                info.reason == ApplicationExitInfo.REASON_CRASH_NATIVE ||
                info.reason == ApplicationExitInfo.REASON_ANR
        } ?: return null

    val preferences = context.getSharedPreferences(RECOVERY_PREFERENCES, Context.MODE_PRIVATE)
    val lastHandled = preferences.getLong(LAST_HANDLED_EXIT_KEY, 0L)
    if (exit.timestamp <= lastHandled) return null

    preferences.edit().putLong(LAST_HANDLED_EXIT_KEY, exit.timestamp).apply()
    val reason = when (exit.reason) {
        ApplicationExitInfo.REASON_ANR -> "ANR"
        ApplicationExitInfo.REASON_CRASH_NATIVE -> "NATIVE"
        else -> "CRASH"
    }
    return "CV-ANDROID-$reason-${exit.timestamp.toString(16).uppercase()}"
}

@Composable
fun CinaVaultRecoveryHost(
    initialDiagnostic: String?,
    onRecoverToLibrary: () -> Unit,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    var diagnostic by rememberSaveable { mutableStateOf(initialDiagnostic) }

    if (diagnostic == null) {
        content()
        return
    }

    RecoveryScreen(
        diagnostic = diagnostic.orEmpty(),
        onRecoverToLibrary = {
            diagnostic = null
            onRecoverToLibrary()
        },
        onRestart = { (context as? Activity)?.recreate() },
    )
}

@Composable
private fun RecoveryScreen(
    diagnostic: String,
    onRecoverToLibrary: () -> Unit,
    onRestart: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF02040A),
                        Color(0xFF08031A),
                        Color(0xFF031520),
                    ),
                ),
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CinaVaultPanel, RoundedCornerShape(26.dp))
                .border(1.dp, CinaVaultCyan.copy(alpha = 0.28f), RoundedCornerShape(26.dp))
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp),
        ) {
            Text(
                text = "CINAVAULT RECOVERY",
                color = CinaVaultCyan,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
            )
            Text(
                text = "The previous session ended unexpectedly",
                color = CinaVaultText,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
            )
            Text(
                text = "Account data, encrypted session material, settings, and library records were preserved. Continue safely to the Library or restart the interface.",
                color = CinaVaultMuted,
                fontSize = 13.sp,
            )
            Text(
                text = "Diagnostic: $diagnostic",
                color = CinaVaultMagenta,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onRecoverToLibrary,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CinaVaultCyan,
                        contentColor = Color(0xFF02040A),
                    ),
                    shape = RoundedCornerShape(15.dp),
                ) {
                    Text("Return to Library", fontWeight = FontWeight.Black)
                }
                TextButton(onClick = onRestart) {
                    Text("Restart interface", color = CinaVaultText)
                }
            }
        }
    }
}
