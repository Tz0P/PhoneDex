package com.phonedex.app.ui.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.phonedex.app.data.Sighting
import com.phonedex.app.domain.Fmt
import com.phonedex.app.domain.TimeFilter
import com.phonedex.app.domain.topBrands
import com.phonedex.app.ui.components.EmptyState
import com.phonedex.app.ui.components.SightingCard

@Composable
fun HistoryScreen(sightings: List<Sighting>?, onOpen: (Long) -> Unit) {
    val all = sightings.orEmpty()
    var query by rememberSaveable { mutableStateOf("") }
    var brand by rememberSaveable { mutableStateOf<String?>(null) }
    var range by rememberSaveable { mutableStateOf(TimeFilter.ANY) }

    val filtered = remember(all, query, brand, range) {
        val q = query.trim().lowercase()
        all.filter { s ->
            (brand == null || s.brand.equals(brand, ignoreCase = true)) &&
                range.matches(s.timestamp) &&
                (q.isEmpty() || listOf(s.brand, s.model, s.location, s.notes, Fmt.date(s.timestamp))
                    .any { it.lowercase().contains(q) })
        }
    }
    val brands = remember(all) { all.topBrands().map { it.label } }

    Column(Modifier.fillMaxSize().statusBarsPadding(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "History", Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
            style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold
        )
        OutlinedTextField(
            value = query, onValueChange = { query = it },
            placeholder = { Text("Search brand, model, place or date") },
            leadingIcon = { Icon(Icons.Rounded.Search, null) },
            trailingIcon = {
                if (query.isNotEmpty()) IconButton(onClick = { query = "" }) { Icon(Icons.Rounded.Close, "Clear") }
            },
            singleLine = true, shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        )
        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(TimeFilter.entries) { f ->
                FilterChip(selected = range == f, onClick = { range = f }, label = { Text(f.label) })
            }
        }
        if (brands.isNotEmpty()) {
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item { FilterChip(selected = brand == null, onClick = { brand = null }, label = { Text("All brands") }) }
                items(brands) { b ->
                    FilterChip(selected = brand.equals(b, true), onClick = { brand = b }, label = { Text(b) })
                }
            }
        }
        Text(
            "${filtered.size} of ${all.size} sightings", Modifier.padding(horizontal = 16.dp),
            style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (filtered.isEmpty()) {
                item {
                    EmptyState(
                        Icons.Rounded.Search,
                        if (all.isEmpty()) "Nothing logged yet" else "No matches",
                        if (all.isEmpty()) "Your sightings will show up here." else "Try a different search or clear the filters."
                    )
                }
            }
            items(filtered, key = { it.id }) { s ->
                SightingCard(s, onClick = { onOpen(s.id) }, modifier = Modifier.animateItem())
            }
        }
    }
}
