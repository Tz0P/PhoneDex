package com.phonedex.app.domain

private val notAlphanumeric = Regex("[^a-z0-9]+")

/**
 * Drawable resource name for a brand's logo: the lowercase brand name, restricted to what Android
 * allows in resource names (a-z, 0-9, underscore).
 * "Samsung" -> "samsung", " REDMI " -> "redmi", "Black Shark" -> "black_shark".
 * Capitalisation and stray whitespace in the stored brand never matter.
 */
fun logoResourceName(brand: String): String =
    cleanText(brand).lowercase().replace(notAlphanumeric, "_").trim('_')
