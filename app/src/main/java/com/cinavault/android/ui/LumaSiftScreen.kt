package com.cinavault.android.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.FolderDelete
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cinavault.android.R
import com.cinavault.android.data.LumaSiftCandidate
import com.cinavault.android.data.LumaSiftGroup
import com.cinavault.android.data.LumaSiftPlan
import com.cinavault.android.data.LumaSiftProgress
import com.cinavault.android.ui.theme.CinaVaultCyan
import com.cinavault.android.ui.theme.CinaVaultMagenta
import com.cinavault.android.ui.theme.CinaVaultMuted
import com.cinavault.android.ui.theme.CinaVaultOrchid
import com.cinavault.android.ui.theme.CinaVaultText
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun LumaSiftScreen(
    progress: LumaSiftProgress,
    plan: LumaSiftPlan?,
    runningAction: String?,
    onRefresh: () -> Unit,
    onStart: (List<String>) -> Unit,
    onApply: (String) -> Unit,
) {
    var confirmPlan by remember { mutableStateOf(false) }
    var selectedTypes by remember { mutableStateOf(setOf("video", "audio", "document", "image")) }
    LaunchedEffect(Unit) { onRefresh() }
    LaunchedEffect(progress.scanning) {
        while (progress.scanning && isActive) {
            delay(900)
            onRefresh()
        }
    }

    val queuedCount = plan?.groups.orEmpty().sumOf { group ->
        group.candidates.count { it.disposition == "queued_for_quarantine" }
    }
    val actionable = plan?.status == "ready_for_review" && queuedCount > 0
    val working = runningAction?.startsWith("lumasift") == true

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            LumaHero(
                scanning = progress.scanning,
                working = working,
                selectedTypes = selectedTypes,
                onStart = onStart,
                onRefresh = onRefresh,
            )
        }
        item {
            LumaTypeSelection(
                selectedTypes = selectedTypes,
                onToggle = { type ->
                    selectedTypes = if (selectedTypes.contains(type)) selectedTypes - type else selectedTypes + type
                },
            )
        }
        item { ProgressPanel(progress) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                LumaMetric(
                    modifier = Modifier.weight(1f),
                    label = "EXACT GROUPS",
                    value = plan?.groups?.size?.toString() ?: "0",
                    detail = "Complete content proof",
                    accent = CinaVaultCyan,
                )
                LumaMetric(
                    modifier = Modifier.weight(1f),
                    label = "RECOVERABLE",
                    value = formatBytes(plan?.reclaimableBytes ?: 0),
                    detail = "After quarantine",
                    accent = Color(0xFFFFD166),
                )
                LumaMetric(
                    modifier = Modifier.weight(1f),
                    label = "QUEUED",
                    value = queuedCount.toString(),
                    detail = "Awaiting approval",
                    accent = CinaVaultMagenta,
                )
            }
        }
        if (plan == null) {
            item { EmptyPlan() }
        } else if (plan.groups.isEmpty()) {
            item { NoDuplicates(plan.status) }
        } else {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0B1125), RoundedCornerShape(20.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.09f), RoundedCornerShape(20.dp))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("RESOLUTION PLAN", color = CinaVaultText, fontWeight = FontWeight.Black, fontSize = 14.sp)
                        Text("Names, score evidence, and final disposition stay visible before any file moves.", color = CinaVaultMuted, fontSize = 11.sp)
                    }
                    if (actionable) {
                        Button(
                            onClick = { confirmPlan = true },
                            enabled = !working,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD166), contentColor = Color(0xFF18110A)),
                        ) {
                            Icon(Icons.Rounded.FolderDelete, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.size(6.dp))
                            Text("QUARANTINE", fontWeight = FontWeight.Black, fontSize = 11.sp)
                        }
                    }
                }
            }
            items(plan.groups, key = { it.id }) { group -> GroupCard(group) }
        }
        if (plan != null && plan.dispositions.isNotEmpty()) {
            item { DispositionLog(plan) }
        }
        item { Spacer(Modifier.height(28.dp)) }
    }

    if (confirmPlan && plan != null) {
        AlertDialog(
            onDismissRequest = { if (!working) confirmPlan = false },
            containerColor = Color(0xFF12162A),
            titleContentColor = CinaVaultText,
            textContentColor = CinaVaultMuted,
            icon = { Icon(Icons.Rounded.Security, null, tint = Color(0xFFFFD166)) },
            title = { Text("Approve quarantine plan?") },
            text = {
                Text(
                    "$queuedCount lower-ranked exact duplicates will be revalidated and moved to the Windows host's LumaSift quarantine. They will not be permanently erased.",
                )
            },
            dismissButton = {
                TextButton(onClick = { confirmPlan = false }, enabled = !working) { Text("Keep reviewing") }
            },
            confirmButton = {
                Button(
                    onClick = { confirmPlan = false; onApply(plan.id) },
                    enabled = !working,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD166), contentColor = Color(0xFF18110A)),
                ) { Text(if (working) "Moving…" else "Move to quarantine", fontWeight = FontWeight.Black) }
            },
        )
    }
}

