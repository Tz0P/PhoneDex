package com.phonedex.app.ui.edit

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.text.format.DateFormat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.phonedex.app.data.Brands
import com.phonedex.app.data.Sighting
import com.phonedex.app.domain.Fmt
import com.phonedex.app.domain.topBrands
import com.phonedex.app.location.LocationHelper
import com.phonedex.app.ui.MainViewModel
import com.phonedex.app.ui.components.BrandAvatar
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

private enum class LocStatus { IDLE, DETECTING, DONE, DONE_NO_ADDRESS, FAILED, DENIED }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditScreen(id: Long, vm: MainViewModel, onClose: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val focus = LocalFocusManager.current
    val sightings by vm.sightings.collectAsStateWithLifecycle()
    val custom by vm.customBrands.collectAsStateWithLifecycle()

    var brand by rememberSaveable { mutableStateOf("") }
    var model by rememberSaveable { mutableStateOf("") }
    var location by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }
    var lat by rememberSaveable { mutableStateOf<Double?>(null) }
    var lon by rememberSaveable { mutableStateOf<Double?>(null) }
    var timestamp by rememberSaveable { mutableStateOf(System.currentTimeMillis()) }
    var locationEdited by rememberSaveable { mutableStateOf(false) }
    var submitted by rememberSaveable { mutableStateOf(false) }
    var loaded by rememberSaveable { mutableStateOf(id == 0L) }
    var status by remember { mutableStateOf(LocStatus.IDLE) }

    // Load existing sighting when editing.
    LaunchedEffect(id) {
        if (!loaded) {
            vm.get(id)?.let {
                brand = it.brand; model = it.model; location = it.location; notes = it.notes
                lat = it.latitude; lon = it.longitude; timestamp = it.timestamp
                locationEdited = true
            }
            loaded = true
        }
    }

    fun detect() {
        if (status == LocStatus.DETECTING) return
        scope.launch {
            status = LocStatus.DETECTING
            val loc = LocationHelper.currentLocation(ctx)
            if (loc == null) {
                status = LocStatus.FAILED
                return@launch
            }
            lat = loc.latitude
            lon = loc.longitude
            val addr = LocationHelper.reverseGeocode(ctx, loc.latitude, loc.longitude)
            if (!locationEdited) location = addr ?: "%.5f, %.5f".format(loc.latitude, loc.longitude)
            status = if (addr != null) LocStatus.DONE else LocStatus.DONE_NO_ADDRESS
        }
    }

    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { res ->
        if (res.values.any { it }) detect() else status = LocStatus.DENIED
    }

    fun requestLocation() {
        if (LocationHelper.hasPermission(ctx)) detect()
        else permLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }

    // Auto-detect only for new sightings (permission is requested right when it's needed).
    LaunchedEffect(id) { if (id == 0L && lat == null) requestLocation() }

    val allBrands = remember(sightings, custom) {
        val used = sightings.orEmpty().topBrands().map { it.label }
        (used + (Brands.ALL + custom).sortedBy { it.lowercase() }).distinctBy { it.lowercase() }
    }
    val suggestions = remember(sightings, brand, model) {
        val m = model.trim()
        if (brand.isBlank()) emptyList() else
            sightings.orEmpty().filter { it.brand.equals(brand.trim(), true) }
                .groupBy { it.model.trim().lowercase() }.values
                .sortedByDescending { it.size }.map { it.first().model }
                .filter { !it.equals(m, true) && it.contains(m, true) }.take(6)
    }

    val brandError = if (submitted && brand.isBlank()) "Choose or add a brand" else null
    val modelError = if (submitted && model.isBlank()) "Enter the model name" else null

    fun save() {
        submitted = true
        if (brand.isBlank() || model.isBlank()) return
        val canonical = (Brands.ALL + custom).firstOrNull { it.equals(brand.trim(), true) } ?: brand.trim()
        vm.save(
            Sighting(
                id = id, brand = canonical, model = model.trim(),
                location = location.trim().ifBlank { "Unknown location" },
                latitude = lat, longitude = lon, timestamp = timestamp, notes = notes.trim()
            ),
            onClose
        )
    }

    val zdt = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault())
    val statusText = when (status) {
        LocStatus.IDLE -> null
        LocStatus.DETECTING -> "Finding your location…"
        LocStatus.DONE -> "Detected automatically. Edit it if it's off."
        LocStatus.DONE_NO_ADDRESS -> "Got coordinates but no address (offline?). Add a place name."
        LocStatus.FAILED -> "Couldn't get a GPS fix. Type the location or tap retry."
        LocStatus.DENIED -> "Location permission is off. Type the location instead."
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (id == 0L) "New sighting" else "Edit sighting") },
                navigationIcon = { IconButton(onClick = onClose) { Icon(Icons.Rounded.Close, "Cancel") } }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Row(
                    Modifier.fillMaxWidth().navigationBarsPadding().imePadding().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(onClick = onClose, Modifier.weight(1f).height(52.dp)) { Text("Cancel") }
                    Button(onClick = { save() }, Modifier.weight(2f).height(52.dp)) { Text("Save") }
                }
            }
        }
    ) { pad ->
        Column(
            Modifier.padding(pad).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = location,
                onValueChange = { location = it; locationEdited = true },
                label = { Text("Location") },
                leadingIcon = { Icon(Icons.Rounded.LocationOn, null) },
                trailingIcon = {
                    if (status == LocStatus.DETECTING) {
                        CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                    } else {
                        IconButton(onClick = { locationEdited = false; requestLocation() }) {
                            Icon(Icons.Rounded.MyLocation, "Detect location")
                        }
                    }
                },
                supportingText = { if (statusText != null) Text(statusText) },
                maxLines = 3, shape = MaterialTheme.shapes.large,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )

            BrandField(
                value = brand, onValueChange = { brand = it }, brands = allBrands,
                error = brandError, onPicked = { focus.moveFocus(FocusDirection.Down) }
            )

            OutlinedTextField(
                value = model, onValueChange = { model = it },
                label = { Text("Model") }, placeholder = { Text("e.g. OnePlus 13, Galaxy S24 Ultra") },
                isError = modelError != null,
                supportingText = { if (modelError != null) Text(modelError) },
                singleLine = true, shape = MaterialTheme.shapes.large,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )
            AnimatedVisibility(suggestions.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    suggestions.forEach { m -> SuggestionChip(onClick = { model = m }, label = { Text(m) }) }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = {
                        DatePickerDialog(ctx, { _, y, m, d ->
                            timestamp = zdt.with(LocalDate.of(y, m + 1, d)).toInstant().toEpochMilli()
                        }, zdt.year, zdt.monthValue - 1, zdt.dayOfMonth).show()
                    },
                    modifier = Modifier.weight(1f).height(56.dp), shape = MaterialTheme.shapes.large
                ) {
                    Icon(Icons.Rounded.CalendarToday, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(Fmt.date(timestamp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                OutlinedButton(
                    onClick = {
                        TimePickerDialog(ctx, { _, h, min ->
                            timestamp = zdt.with(LocalTime.of(h, min)).toInstant().toEpochMilli()
                        }, zdt.hour, zdt.minute, DateFormat.is24HourFormat(ctx)).show()
                    },
                    modifier = Modifier.weight(1f).height(56.dp), shape = MaterialTheme.shapes.large
                ) {
                    Icon(Icons.Rounded.Schedule, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(Fmt.time(timestamp), maxLines = 1)
                }
            }

            OutlinedTextField(
                value = notes, onValueChange = { notes = it },
                label = { Text("Notes (optional)") }, minLines = 3,
                shape = MaterialTheme.shapes.large,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BrandField(
    value: String,
    onValueChange: (String) -> Unit,
    brands: List<String>,
    error: String?,
    onPicked: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val q = value.trim()
    val matches = remember(q, brands) {
        if (q.isEmpty()) brands
        else brands.filter { it.contains(q, ignoreCase = true) }.sortedByDescending { it.startsWith(q, true) }
    }
    val canAdd = q.isNotEmpty() && brands.none { it.equals(q, ignoreCase = true) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = value,
            onValueChange = { onValueChange(it); expanded = true },
            label = { Text("Brand") },
            placeholder = { Text("Search or add a brand") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            isError = error != null,
            supportingText = { if (error != null) Text(error) },
            singleLine = true, shape = MaterialTheme.shapes.large,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded && (matches.isNotEmpty() || canAdd),
            onDismissRequest = { expanded = false }
        ) {
            if (canAdd) {
                DropdownMenuItem(
                    text = { Text("Add “$q” as a new brand") },
                    leadingIcon = { Icon(Icons.Rounded.Add, null) },
                    onClick = { onValueChange(q); expanded = false; onPicked() }
                )
            }
            matches.forEach { b ->
                DropdownMenuItem(
                    text = { Text(b) },
                    leadingIcon = { BrandAvatar(b, 28.dp) },
                    onClick = { onValueChange(b); expanded = false; onPicked() }
                )
            }
        }
    }
}
