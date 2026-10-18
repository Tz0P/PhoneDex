package com.phonedex.app.domain

import com.phonedex.app.data.Sighting

/**
 * The collection is *derived* from the individual sightings, which stay untouched in the database.
 * Counters therefore can never drift: add / edit / delete a sighting and the grouping follows.
 */
data class ModelEntry(
    val key: String,
    val brandKey: String,
    val brand: String,
    /** Full display name, e.g. "Samsung Galaxy S25 Ultra". */
    val name: String,
    val count: Int,
    val firstSpotted: Long,
    val lastSpotted: Long,
    /** Newest first. */
    val sightings: List<Sighting>
)

data class BrandEntry(
    val key: String,
    val brand: String,
    val models: Int,
    val sightings: Int,
    val lastSpotted: Long
)

enum class ModelSort(val label: String) {
    COUNT("Most spotted"), NAME("A–Z"), RECENT("Recent")
}

private val whitespace = Regex("\\s+")

/** Trims and collapses repeated whitespace. */
fun cleanText(s: String): String = s.trim().replace(whitespace, " ")

fun brandKey(brand: String): String = cleanText(brand).lowercase()

/**
 * Normalised model name: only case, surrounding/repeated whitespace and a leading copy of the
 * brand name are ignored ("OnePlus 13" and "13" under OnePlus are the same phone).
 * Punctuation, digits and suffixes are kept, so "S25", "S25+" and "S25 Ultra" stay distinct.
 */
fun normalizedModel(brand: String, model: String): String {
    val b = cleanText(brand)
    val m = cleanText(model)
    val stripped =
        if (b.isNotEmpty() && m.length > b.length && m.startsWith(b, ignoreCase = true) && m[b.length] == ' ')
            m.substring(b.length).trim()
        else m
    return stripped.lowercase()
}

/** Identity of a model = brand + normalised model name. */
fun modelKey(brand: String, model: String): String = brandKey(brand) + "\u001F" + normalizedModel(brand, model)

/** "Samsung" + "Galaxy S25 Ultra" -> "Samsung Galaxy S25 Ultra" (without repeating the brand). */
fun fullName(brand: String, model: String): String {
    val b = cleanText(brand)
    val m = cleanText(model)
    return if (b.isNotEmpty() && m.startsWith(b, ignoreCase = true) && (m.length == b.length || m[b.length] == ' ')) m
    else "$b $m".trim()
}

fun plural(n: Int, one: String, many: String = one + "s"): String = "$n ${if (n == 1) one else many}"

private val newestFirst = compareByDescending<Sighting> { it.timestamp }.thenByDescending { it.id }

fun List<Sighting>.toModelEntries(): List<ModelEntry> =
    groupBy { modelKey(it.brand, it.model) }.map { (key, group) ->
        val sorted = group.sortedWith(newestFirst)
        val latest = sorted.first()
        ModelEntry(
            key = key,
            brandKey = brandKey(latest.brand),
            brand = cleanText(latest.brand),
            name = fullName(latest.brand, latest.model),
            count = sorted.size,
            firstSpotted = sorted.last().timestamp,
            lastSpotted = latest.timestamp,
            sightings = sorted
        )
    }

fun List<ModelEntry>.toBrandEntries(): List<BrandEntry> =
    groupBy { it.brandKey }.map { (key, models) ->
        val latest = models.maxBy { it.lastSpotted }
        BrandEntry(key, latest.brand, models.size, models.sumOf { it.count }, latest.lastSpotted)
    }.sortedWith(
        compareByDescending<BrandEntry> { it.sightings }
            .thenByDescending { it.models }
            .thenBy { it.brand.lowercase() }
    )

fun List<ModelEntry>.sortModels(mode: ModelSort): List<ModelEntry> = when (mode) {
    ModelSort.COUNT -> sortedWith(
        compareByDescending<ModelEntry> { it.count }.thenByDescending { it.lastSpotted }.thenBy { it.name.lowercase() }
    )
    ModelSort.NAME -> sortedWith(compareBy<ModelEntry> { it.name.lowercase() })
    ModelSort.RECENT -> sortedWith(
        compareByDescending<ModelEntry> { it.lastSpotted }.thenByDescending { it.count }.thenBy { it.name.lowercase() }
    )
}
