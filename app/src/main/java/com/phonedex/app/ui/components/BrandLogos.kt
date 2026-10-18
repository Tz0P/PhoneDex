package com.phonedex.app.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.phonedex.app.domain.logoResourceName

/**
 * Single place that maps a brand name to its logo in res/drawable-nodpi/.
 *
 * Lookup is by resource *name* at runtime, so the app builds and runs whether or not a given
 * logo file exists: add or remove PNGs at any time, no code changes needed. A missing logo
 * simply returns 0 / null and callers fall back to the brand's initial.
 */
object BrandLogos {
    private val ids = HashMap<String, Int>()
    private val bitmaps = HashMap<Int, Bitmap?>()

    /** Drawable id of the brand's logo, or 0 when there is none. */
    @Synchronized
    fun resId(context: Context, brand: String): Int {
        val name = logoResourceName(brand)
        if (name.isEmpty()) return 0
        return ids.getOrPut(name) {
            context.resources.getIdentifier(name, "drawable", context.packageName)
        }
    }

    /** Decoded logo for canvas drawing (map markers), or null when there is none. */
    @Synchronized
    fun bitmap(context: Context, brand: String): Bitmap? {
        val id = resId(context, brand)
        if (id == 0) return null
        return bitmaps.getOrPut(id) { BitmapFactory.decodeResource(context.resources, id) }
    }
}
