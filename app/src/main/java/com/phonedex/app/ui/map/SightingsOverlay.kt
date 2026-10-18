package com.phonedex.app.ui.map

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.graphics.RectF
import android.view.MotionEvent
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay
import kotlin.math.floor
import kotlin.math.hypot

/**
 * Draws saved sightings as round markers and groups nearby ones into numbered clusters.
 * Clustering is recomputed on every draw from the current zoom, so it adapts as you pinch/zoom.
 */
class SightingsOverlay : Overlay() {

    data class Pt(val id: Long, val geo: GeoPoint, val brand: String)
    private class Cluster(var x: Float, var y: Float, val members: MutableList<Pt>)

    var points: List<Pt> = emptyList()
    var selected: Set<Long> = emptySet()
    var primaryColor: Int = Color.BLUE
    var onPrimaryColor: Int = Color.WHITE
    var accentFor: (String) -> Int = { primaryColor }
    /** Logo bitmap for a brand, or null to fall back to the initial. */
    var logoFor: (String) -> Bitmap? = { null }
    var onSelect: (List<Long>) -> Unit = {}
    var onEmptyTap: () -> Unit = {}

    private var clusters: List<Cluster> = emptyList()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }
    private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val logoRect = RectF()
    private val tmp = Point()

    override fun draw(canvas: Canvas, mapView: MapView, shadow: Boolean) {
        if (shadow) return
        val d = mapView.context.resources.displayMetrics.density
        val cell = 56f * d
        val pj = mapView.projection
        val w = mapView.width.toFloat()
        val h = mapView.height.toFloat()

        val cells = HashMap<Long, Cluster>()
        for (p in points) {
            pj.toPixels(p.geo, tmp)
            val x = tmp.x.toFloat()
            val y = tmp.y.toFloat()
            if (x < -cell || y < -cell || x > w + cell || y > h + cell) continue
            val cx = floor((x / cell).toDouble()).toLong()
            val cy = floor((y / cell).toDouble()).toLong()
            val c = cells.getOrPut((cx shl 32) xor (cy and 0xffffffffL)) { Cluster(0f, 0f, mutableListOf()) }
            c.members.add(p)
            c.x += x
            c.y += y
        }
        val built = cells.values.toList()
        built.forEach { it.x /= it.members.size; it.y /= it.members.size }
        clusters = built

        for (c in built) {
            val single = c.members.size == 1
            val isSelected = c.members.any { it.id in selected }
            val base = if (single) 15f else 19f
            val r = (if (isSelected) base + 4f else base) * d

            paint.style = Paint.Style.FILL
            paint.color = if (isSelected) primaryColor else Color.WHITE
            canvas.drawCircle(c.x, c.y, r + 3f * d, paint)

            val logo = if (single) logoFor(c.members[0].brand) else null
            if (logo != null) {
                // Brand logo on a light disc, scaled to fit without distortion.
                paint.color = LOGO_BACKGROUND
                canvas.drawCircle(c.x, c.y, r, paint)
                val box = r * 1.36f
                val scale = minOf(box / logo.width, box / logo.height)
                val lw = logo.width * scale
                val lh = logo.height * scale
                logoRect.set(c.x - lw / 2f, c.y - lh / 2f, c.x + lw / 2f, c.y + lh / 2f)
                canvas.drawBitmap(logo, null, logoRect, bitmapPaint)
            } else {
                paint.color = if (single) accentFor(c.members[0].brand) else primaryColor
                canvas.drawCircle(c.x, c.y, r, paint)

                textPaint.color = if (single) Color.WHITE else onPrimaryColor
                textPaint.textSize = (if (single) 14f else 13f) * d
                val label = if (single) c.members[0].brand.trim().firstOrNull()?.uppercase() ?: "?" else c.members.size.toString()
                canvas.drawText(label, c.x, c.y - (textPaint.ascent() + textPaint.descent()) / 2f, textPaint)
            }
        }
    }

    override fun onSingleTapConfirmed(e: MotionEvent, mapView: MapView): Boolean {
        val d = mapView.context.resources.displayMetrics.density
        val hit = clusters
            .map { it to hypot(it.x - e.x, it.y - e.y) }
            .minByOrNull { it.second }
            ?.takeIf { it.second <= 30f * d }
            ?.first
        if (hit == null) {
            onEmptyTap()
            return false
        }
        if (hit.members.size == 1) {
            onSelect(listOf(hit.members[0].id))
            return true
        }
        val box = BoundingBox.fromGeoPoints(hit.members.map { it.geo })
        val tiny = box.latitudeSpan < 1e-4 && box.longitudeSpan < 1e-4
        if (tiny || mapView.zoomLevelDouble >= 18.0) {
            // Same spot (or can't zoom further): list the sightings instead of zooming.
            onSelect(hit.members.map { it.id })
        } else {
            mapView.zoomToBoundingBox(box.increaseByScale(1.4f), true, (60 * d).toInt())
        }
        return true
    }

    private companion object {
        val LOGO_BACKGROUND: Int = Color.rgb(0xF5, 0xF5, 0xF7)
    }
}
