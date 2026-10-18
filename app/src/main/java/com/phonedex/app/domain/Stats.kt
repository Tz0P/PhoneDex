package com.phonedex.app.domain

import com.phonedex.app.data.Sighting
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

data class CountItem(val label: String, val count: Int)

private val zone: ZoneId get() = ZoneId.systemDefault()

fun Sighting.title(): String = fullName(brand, model)

private val byCountThenLabel = compareByDescending<CountItem> { it.count }.thenBy { it.label.lowercase() }

fun List<Sighting>.topBrands(): List<CountItem> =
    groupBy { brandKey(it.brand) }
        .map { (_, v) -> CountItem(cleanText(v.first().brand), v.size) }
        .sortedWith(byCountThenLabel)

fun List<Sighting>.topModels(): List<CountItem> =
    toModelEntries().map { CountItem(it.name, it.count) }.sortedWith(byCountThenLabel)

fun List<Sighting>.perDay(days: Int): List<CountItem> {
    val counts = groupingBy { Instant.ofEpochMilli(it.timestamp).atZone(zone).toLocalDate() }.eachCount()
    val today = LocalDate.now(zone)
    val fmt = DateTimeFormatter.ofPattern("d MMM")
    return (days - 1 downTo 0).map {
        val d = today.minusDays(it.toLong())
        CountItem(d.format(fmt), counts[d] ?: 0)
    }
}

fun List<Sighting>.perMonth(months: Int): List<CountItem> {
    val counts = groupingBy { YearMonth.from(Instant.ofEpochMilli(it.timestamp).atZone(zone)) }.eachCount()
    val now = YearMonth.now(zone)
    val fmt = DateTimeFormatter.ofPattern("MMM yy")
    return (months - 1 downTo 0).map {
        val m = now.minusMonths(it.toLong())
        CountItem(m.format(fmt), counts[m] ?: 0)
    }
}

fun startOfDayMillis(daysAgo: Int): Long =
    LocalDate.now(zone).minusDays(daysAgo.toLong()).atStartOfDay(zone).toInstant().toEpochMilli()

enum class TimeFilter(val label: String, private val daysBack: Int?) {
    ANY("Any time", null), TODAY("Today", 0), WEEK("7 days", 6), MONTH("30 days", 29);

    fun matches(ts: Long): Boolean = daysBack == null || ts >= startOfDayMillis(daysBack)
}

object Fmt {
    private val dateF = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
    private val timeF = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
    fun date(ts: Long): String = dateF.format(Instant.ofEpochMilli(ts).atZone(zone))
    fun time(ts: Long): String = timeF.format(Instant.ofEpochMilli(ts).atZone(zone))
    fun dateTime(ts: Long): String = "${date(ts)} · ${time(ts)}"
}
