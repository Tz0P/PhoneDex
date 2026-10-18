package com.phonedex.app.domain

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.phonedex.app.data.Sighting
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.roundToLong

enum class Category(val label: String) {
    TOTAL("Total sightings"),
    BRANDS("Brand diversity"),
    MODELS("Model diversity"),
    MILESTONES("Milestones"),
    BONUS("Bonus")
}

enum class Metric { TOTAL, BRANDS, MODELS, BRANDS_IN_DAY, LOCATIONS, DAYS, STREAK, NIGHT, NOTES }

data class AchievementDef(
    val id: String,
    val title: String,
    val description: String,
    val category: Category,
    val icon: ImageVector,
    val metric: Metric,
    val target: Int
)

data class AchievementProgress(val def: AchievementDef, val value: Int) {
    val unlocked: Boolean get() = value >= def.target
    val shownValue: Int get() = value.coerceAtMost(def.target)
    val fraction: Float get() = (value.toFloat() / def.target).coerceIn(0f, 1f)
}

object Achievements {

    private fun d(id: String, title: String, desc: String, cat: Category, icon: ImageVector, metric: Metric, target: Int) =
        AchievementDef(id, title, desc, cat, icon, metric, target)

    val ALL: List<AchievementDef> = listOf(
        d("first_spot", "First Spot", "Log your very first sighting", Category.TOTAL, Icons.Rounded.Visibility, Metric.TOTAL, 1),
        d("getting_started", "Getting Started", "Reach 10 sightings", Category.TOTAL, Icons.Rounded.Flag, Metric.TOTAL, 10),
        d("dedicated_spotter", "Dedicated Spotter", "Reach 25 sightings", Category.TOTAL, Icons.Rounded.Star, Metric.TOTAL, 25),
        d("phone_hunter", "Phone Hunter", "Reach 50 sightings", Category.TOTAL, Icons.Rounded.Search, Metric.TOTAL, 50),
        d("legendary_collector", "Legendary Collector", "Reach 100 sightings", Category.TOTAL, Icons.Rounded.EmojiEvents, Metric.TOTAL, 100),
        d("ultimate_spotter", "Ultimate Spotter", "Reach 250 sightings", Category.TOTAL, Icons.Rounded.WorkspacePremium, Metric.TOTAL, 250),

        d("brand_explorer", "Brand Explorer", "Discover 3 different brands", Category.BRANDS, Icons.Rounded.Category, Metric.BRANDS, 3),
        d("brand_collector", "Brand Collector", "Discover 5 different brands", Category.BRANDS, Icons.Rounded.Collections, Metric.BRANDS, 5),
        d("brand_enthusiast", "Brand Enthusiast", "Discover 10 different brands", Category.BRANDS, Icons.Rounded.Layers, Metric.BRANDS, 10),
        d("brand_master", "Brand Master", "Discover 20 different brands", Category.BRANDS, Icons.Rounded.Diamond, Metric.BRANDS, 20),

        d("model_explorer", "Model Explorer", "Discover 10 unique models", Category.MODELS, Icons.Rounded.PhoneAndroid, Metric.MODELS, 10),
        d("model_collector", "Model Collector", "Discover 25 unique models", Category.MODELS, Icons.Rounded.Stars, Metric.MODELS, 25),
        d("model_master", "Model Master", "Discover 50 unique models", Category.MODELS, Icons.Rounded.MilitaryTech, Metric.MODELS, 50),
        d("model_legend", "Model Legend", "Discover 100 unique models", Category.MODELS, Icons.Rounded.EmojiEvents, Metric.MODELS, 100),

        d("multi_brand_day", "Multi-Brand Day", "Spot 3 different brands on the same day", Category.MILESTONES, Icons.Rounded.Bolt, Metric.BRANDS_IN_DAY, 3),
        d("on_the_move", "On the Move", "Log sightings in 5 different places (GPS)", Category.MILESTONES, Icons.Rounded.Place, Metric.LOCATIONS, 5),
        d("regular_spotter", "Regular Spotter", "Log sightings on 7 different days", Category.MILESTONES, Icons.Rounded.CalendarMonth, Metric.DAYS, 7),
        d("century_of_brands", "Century of Brands", "Reach 100 total sightings", Category.MILESTONES, Icons.Rounded.Public, Metric.TOTAL, 100),

        d("night_owl", "Night Owl", "Spot a phone between midnight and 5 AM", Category.BONUS, Icons.Rounded.NightsStay, Metric.NIGHT, 1),
        d("detective", "Pocket Detective", "Add notes to 10 sightings", Category.BONUS, Icons.Rounded.EditNote, Metric.NOTES, 10),
        d("three_day_streak", "On a Roll", "Log sightings 3 days in a row", Category.BONUS, Icons.Rounded.LocalFireDepartment, Metric.STREAK, 3)
    )

    fun evaluate(sightings: List<Sighting>): List<AchievementProgress> {
        val m = metrics(sightings)
        return ALL.map { AchievementProgress(it, m.getValue(it.metric)) }
    }

    /** Every number comes straight from saved sightings. */
    fun metrics(list: List<Sighting>): Map<Metric, Int> {
        val zone = ZoneId.systemDefault()
        fun dateOf(s: Sighting): LocalDate = Instant.ofEpochMilli(s.timestamp).atZone(zone).toLocalDate()

        val byDay = list.groupBy { dateOf(it) }
        val days = byDay.keys.sorted()

        var streak = 0
        var run = 0
        var prev: LocalDate? = null
        for (day in days) {
            run = if (prev != null && prev.plusDays(1) == day) run + 1 else 1
            if (run > streak) streak = run
            prev = day
        }

        // A "place" is a ~500 m grid cell of a sighting's saved GPS coordinates.
        // Sightings without coordinates never count.
        val cell = 0.005
        val places = list.mapNotNull { s ->
            val la = s.latitude
            val lo = s.longitude
            if (la != null && lo != null) "${(la / cell).roundToLong()}:${(lo / cell).roundToLong()}" else null
        }.toSet()

        return mapOf(
            Metric.TOTAL to list.size,
            Metric.BRANDS to list.map { brandKey(it.brand) }.toSet().size,
            Metric.MODELS to list.map { modelKey(it.brand, it.model) }.toSet().size,
            Metric.BRANDS_IN_DAY to (byDay.values.maxOfOrNull { day -> day.map { brandKey(it.brand) }.toSet().size } ?: 0),
            Metric.LOCATIONS to places.size,
            Metric.DAYS to days.size,
            Metric.STREAK to streak,
            Metric.NIGHT to list.count { Instant.ofEpochMilli(it.timestamp).atZone(zone).hour in 0..4 },
            Metric.NOTES to list.count { it.notes.isNotBlank() }
        )
    }
}
