package com.cinavault.android.ui

import android.app.Activity
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
import androidx.compose.runtime.remember
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

@Composable
fun CinaVaultRecoveryHost(
    onRecoverToLibrary: () -> Unit,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    var retainedDiagnostic by rememberSaveable { mutableStateOf<String?>(null) }
    var caughtFailure: Exception? by remember { mutableStateOf(null) }

    if (retainedDiagnostic == null) {
        try {
            content()
        } catch (error: Exception) {
            caughtFailure = error
            retainedDiagnostic = diagnosticId(error)
        }
    }

    val diagnostic = retainedDiagnostic
    if (diagnostic != null) {
        RecoveryScreen(
            diagnostic = diagnostic,
            onRecoverToLibrary = {
                caughtFailure = null
                retainedDiagnostic = null
                onRecoverToLibrary()
            },
            onRestart = { (context as? Activity)?.recreate() },
        )
    }
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
                text = "The interface recovered from a rendering failure",
                color = CinaVaultText,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
            )
            Text(
                text = "Account data, encrypted session material, and library records were not cleared. Return safely to the Library or restart the interface.",
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

private fun diagnosticId(error: Exception): String {
    val fingerprint = "${error::class.java.name}:${error.message.orEmpty()}".hashCode()
        .toUInt()
        .toString(16)
        .uppercase()
        .padStart(8, '0')
    return "CV-ANDROID-$fingerprint"
}
