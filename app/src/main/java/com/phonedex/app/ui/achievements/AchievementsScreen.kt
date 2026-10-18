package com.phonedex.app.ui.achievements

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.phonedex.app.data.Sighting
import com.phonedex.app.data.UnlockedAchievement
import com.phonedex.app.domain.*
import com.phonedex.app.ui.components.BarRow
import com.phonedex.app.ui.components.SectionTitle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(sightings: List<Sighting>?, unlocked: List<UnlockedAchievement>, onBack: () -> Unit) {
    val progress = remember(sightings) { Achievements.evaluate(sightings.orEmpty()) }
    val dates = remember(unlocked) { unlocked.associate { it.id to it.unlockedAt } }
    val done = progress.count { it.unlocked }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Achievements") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back") } }
            )
        }
    ) { pad ->
        LazyColumn(
            Modifier.padding(pad),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("$done", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
                            Text(
                                " / ${progress.size} unlocked", Modifier.padding(bottom = 10.dp),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        BarRow("Overall progress", done, progress.size, MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
            Category.entries.forEach { cat ->
                val list = progress.filter { it.def.category == cat }
                if (list.isNotEmpty()) {
                    item(key = "header-${cat.name}") { SectionTitle(cat.label, Modifier.padding(top = 8.dp)) }
                    items(list, key = { it.def.id }) { p ->
                        AchievementCard(p, dates[p.def.id], Modifier.animateItem())
                    }
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun AchievementCard(p: AchievementProgress, unlockedAt: Long?, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    Card(
        modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = cs.surfaceContainerLow)
    ) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.Top) {
            Box(
                Modifier.size(48.dp).clip(CircleShape)
                    .background(if (p.unlocked) cs.primary else cs.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (p.unlocked) p.def.icon else Icons.Rounded.Lock, null,
                    Modifier.size(24.dp),
                    tint = if (p.unlocked) cs.onPrimary else cs.onSurfaceVariant
                )
            }
            Column(Modifier.weight(1f).alpha(if (p.unlocked) 1f else 0.85f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(p.def.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(p.def.description, style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                if (p.unlocked) {
                    Text(
                        if (unlockedAt != null) "Unlocked · ${Fmt.date(unlockedAt)}" else "Unlocked",
                        style = MaterialTheme.typography.labelMedium, color = cs.primary
                    )
                } else {
                    Spacer(Modifier.height(4.dp))
                    ProgressBar(p.fraction)
                    Text(
                        "${p.shownValue} / ${p.def.target}",
                        style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant
                    )
                }
            }
            if (p.unlocked) Icon(Icons.Rounded.CheckCircle, "Unlocked", tint = cs.primary)
        }
    }
}

@Composable
private fun ProgressBar(fraction: Float) {
    var go by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { go = true }
    val f by animateFloatAsState(if (go) fraction else 0f, tween(700), label = "progress")
    val color = MaterialTheme.colorScheme.primary
    Box(Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(50)).background(color.copy(alpha = 0.18f))) {
        Box(Modifier.fillMaxHeight().fillMaxWidth(f).clip(RoundedCornerShape(50)).background(color))
    }
}
