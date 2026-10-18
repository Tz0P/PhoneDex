package com.phonedex.app.ui.detail

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.phonedex.app.data.Sighting
import com.phonedex.app.domain.Fmt
import com.phonedex.app.domain.title
import com.phonedex.app.ui.components.BrandAvatar
import com.phonedex.app.ui.components.StatCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(sighting: Sighting?, onBack: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    var confirm by remember { mutableStateOf(false) }
    val ctx = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sighting") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back") } },
                actions = {
                    IconButton(onClick = onEdit) { Icon(Icons.Rounded.Edit, "Edit") }
                    IconButton(onClick = { confirm = true }) { Icon(Icons.Rounded.Delete, "Delete") }
                }
            )
        }
    ) { pad ->
        sighting?.let { s ->
            Column(
                Modifier.padding(pad).verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Card(
                    Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(Modifier.padding(22.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        BrandAvatar(s.brand, 64.dp)
                        Column {
                            Text(s.model, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Text(s.brand, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
                StatCard("Location") {
                    Text(s.location, style = MaterialTheme.typography.bodyLarge)
                    val lat = s.latitude
                    val lon = s.longitude
                    if (lat != null && lon != null) {
                        Text(
                            "%.5f, %.5f".format(lat, lon), style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        FilledTonalButton(onClick = {
                            val uri = Uri.parse("geo:$lat,$lon?q=$lat,$lon(${Uri.encode(s.title())})")
                            try {
                                ctx.startActivity(Intent(Intent.ACTION_VIEW, uri))
                            } catch (e: ActivityNotFoundException) {
                                Toast.makeText(ctx, "No maps app found", Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Icon(Icons.Rounded.Map, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Open in Maps")
                        }
                    }
                }
                StatCard("Spotted") {
                    Text(Fmt.date(s.timestamp), style = MaterialTheme.typography.bodyLarge)
                    Text(Fmt.time(s.timestamp), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (s.notes.isNotBlank()) {
                    StatCard("Notes") { Text(s.notes, style = MaterialTheme.typography.bodyLarge) }
                }
            }
        }
    }

    if (confirm) {
        AlertDialog(
            onDismissRequest = { confirm = false },
            title = { Text("Delete sighting?") },
            text = { Text("“${sighting?.title().orEmpty()}” will be permanently removed. This can't be undone.") },
            confirmButton = {
                TextButton(onClick = { confirm = false; onDelete() }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { confirm = false }) { Text("Keep") } }
        )
    }
}
