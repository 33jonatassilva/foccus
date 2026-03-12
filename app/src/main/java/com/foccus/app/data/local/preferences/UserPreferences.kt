package com.foccus.app.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "foccus_prefs")

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    private object Keys {
        val FOCUS_DURATION = intPreferencesKey("focus_duration_minutes")
        val BREAK_DURATION = intPreferencesKey("break_duration_minutes")
        val GRAYSCALE_ENABLED = booleanPreferencesKey("grayscale_enabled")
        val BLOCKING_ENABLED = booleanPreferencesKey("blocking_enabled")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val BLOCK_SHORTS_ENABLED = booleanPreferencesKey("block_shorts_enabled")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val ACTIVE_SESSION_ID = longPreferencesKey("active_session_id")
        val SESSION_START_TIME = longPreferencesKey("session_start_time")
    }

    val focusDuration: Flow<Int> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs -> prefs[Keys.FOCUS_DURATION] ?: 25 }

    val breakDuration: Flow<Int> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs -> prefs[Keys.BREAK_DURATION] ?: 5 }

    val grayscaleEnabled: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs -> prefs[Keys.GRAYSCALE_ENABLED] ?: false }

    val blockingEnabled: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs -> prefs[Keys.BLOCKING_ENABLED] ?: true }

    val notificationsEnabled: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs -> prefs[Keys.NOTIFICATIONS_ENABLED] ?: true }

    val blockShortsEnabled: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs -> prefs[Keys.BLOCK_SHORTS_ENABLED] ?: true }

    val onboardingCompleted: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs -> prefs[Keys.ONBOARDING_COMPLETED] ?: false }

    val activeSessionId: Flow<Long?> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs -> prefs[Keys.ACTIVE_SESSION_ID]?.takeIf { it > 0 } }

    suspend fun setFocusDuration(minutes: Int) {
        dataStore.edit { prefs -> prefs[Keys.FOCUS_DURATION] = minutes }
    }

    suspend fun setBreakDuration(minutes: Int) {
        dataStore.edit { prefs -> prefs[Keys.BREAK_DURATION] = minutes }
    }

    suspend fun setGrayscaleEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[Keys.GRAYSCALE_ENABLED] = enabled }
    }

    suspend fun setBlockingEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[Keys.BLOCKING_ENABLED] = enabled }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[Keys.NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setBlockShortsEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[Keys.BLOCK_SHORTS_ENABLED] = enabled }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { prefs -> prefs[Keys.ONBOARDING_COMPLETED] = completed }
    }

    suspend fun setActiveSessionId(sessionId: Long?) {
        dataStore.edit { prefs ->
            if (sessionId != null) {
                prefs[Keys.ACTIVE_SESSION_ID] = sessionId
                prefs[Keys.SESSION_START_TIME] = System.currentTimeMillis()
            } else {
                prefs.remove(Keys.ACTIVE_SESSION_ID)
                prefs.remove(Keys.SESSION_START_TIME)
            }
        }
    }
}
