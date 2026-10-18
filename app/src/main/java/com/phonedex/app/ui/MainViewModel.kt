package com.phonedex.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phonedex.app.data.Sighting
import com.phonedex.app.data.SightingRepository
import com.phonedex.app.data.UnlockedAchievement
import com.phonedex.app.domain.AchievementDef
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(private val repo: SightingRepository) : ViewModel() {

    /** null while the first database read is in flight. */
    val sightings: StateFlow<List<Sighting>?> =
        repo.sightings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val customBrands: StateFlow<List<String>> =
        repo.customBrands.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val unlocked: StateFlow<List<UnlockedAchievement>> =
        repo.unlocked.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _unlockEvents = MutableSharedFlow<List<AchievementDef>>(extraBufferCapacity = 8)
    /** Emits each time a save unlocks something new (never for already-unlocked achievements). */
    val unlockEvents: SharedFlow<List<AchievementDef>> = _unlockEvents.asSharedFlow()

    init {
        // Silently reconcile achievements with existing sightings (e.g. right after an app update).
        viewModelScope.launch { repo.syncAchievements() }
    }

    fun observe(id: Long): Flow<Sighting?> = repo.observe(id)
    suspend fun get(id: Long): Sighting? = repo.get(id)

    fun save(sighting: Sighting, onDone: () -> Unit) {
        viewModelScope.launch {
            val newly = repo.save(sighting)
            if (newly.isNotEmpty()) _unlockEvents.emit(newly)
            onDone()
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch { repo.delete(id) }
    }
}
