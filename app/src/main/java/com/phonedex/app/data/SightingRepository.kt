package com.phonedex.app.data

import android.content.SharedPreferences
import com.phonedex.app.domain.AchievementDef
import com.phonedex.app.domain.Achievements
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class SightingRepository(
    private val dao: SightingDao,
    private val prefs: SharedPreferences
) {
    val sightings: Flow<List<Sighting>> = dao.observeAll()
    val customBrands: Flow<List<String>> = dao.observeCustomBrands()
    val unlocked: Flow<List<UnlockedAchievement>> = dao.observeUnlocked()

    private val achievementLock = Mutex()

    fun observe(id: Long): Flow<Sighting?> = dao.observe(id)
    suspend fun get(id: Long): Sighting? = dao.get(id)

    /** Saves and returns achievements that were newly unlocked by this change. */
    suspend fun save(sighting: Sighting): List<AchievementDef> {
        dao.upsert(sighting)
        if (Brands.ALL.none { it.equals(sighting.brand, ignoreCase = true) }) {
            dao.insertBrand(CustomBrand(sighting.brand))
        }
        return syncAchievements()
    }

    suspend fun delete(id: Long) {
        dao.delete(id)
        syncAchievements()
    }

    /**
     * Recomputes achievements from the saved sightings.
     * - Newly earned ones are stored and returned (so they can be celebrated once).
     * - Ones that are no longer earned (after an edit/delete) are removed so they re-lock.
     * - The very first run silently records what existing sightings already earned,
     *   so upgrading users aren't hit with a pile of pop-ups.
     */
    suspend fun syncAchievements(): List<AchievementDef> = achievementLock.withLock {
        val earned = Achievements.evaluate(dao.getAll()).filter { it.unlocked }.map { it.def }
        val earnedIds = earned.map { it.id }.toSet()
        val stored = dao.getUnlockedIds().toSet()
        val newly = earned.filter { it.id !in stored }
        val now = System.currentTimeMillis()
        newly.forEach { dao.insertUnlocked(UnlockedAchievement(it.id, now)) }
        (stored - earnedIds).forEach { dao.deleteUnlocked(it) }

        val firstRun = !prefs.getBoolean(KEY_SEEDED, false)
        if (firstRun) prefs.edit().putBoolean(KEY_SEEDED, true).apply()
        if (firstRun) emptyList() else newly
    }

    private companion object {
        const val KEY_SEEDED = "achievements_seeded"
    }
}
