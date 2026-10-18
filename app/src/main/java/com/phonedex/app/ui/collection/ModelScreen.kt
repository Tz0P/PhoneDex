package com.phonedex.app.ui.collection

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.phonedex.app.data.Sighting
import com.phonedex.app.domain.*
import com.phonedex.app.ui.components.BrandAvatar
import com.phonedex.app.ui.components.SectionTitle
import com.phonedex.app.ui.components.openInMaps

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelScreen(modelKey: String, sightings: List<Sighting>?, onBack: () -> Unit, onOpenSighting: (Long) -> Unit) {
    val ctx = LocalContext.current
    val entry = remember(sightings, modelKey) { sightings.orEmpty().toModelEntries().firstOrNull { it.key == modelKey } }
    var newestFirst by rememberSaveable { mutableStateOf(false) }

    // If the last sighting of this model was edited/deleted, leave the empty screen.
    val gone = sightings != null && entry == null
    LaunchedEffect(gone) { if (gone) onBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(entry?.name ?: "", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back") } }
            )
        }
    ) { pad ->
        entry?.let { e ->
            val chronological = e.sightings.reversed()
            val numbers = remember(e) { chronological.mapIndexed { i, s -> s.id to i + 1 }.toMap() }
            val shown = if (newestFirst) e.sightings else chronological
            val located = e.sightings.firstOrNull { it.latitude != null && it.longitude != null }

            LazyColumn(
                Modifier.padding(pad),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Card(
                        Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.extraLarge,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                BrandAvatar(e.brand, 56.dp)
                                Column {
                                    Text(e.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    Text(e.brand, style = MaterialTheme.typography.titleSmall)
                                }
                            }
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text("${e.count}", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
                                Text(
                                    if (e.count == 1) " sighting" else " sightings",
                                    Modifier.padding(bottom = 10.dp), style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        DateTile("First spotted", e.firstSpotted, Modifier.weight(1f))
                        DateTile("Last spotted", e.lastSpotted, Modifier.weight(1f))
                    }
                }
                if (located != null) {
                    item {
                        FilledTonalButton(
                            onClick = { openInMaps(ctx, located.latitude!!, located.longitude!!, e.name) },
                            Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Rounded.Map, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Open latest location in Maps")
                        }
                    }
                }
                item {
                    Row(Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        SectionTitle("History", Modifier.weight(1f))
                        FilterChip(
                            selected = newestFirst, onClick = { newestFirst = !newestFirst },
                            label = { Text("Newest first") }
                        )
                    }
                }
                items(shown, key = { it.id }) { s ->
                    SightingRow(s, numbers[s.id] ?: 0, e.name, onClick = { onOpenSighting(s.id) }, modifier = Modifier.animateItem())
                }
                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    }
}

@Composable
private fun DateTile(label: String, ts: Long, modifier: Modifier) {
    Card(
        modifier, shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(Fmt.date(ts), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun SightingRow(s: Sighting, number: Int, label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    val ctx = LocalContext.current
    val lat = s.latitude
    val lon = s.longitude
    Card(
        onClick = onClick, modifier = modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = cs.surfaceContainerLow)
    ) {
        Row(Modifier.padding(start = 16.dp, top = 14.dp, bottom = 14.dp, end = 8.dp), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "#$number · ${Fmt.date(s.timestamp)} · ${Fmt.time(s.timestamp)}",
                    style = MaterialTheme.typography.labelLarge, color = cs.primary
                )
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Rounded.LocationOn, null, Modifier.size(16.dp).padding(top = 2.dp), tint = cs.onSurfaceVariant)
                    Spacer(Modifier.width(6.dp))
                    Text(s.location, style = MaterialTheme.typography.bodyMedium)
                }
                if (s.notes.isNotBlank()) {
                    Text(s.notes, style = MaterialTheme.typography.bodySmall, fontStyle = FontStyle.Italic, color = cs.onSurfaceVariant)
                }
            }
            if (lat != null && lon != null) {
                IconButton(onClick = { openInMaps(ctx, lat, lon, label) }) { Icon(Icons.Rounded.Map, "Open in Maps") }
            }
        }
    }
}
