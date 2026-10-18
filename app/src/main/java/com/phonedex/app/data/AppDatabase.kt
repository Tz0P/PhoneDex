package com.phonedex.app.data

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

@Dao
interface SightingDao {
    @Query("SELECT * FROM sightings ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<Sighting>>

    @Query("SELECT * FROM sightings")
    suspend fun getAll(): List<Sighting>

    @Query("SELECT * FROM sightings WHERE id = :id")
    fun observe(id: Long): Flow<Sighting?>

    @Query("SELECT * FROM sightings WHERE id = :id")
    suspend fun get(id: Long): Sighting?

    @Upsert
    suspend fun upsert(sighting: Sighting)

    @Query("DELETE FROM sightings WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT name FROM custom_brands ORDER BY name")
    fun observeCustomBrands(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBrand(brand: CustomBrand)

    @Query("SELECT * FROM unlocked_achievements")
    fun observeUnlocked(): Flow<List<UnlockedAchievement>>

    @Query("SELECT id FROM unlocked_achievements")
    suspend fun getUnlockedIds(): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUnlocked(unlocked: UnlockedAchievement)

    @Query("DELETE FROM unlocked_achievements WHERE id = :id")
    suspend fun deleteUnlocked(id: String)
}

@Database(
    entities = [Sighting::class, CustomBrand::class, UnlockedAchievement::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): SightingDao

    companion object {
        /** Adds the achievements table. Existing sightings are untouched. */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `unlocked_achievements` " +
                        "(`id` TEXT NOT NULL, `unlockedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))"
                )
            }
        }

        fun build(context: Context): AppDatabase =
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "phonedex.db")
                .addMigrations(MIGRATION_1_2)
                .build()
    }
}
