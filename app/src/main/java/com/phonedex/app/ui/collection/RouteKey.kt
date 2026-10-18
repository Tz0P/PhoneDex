package com.phonedex.app.ui.collection

import android.util.Base64

/** Makes arbitrary brand/model keys safe to use inside navigation routes (no slashes, spaces, etc.). */
object RouteKey {
    private const val FLAGS = Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING

    fun encode(key: String): String = Base64.encodeToString(key.toByteArray(Charsets.UTF_8), FLAGS)

    fun decode(route: String): String =
        try {
            String(Base64.decode(route, FLAGS), Charsets.UTF_8)
        } catch (e: IllegalArgumentException) {
            ""
        }
}
