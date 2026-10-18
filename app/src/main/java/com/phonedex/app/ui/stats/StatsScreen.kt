package com.phonedex.app.ui.stats

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.phonedex.app.data.Sighting
import com.phonedex.app.domain.*
import com.phonedex.app.ui.components.BarRow
import com.phonedex.app.ui.components.EmptyState
import com.phonedex.app.ui.components.StatCard
import com.phonedex.app.ui.components.brandAccent
import kotlin.math.roundToInt

@Composable
fun StatsScreen(sightings: List<Sighting>?) {
    val all = sightings.orEmpty()
    var mode by rememberSaveable { mutableStateOf(0) }

    Column(
        Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Statistics", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
        if (all.isEmpty()) {
            EmptyState(Icons.Rounded.BarChart, "No data yet", "Log a few sightings and your stats will appear here.")
            return@Column
        }

        val brands = remember(all) { all.topBrands() }
        val models = remember(all) { all.topModels() }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Tile("Sightings", all.size, Modifier.weight(1f))
            Tile("Brands", brands.size, Modifier.weight(1f))
            Tile("Models", models.size, Modifier.weight(1f))
        }

        StatCard("Most spotted brands") {
            brands.take(5).forEach { BarRow(it.label, it.count, brands.first().count, brandAccent(it.label)) }
        }
        StatCard("Most spotted models") {
            models.take(5).forEach { BarRow(it.label, it.count, models.first().count, MaterialTheme.colorScheme.primary) }
        }

        StatCard("Sightings over time") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = mode == 0, onClick = { mode = 0 }, label = { Text("30 days") })
                FilterChip(selected = mode == 1, onClick = { mode = 1 }, label = { Text("12 months") })
            }
            val data = remember(all, mode) { if (mode == 0) all.perDay(30) else all.perMonth(12) }
            Text(
                "${data.sumOf { it.count }} in this period",
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TimeChart(data)
        }

        StatCard("Breakdown by brand") {
            val total = all.size
            val slices = brands.take(6) + brands.drop(6).sumOf { it.count }.let {
                if (it > 0) listOf(CountItem("Other", it)) else emptyList()
            }
            val other = MaterialTheme.colorScheme.outline
            Row(Modifier.fillMaxWidth().height(14.dp).clip(CircleShape)) {
                slices.forEach {
                    Box(
                        Modifier.weight(it.count.toFloat()).fillMaxHeight()
                            .background(if (it.label == "Other") other else brandAccent(it.label))
                    )
                }
            }
            slices.forEach {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(10.dp).clip(CircleShape)
                            .background(if (it.label == "Other") other else brandAccent(it.label))
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(it.label, Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("${it.count} · ${(it.count * 100f / total).roundToInt()}%", fontWeight = FontWeight.Medium)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun Tile(label: String, value: Int, modifier: Modifier) {
    Card(
        modifier, shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$value", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun TimeChart(data: List<CountItem>) {
    val max = (data.maxOfOrNull { it.count } ?: 0).coerceAtLeast(1)
    var go by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { go = true }
    val color = MaterialTheme.colorScheme.primary
    Row(
        Modifier.fillMaxWidth().height(120.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { d ->
            val target = if (d.count == 0) 0.03f else (d.count / max.toFloat()).coerceAtLeast(0.08f)
            val h by animateFloatAsState(if (go) target else 0f, tween(600), label = "h")
            Box(
                Modifier.weight(1f).fillMaxHeight(h.coerceAtLeast(0.001f))
                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                    .background(if (d.count == 0) color.copy(alpha = 0.18f) else color)
            )
        }
    }
    Row(Modifier.fillMaxWidth()) {
        Text(data.first().label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.weight(1f))
        Text(data.last().label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
