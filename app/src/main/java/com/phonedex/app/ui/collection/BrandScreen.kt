package com.phonedex.app.ui.collection

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.phonedex.app.data.Sighting
import com.phonedex.app.domain.*
import com.phonedex.app.ui.components.BrandAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrandScreen(brandKey: String, sightings: List<Sighting>?, onBack: () -> Unit, onOpenModel: (String) -> Unit) {
    val entries = remember(sightings, brandKey) {
        sightings.orEmpty().toModelEntries().filter { it.brandKey == brandKey }
    }
    var sort by rememberSaveable { mutableStateOf(ModelSort.COUNT) }
    val sorted = remember(entries, sort) { entries.sortModels(sort) }

    // If the last sighting of this brand was edited/deleted, leave the empty screen.
    val gone = sightings != null && entries.isEmpty()
    LaunchedEffect(gone) { if (gone) onBack() }

    val brand = entries.firstOrNull()?.brand.orEmpty()
    val total = entries.sumOf { it.count }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(brand) },
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
                    Row(
                        Modifier.fillMaxWidth().padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        BrandAvatar(brand, 56.dp)
                        Column {
                            Text(brand, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text(
                                "${plural(entries.size, "model")} · ${plural(total, "sighting")}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    ModelSort.entries.forEach { m ->
                        FilterChip(selected = sort == m, onClick = { sort = m }, label = { Text(m.label) })
                    }
                }
            }
            items(sorted, key = { it.key }) { e ->
                ModelCard(e, onClick = { onOpenModel(e.key) }, modifier = Modifier.animateItem())
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun ModelCard(entry: ModelEntry, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    Card(
        onClick = onClick, modifier = modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = cs.surfaceContainerLow)
    ) {
        Row(
            Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    entry.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold,
                    maxLines = 2, overflow = TextOverflow.Ellipsis
                )
                Text(
                    "Last spotted ${Fmt.date(entry.lastSpotted)}",
                    style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant
                )
            }
            Surface(shape = MaterialTheme.shapes.medium, color = cs.primaryContainer) {
                Column(
                    Modifier.widthIn(min = 64.dp).padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "${entry.count}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold,
                        color = cs.onPrimaryContainer
                    )
                    Text(
                        if (entry.count == 1) "sighting" else "sightings",
                        style = MaterialTheme.typography.labelSmall, color = cs.onPrimaryContainer, textAlign = TextAlign.Center
                    )
                }
            }
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, tint = cs.onSurfaceVariant)
        }
    }
}
