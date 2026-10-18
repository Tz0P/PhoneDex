package com.phonedex.app

import android.app.Application
import com.phonedex.app.data.AppDatabase
import com.phonedex.app.data.SightingRepository
import org.osmdroid.config.Configuration
import java.io.File

class PhoneDexApplication : Application() {
    val repository: SightingRepository by lazy {
        SightingRepository(
            AppDatabase.build(this).dao(),
            getSharedPreferences("phonedex_prefs", MODE_PRIVATE)
        )
    }

    override fun onCreate() {
        super.onCreate()
        // OpenStreetMap map engine: app-private tile cache (so previously viewed areas work offline).
        Configuration.getInstance().apply {
            load(this@PhoneDexApplication, getSharedPreferences("osmdroid", MODE_PRIVATE))
            userAgentValue = packageName
            val base = File(filesDir, "osmdroid")
            osmdroidBasePath = base
            osmdroidTileCache = File(base, "tiles")
            tileFileSystemCacheMaxBytes = 200L * 1024 * 1024
            tileFileSystemCacheTrimBytes = 150L * 1024 * 1024
        }
    }
}
