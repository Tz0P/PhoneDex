package com.phonedex.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sightings")
data class Sighting(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val brand: String,
    val model: String,
    val location: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val timestamp: Long,
    val notes: String = ""
)

@Entity(tableName = "custom_brands")
data class CustomBrand(@PrimaryKey val name: String)

/** Achievements the user currently holds. Presence of a row = unlocked (and already celebrated). */
@Entity(tableName = "unlocked_achievements")
data class UnlockedAchievement(
    @PrimaryKey val id: String,
    val unlockedAt: Long
)
