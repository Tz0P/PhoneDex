package com.phonedex.app.ui.map

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.phonedex.app.data.Sighting
import com.phonedex.app.domain.Fmt
import com.phonedex.app.ui.components.BrandAvatar
import com.phonedex.app.ui.components.BrandLogos
import com.phonedex.app.ui.components.brandAccent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.TilesOverlay

@Composable
fun MapScreen(sightings: List<Sighting>?, onOpenDetail: (Long) -> Unit, onOpenHistory: () -> Unit) {
    val ctx = LocalContext.current
    val dark = isSystemInDarkTheme()
    val cs = MaterialTheme.colorScheme

    val all = sightings.orEmpty()
    // Only sightings with saved coordinates are shown; nothing is ever guessed.
    val located = remember(all) { all.filter { it.latitude != null && it.longitude != null } }
    val pts = remember(located) {
        located.map { SightingsOverlay.Pt(it.id, GeoPoint(it.latitude!!, it.longitude!!), it.brand) }
    }
    val byId = remember(located) { located.associateBy { it.id } }
    val missing = all.size - located.size
    var selectedIds by remember { mutableStateOf<List<Long>>(emptyList()) }

    val overlay = remember { SightingsOverlay() }
    val mapView = remember {
        MapView(ctx).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            setMinZoomLevel(2.0)
            setMaxZoomLevel(19.0)
            controller.setZoom(3.0)
            controller.setCenter(GeoPoint(30.0, 10.0))
            overlays.add(overlay)
            overlays.add(CopyrightOverlay(ctx))
        }
    }

    fun fitAll(animate: Boolean) {
        when {
            pts.isEmpty() -> Unit
            pts.size == 1 -> {
                mapView.controller.setZoom(15.0)
                mapView.controller.setCenter(pts[0].geo)
            }
            else -> {
                val box = BoundingBox.fromGeoPoints(pts.map { it.geo })
                if (box.latitudeSpan < 1e-4 && box.longitudeSpan < 1e-4) {
                    mapView.controller.setZoom(16.0)
                    mapView.controller.setCenter(box.centerWithDateLine)
                } else {
                    mapView.post { mapView.zoomToBoundingBox(box.increaseByScale(1.3f), animate, 80) }
                }
            }
        }
    }

    // Map engine lifecycle (no location updates are ever requested).
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, mapView) {
        val obs = LifecycleEventObserver { _, e ->
            when (e) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> Unit
            }
        }
        lifecycle.addObserver(obs)
        onDispose {
            lifecycle.removeObserver(obs)
            mapView.onPause()
            mapView.onDetach()
        }
    }

    LaunchedEffect(located.isNotEmpty()) { if (located.isNotEmpty()) fitAll(false) }

    val primary = cs.primary.toArgb()
    val onPrimary = cs.onPrimary.toArgb()
    SideEffect {
        overlay.onSelect = { selectedIds = it }
        overlay.onEmptyTap = { selectedIds = emptyList() }
    }

    BackHandler(enabled = selectedIds.isNotEmpty()) { selectedIds = emptyList() }

    Box(Modifier.fillMaxSize()) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize(),
            update = { mv ->
                overlay.points = pts
                overlay.selected = selectedIds.toSet()
                overlay.primaryColor = primary
                overlay.onPrimaryColor = onPrimary
                overlay.accentFor = { brandAccent(it).toArgb() }
                overlay.logoFor = { BrandLogos.bitmap(ctx, it) }
                mv.overlayManager.tilesOverlay.setColorFilter(if (dark) TilesOverlay.INVERT_COLORS else null)
                mv.invalidate()
            }
        )

        // Top bar: count + way back to the full history list.
        Column(
            Modifier.fillMaxWidth().statusBarsPadding().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = cs.surfaceContainerHigh, shadowElevation = 3.dp) {
                    Text(
                        "${located.size} on map", Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(Modifier.weight(1f))
                FilledTonalButton(onClick = onOpenHistory) {
                    Icon(Icons.Rounded.History, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("All sightings")
                }
            }
            if (missing > 0) {
                Surface(shape = CircleShape, color = cs.surfaceContainerHigh, shadowElevation = 3.dp) {
                    Text(
                        "$missing without location (see History)",
                        Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant
                    )
                }
            }
        }

        // Zoom controls.
        Column(
            Modifier.align(Alignment.CenterEnd).padding(end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SmallFloatingActionButton(onClick = { mapView.controller.zoomIn() }) { Icon(Icons.Rounded.Add, "Zoom in") }
            SmallFloatingActionButton(onClick = { mapView.controller.zoomOut() }) { Icon(Icons.Rounded.Remove, "Zoom out") }
            SmallFloatingActionButton(onClick = { fitAll(true) }) { Icon(Icons.Rounded.ZoomOutMap, "Show all sightings") }
        }

        if (located.isEmpty() && sightings != null) {
            Card(
                Modifier.align(Alignment.Center).padding(32.dp), shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = cs.surfaceContainerHigh)
            ) {
                Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Rounded.Map, null, Modifier.size(36.dp), tint = cs.primary)
                    Text("Nothing to show yet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        "Sightings saved with a GPS location appear here.",
                        style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant, textAlign = TextAlign.Center
                    )
                }
            }
        }

        val selection = selectedIds.mapNotNull { byId[it] }
        AnimatedVisibility(
            visible = selection.isNotEmpty(),
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically { it } + fadeIn(), exit = slideOutVertically { it } + fadeOut()
        ) {
            SelectionPanel(selection, onClose = { selectedIds = emptyList() }, onOpen = onOpenDetail, Modifier.padding(12.dp))
        }
    }
}

@Composable
private fun SelectionPanel(items: List<Sighting>, onClose: () -> Unit, onOpen: (Long) -> Unit, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    Card(
        modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = cs.surfaceContainerHigh),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (items.size == 1) {
                val s = items[0]
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    BrandAvatar(s.brand)
                    Column(Modifier.weight(1f)) {
                        Text(s.model, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(s.brand, style = MaterialTheme.typography.labelLarge, color = cs.primary)
                        Text(Fmt.dateTime(s.timestamp), style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                        Text(s.location, style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    IconButton(onClick = onClose) { Icon(Icons.Rounded.Close, "Close") }
                }
                Button(onClick = { onOpen(s.id) }, Modifier.fillMaxWidth()) { Text("Open details") }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${items.size} sightings here", Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold
                    )
                    IconButton(onClick = onClose) { Icon(Icons.Rounded.Close, "Close") }
                }
                Column(Modifier.heightIn(max = 240.dp).verticalScroll(rememberScrollState())) {
                    items.sortedByDescending { it.timestamp }.forEach { s ->
                        Row(
                            Modifier.fillMaxWidth().clickable { onOpen(s.id) }.padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            BrandAvatar(s.brand, 36.dp)
                            Column(Modifier.weight(1f)) {
                                Text("${s.brand} · ${s.model}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(Fmt.dateTime(s.timestamp), style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}