@Composable
private fun LumaHero(
    scanning: Boolean,
    working: Boolean,
    selectedTypes: Set<String>,
    onStart: (List<String>) -> Unit,
    onRefresh: () -> Unit,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF071A30), Color(0xFF2A1357), Color(0xFF4B0B51))))
                .border(1.dp, CinaVaultCyan.copy(alpha = 0.28f), RoundedCornerShape(26.dp))
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.24f), RoundedCornerShape(20.dp)),
            ) {
                androidx.compose.foundation.Image(
                    painter = painterResource(R.drawable.lumasift_prism),
                    contentDescription = "LumaSift prism logo",
                    modifier = Modifier.size(72.dp),
                    contentScale = ContentScale.Crop,
                )
            }
            Column(Modifier.weight(1f)) {
                Text("EXACT MEDIA RESOLUTION", color = Color(0xFFB4F1FF), fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.4.sp)
                Text("LumaSift", color = Color.White, fontWeight = FontWeight.Black, fontSize = 26.sp)
                Text("Keep the luminous best copy. Every candidate is proven before it is ranked.", color = Color(0xFFD2D5E5), fontSize = 11.sp)
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { onStart(selectedTypes.toList()) },
                enabled = !scanning && !working && selectedTypes.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = CinaVaultCyan, contentColor = Color(0xFF01121E)),
            ) {
                Icon(Icons.Rounded.AutoAwesome, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.size(6.dp))
                Text("BUILD EXACT PLAN", fontWeight = FontWeight.Black, fontSize = 11.sp)
            }
            OutlinedButton(onClick = onRefresh, enabled = !working) {
                Icon(Icons.Rounded.Refresh, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.size(5.dp))
                Text("REFRESH", fontWeight = FontWeight.Black, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun LumaTypeSelection(
    selectedTypes: Set<String>,
    onToggle: (String) -> Unit,
) {
    val types = listOf(
        Triple("video", "VIDEOS", "MP4, MKV, MOV and more"),
        Triple("audio", "MP3 AUDIO", "MP3 files only"),
        Triple("document", "DOCX + PDF", "Documents and ebooks"),
        Triple("image", "IMAGES", "JPG, PNG, HEIC and more"),
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF091225), RoundedCornerShape(20.dp))
            .border(1.dp, Color.White.copy(alpha = 0.09f), RoundedCornerShape(20.dp))
            .padding(15.dp),
    ) {
        Text("CHOOSE FILE TYPES", color = CinaVaultText, fontSize = 13.sp, fontWeight = FontWeight.Black)
        Text("LumaSift scans only the selected categories. Every match is still proven by complete content hashing.", color = CinaVaultMuted, fontSize = 10.sp)
        Spacer(Modifier.height(10.dp))
        types.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { (id, label, detail) ->
                    val selected = selectedTypes.contains(id)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onToggle(id) }
                            .background(if (selected) CinaVaultCyan.copy(alpha = 0.11f) else Color.Black.copy(alpha = 0.18f), RoundedCornerShape(14.dp))
                            .border(1.dp, if (selected) CinaVaultCyan.copy(alpha = 0.38f) else Color.White.copy(alpha = 0.10f), RoundedCornerShape(14.dp))
                            .padding(10.dp),
                    ) {
                        Text(label, color = if (selected) CinaVaultCyan else CinaVaultText, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 0.7.sp)
                        Text(detail, color = CinaVaultMuted, fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(if (selected) "SELECTED" else "NOT SELECTED", color = if (selected) CinaVaultCyan else CinaVaultMuted, fontSize = 8.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
        if (selectedTypes.isEmpty()) {
            Text("Choose at least one category to build an exact plan.", color = Color(0xFFFFD166), fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ProgressPanel(progress: LumaSiftProgress) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF091225), RoundedCornerShape(20.dp))
            .border(1.dp, Color.White.copy(alpha = 0.09f), RoundedCornerShape(20.dp))
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(progress.phase.uppercase(), color = CinaVaultText, fontWeight = FontWeight.Black, fontSize = 13.sp)
                Text(progress.message, color = CinaVaultMuted, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Text("${progress.percentage.coerceIn(0, 100)}%", color = CinaVaultCyan, fontSize = 22.sp, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.height(10.dp))
        LinearProgressIndicator(
            progress = { progress.percentage.coerceIn(0, 100) / 100f },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(99.dp)),
            color = CinaVaultCyan,
            trackColor = Color.White.copy(alpha = 0.10f),
        )
        Spacer(Modifier.height(10.dp))
        Text("${progress.current} / ${progress.total} processed · ${progress.filesConsidered} indexed media", color = CinaVaultMuted, fontSize = 10.sp)
        progress.currentDisplayName?.let { name ->
            Text("NOW: $name", color = Color(0xFFE8EAF7), fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        AnimatedVisibility(progress.error != null) {
            Text("${progress.error}", color = Color(0xFFFFB4AB), fontSize = 11.sp, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
private fun LumaMetric(modifier: Modifier, label: String, value: String, detail: String, accent: Color) {
    Column(
        modifier = modifier
            .background(Color(0xFF0A1022), RoundedCornerShape(17.dp))
            .border(1.dp, accent.copy(alpha = 0.22f), RoundedCornerShape(17.dp))
            .padding(12.dp),
    ) {
        Text(label, color = accent, fontSize = 8.sp, fontWeight = FontWeight.Black, letterSpacing = 0.9.sp)
        Text(value, color = CinaVaultText, fontSize = 16.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(detail, color = CinaVaultMuted, fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun EmptyPlan() {
    Column(
        modifier = Modifier.fillMaxWidth().background(Color(0xFF0A1022), RoundedCornerShape(22.dp)).padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(Icons.Rounded.AutoAwesome, null, tint = CinaVaultCyan.copy(alpha = 0.52f), modifier = Modifier.size(36.dp))
        Spacer(Modifier.height(8.dp))
        Text("Ready to resolve", color = CinaVaultText, fontWeight = FontWeight.Black)
        Text("Ask the connected Windows host to build a review-only exact-media plan.", color = CinaVaultMuted, fontSize = 11.sp)
    }
}

@Composable
private fun NoDuplicates(status: String) {
    Column(
        modifier = Modifier.fillMaxWidth().background(Color(0xFF0A1823), RoundedCornerShape(22.dp)).padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(Icons.Rounded.CheckCircle, null, tint = Color(0xFF6CFFB2), modifier = Modifier.size(36.dp))
        Spacer(Modifier.height(8.dp))
        Text("No exact duplicates in this plan", color = CinaVaultText, fontWeight = FontWeight.Black)
        Text(if (status == "cancelled") "The last LumaSift scan stopped safely." else "Only complete content matches can enter a cleanup plan.", color = CinaVaultMuted, fontSize = 11.sp)
    }
}

@Composable
private fun GroupCard(group: LumaSiftGroup) {
    Column(
        modifier = Modifier.fillMaxWidth().background(Color(0xFF0B1021), RoundedCornerShape(22.dp)).border(1.dp, Color.White.copy(alpha = 0.09f), RoundedCornerShape(22.dp)).padding(15.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("EXACT CONTENT GROUP", color = CinaVaultCyan, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                Text("${group.candidates.size} matching files", color = CinaVaultText, fontSize = 14.sp, fontWeight = FontWeight.Black)
            }
            Text("${formatBytes(group.reclaimableBytes)}", color = Color(0xFFFFD166), fontWeight = FontWeight.Black, fontSize = 13.sp)
        }
        Spacer(Modifier.height(9.dp))
        group.candidates.forEach { candidate -> CandidateRow(candidate, candidate.id == group.winnerId) }
    }
}

@Composable
private fun CandidateRow(candidate: LumaSiftCandidate, retained: Boolean) {
    val tint = if (retained) CinaVaultCyan else CinaVaultMagenta
    val icon = if (candidate.mediaKind == "image" || candidate.mediaKind == "photo") Icons.Rounded.Image else Icons.Rounded.VideoLibrary
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp)
            .background(if (retained) CinaVaultCyan.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.035f), RoundedCornerShape(15.dp))
            .border(1.dp, tint.copy(alpha = if (retained) 0.30f else 0.14f), RoundedCornerShape(15.dp))
            .padding(11.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(17.dp))
            Spacer(Modifier.size(8.dp))
            Column(Modifier.weight(1f)) {
                Text(candidate.displayName, color = CinaVaultText, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(candidate.disposition.replace('_', ' ').uppercase(), color = tint, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 0.7.sp)
            }
            Text(candidate.qualityScore.toString(), color = CinaVaultText, fontSize = 12.sp, fontWeight = FontWeight.Black)
        }
        if (candidate.quality.reasons.isNotEmpty()) {
            Text(candidate.quality.reasons.take(3).joinToString(" · "), color = CinaVaultMuted, fontSize = 10.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 5.dp))
        }
        Text(candidate.dispositionDetail, color = Color(0xFFD5D8E4), fontSize = 10.sp, modifier = Modifier.padding(top = 3.dp))
    }
}

@Composable
private fun DispositionLog(plan: LumaSiftPlan) {
    Column(
        modifier = Modifier.fillMaxWidth().background(Color(0xFF0A1022), RoundedCornerShape(20.dp)).border(1.dp, Color.White.copy(alpha = 0.09f), RoundedCornerShape(20.dp)).padding(15.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.ErrorOutline, null, tint = CinaVaultOrchid, modifier = Modifier.size(16.dp))
            Spacer(Modifier.size(7.dp))
            Text("FILES & DISPOSITIONS", color = CinaVaultText, fontWeight = FontWeight.Black, fontSize = 13.sp)
        }
        plan.dispositions.takeLast(12).reversed().forEach { event ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 9.dp).clickable(enabled = false) {},
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(event.disposition.replace('_', ' ').uppercase(), color = dispositionColor(event.disposition), fontSize = 8.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(0.40f))
                Column(Modifier.weight(0.60f)) {
                    Text(event.displayName, color = CinaVaultText, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(event.detail, color = CinaVaultMuted, fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

private fun dispositionColor(value: String): Color = when (value) {
    "retain" -> CinaVaultCyan
    "quarantined" -> CinaVaultOrchid
    "queued_for_quarantine" -> Color(0xFFFFD166)
    "failed" -> Color(0xFFFFB4AB)
    else -> Color(0xFFD4D8E8)
}

private fun formatBytes(value: Long): String {
    if (value <= 0) return "0 B"
    val units = listOf("B", "KB", "MB", "GB", "TB")
    val exponent = kotlin.math.min((kotlin.math.ln(value.toDouble()) / kotlin.math.ln(1024.0)).toInt(), units.lastIndex)
    val divisor = Math.pow(1024.0, exponent.toDouble())
    return "%.1f %s".format(value / divisor, units[exponent])
}
