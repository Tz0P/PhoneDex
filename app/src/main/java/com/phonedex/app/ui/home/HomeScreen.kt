package com.phonedex.app.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.phonedex.app.data.Sighting
import com.phonedex.app.domain.*
import com.phonedex.app.ui.components.BrandAvatar
import com.phonedex.app.ui.components.EmptyState
import com.phonedex.app.ui.components.SectionTitle

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    sightings: List<Sighting>?,
    onOpenBrand: (String) -> Unit,
    onOpenStats: () -> Unit,
    onOpenAchievements: () -> Unit
) {
    if (sightings == null) return
    val models = remember(sightings) { sightings.toModelEntries() }
    val brands = remember(models) { models.toBrandEntries() }

    LazyColumn(
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column {
                Text("PhoneDex", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
                Text(
                    "Phones spotted in the wild",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        item {
            val top = sightings.topBrands().take(4)
            val thisWeek = sightings.count { it.timestamp >= startOfDayMillis(6) }
            Card(
                onClick = onOpenStats, shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Column(Modifier.padding(22.dp)) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("${sightings.size}", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (sightings.size == 1) "sighting" else "sightings",
                            Modifier.padding(bottom = 10.dp), style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.weight(1f))
                        Column(horizontalAlignment = Alignment.End) {
                            Text("$thisWeek", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text("last 7 days", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    if (top.isNotEmpty()) {
                        Spacer(Modifier.height(16.dp))
                        Text("Most spotted", style = MaterialTheme.typography.labelMedium)
                        Spacer(Modifier.height(8.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            top.forEach {
                                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)) {
                                    Text(
                                        "${it.label} · ${it.count}",
                                        Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        item {
            val progress = remember(sightings) { Achievements.evaluate(sightings) }
            val done = progress.count { it.unlocked }
            val next = progress.filter { !it.unlocked }.maxByOrNull { it.fraction }
            Card(
                onClick = onOpenAchievements, shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Row(
                    Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Surface(Modifier.size(48.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.EmojiEvents, null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                    Column(Modifier.weight(1f)) {
                        Text("Achievements", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            "$done of ${progress.size} unlocked" + (next?.let { " · Next: ${it.def.title}" } ?: ""),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2
                        )
                    }
                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        if (sightings.isEmpty()) {
            item {
                EmptyState(
                    Icons.Rounded.PhoneAndroid, "No sightings yet",
                    "Spot an interesting phone? Tap + to log it in a few seconds."
                )
            }
        } else {
            item {
                Column(Modifier.padding(top = 4.dp)) {
                    SectionTitle("Your collection")
                    Text(
                        "${plural(brands.size, "brand")} · ${plural(models.size, "model")}",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            items(brands, key = { it.key }) { b ->
                BrandCard(b, onClick = { onOpenBrand(b.key) }, modifier = Modifier.animateItem())
            }
        }
    }
}

@Composable
private fun BrandCard(brand: BrandEntry, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    Card(
        onClick = onClick, modifier = modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = cs.surfaceContainerLow)
    ) {
        Row(
            Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            BrandAvatar(brand.brand, 52.dp)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(brand.brand, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    "${plural(brand.models, "model")} · ${plural(brand.sightings, "sighting")}",
                    style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant
                )
                Text(
                    "Last spotted ${Fmt.date(brand.lastSpotted)}",
                    style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant
                )
            }
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, tint = cs.onSurfaceVariant)
        }
    }
}
