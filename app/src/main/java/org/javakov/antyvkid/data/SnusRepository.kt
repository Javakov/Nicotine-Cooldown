package org.javakov.antyvkid.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.javakov.antyvkid.domain.WindowSlot

private val Context.snusDataStore by preferencesDataStore(name = "snus_state")

data class SnusState(
    val lastMorningSubmitTimestamp: Long,
    val lastEveningSubmitTimestamp: Long,
    val lastKnownSystemTime: Long
) {
    companion object {
        val EMPTY = SnusState(
            lastMorningSubmitTimestamp = 0L,
            lastEveningSubmitTimestamp = 0L,
            lastKnownSystemTime = 0L
        )
    }
}

class SnusRepository(private val context: Context) {

    private object Keys {
        val LAST_MORNING_SUBMIT = longPreferencesKey("last_morning_submit_timestamp")
        val LAST_EVENING_SUBMIT = longPreferencesKey("last_evening_submit_timestamp")
        val LAST_SEEN_SYSTEM = longPreferencesKey("last_known_system_time")
    }

    val state: Flow<SnusState> = context.snusDataStore.data.map { it.toState() }

    suspend fun recordSubmit(timestamp: Long, slot: WindowSlot) {
        context.snusDataStore.edit { prefs ->
            when (slot) {
                WindowSlot.MORNING -> prefs[Keys.LAST_MORNING_SUBMIT] = timestamp
                WindowSlot.EVENING -> prefs[Keys.LAST_EVENING_SUBMIT] = timestamp
            }
            prefs[Keys.LAST_SEEN_SYSTEM] = timestamp
        }
    }

    suspend fun touchSystemTime(now: Long) {
        context.snusDataStore.edit { prefs ->
            val previous = prefs[Keys.LAST_SEEN_SYSTEM] ?: 0L
            if (now > previous) prefs[Keys.LAST_SEEN_SYSTEM] = now
        }
    }

    private fun Preferences.toState() = SnusState(
        lastMorningSubmitTimestamp = this[Keys.LAST_MORNING_SUBMIT] ?: 0L,
        lastEveningSubmitTimestamp = this[Keys.LAST_EVENING_SUBMIT] ?: 0L,
        lastKnownSystemTime = this[Keys.LAST_SEEN_SYSTEM] ?: 0L
    )
}
