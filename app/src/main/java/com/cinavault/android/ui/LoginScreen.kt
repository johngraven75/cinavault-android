package com.cinavault.android.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Login
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cinavault.android.ui.theme.CinaVaultCyan
import com.cinavault.android.ui.theme.CinaVaultMagenta
import com.cinavault.android.ui.theme.CinaVaultMuted
import com.cinavault.android.ui.theme.CinaVaultOrchid
import com.cinavault.android.ui.theme.CinaVaultPanel
import com.cinavault.android.ui.theme.CinaVaultText

@Composable
fun LoginScreen(
    loading: Boolean,
    statusMessage: String,
    onPasswordLogin: (String, String, String) -> Unit,
    onAccessKeyLogin: (String, String) -> Unit,
) {
    var endpoint by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var accessKey by remember { mutableStateOf("") }
    var useAccessKey by remember { mutableStateOf(false) }
    val cardScale by animateFloatAsState(
        targetValue = if (loading) 0.985f else 1f,
        label = "login-card-scale",
    )

    Box(Modifier.fillMaxSize()) {
        ExperienceBackdrop()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 34.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 560.dp)
                    .scale(cardScale)
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            listOf(
                                CinaVaultCyan.copy(alpha = 0.5f),
                                CinaVaultMagenta.copy(alpha = 0.32f),
                                Color.White.copy(alpha = 0.08f),
                            ),
                        ),
                        shape = RoundedCornerShape(30.dp),
                    )
                    .background(
                        brush = Brush.linearGradient(
                            listOf(
                                CinaVaultPanel.copy(alpha = 0.95f),
                                Color(0xFF110A28).copy(alpha = 0.9f),
                            ),
                        ),
                        shape = RoundedCornerShape(30.dp),
                    )
                    .padding(24.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(
                                Brush.linearGradient(listOf(CinaVaultCyan, CinaVaultOrchid)),
                                RoundedCornerShape(18.dp),
                            )
                            .padding(14.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Security,
                            contentDescription = null,
                            tint = Color(0xFF02040A),
                        )
                    }
                    Column(Modifier.padding(start = 14.dp)) {
                        Text(
                            text = "CINAVAULT",
                            color = CinaVaultCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 3.sp,
                        )
                        Text(
                            text = "Spatial Media OS",
                            color = CinaVaultText,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                        )
                        Text(
                            text = "v2 Build 2 · Android",
                            color = CinaVaultMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))
                Text(
                    text = "Connect through the encrypted HTTPS relay generated by the CinaVault desktop server. Local file paths are never exposed to this device.",
                    color = CinaVaultMuted,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                )
                Spacer(Modifier.height(18.dp))

                OutlinedTextField(
                    value = endpoint,
                    onValueChange = { endpoint = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Encrypted server URL") },
                    placeholder = { Text("https://your-server.example.com") },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Rounded.Lock, null) },
                )

                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TextButton(
                        onClick = { useAccessKey = false },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = if (!useAccessKey) CinaVaultCyan else CinaVaultMuted,
                        ),
                    ) {
                        Icon(Icons.Rounded.Login, null)
                        Text(" Account", fontWeight = FontWeight.Bold)
                    }
                    TextButton(
                        onClick = { useAccessKey = true },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = if (useAccessKey) CinaVaultMagenta else CinaVaultMuted,
                        ),
                    ) {
                        Icon(Icons.Rounded.Key, null)
                        Text(" Access key", fontWeight = FontWeight.Bold)
                    }
                }

                AnimatedVisibility(visible = !useAccessKey) {
                    Column {
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Account email") },
                            singleLine = true,
                        )
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Password") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                        )
                    }
                }

                AnimatedVisibility(visible = useAccessKey) {
                    OutlinedTextField(
                        value = accessKey,
                        onValueChange = { accessKey = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("CinaVault access key") },
                        placeholder = { Text("cvra_...") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                    )
                }

                Spacer(Modifier.height(18.dp))
                Button(
                    onClick = {
                        if (useAccessKey) {
                            onAccessKeyLogin(endpoint, accessKey)
                        } else {
                            onPasswordLogin(endpoint, email, password)
                        }
                    },
                    enabled = !loading && endpoint.startsWith("https://") &&
                        if (useAccessKey) accessKey.isNotBlank() else email.isNotBlank() && password.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CinaVaultCyan,
                        contentColor = Color(0xFF02040A),
                    ),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    if (loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(end = 10.dp),
                            color = Color(0xFF02040A),
                            strokeWidth = 2.dp,
                        )
                    }
                    Text(
                        text = if (loading) statusMessage else "Enter the Vault",
                        fontWeight = FontWeight.Black,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(
                text = "Account sessions are encrypted with Android Keystore and excluded from cloud backup.",
                color = CinaVaultMuted,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}
