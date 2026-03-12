package com.foccus.app.presentation.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foccus.app.data.grayscale.GrayscaleManager
import com.foccus.app.data.local.preferences.UserPreferences
import com.foccus.app.domain.repository.BlockedAppRepository
import com.foccus.app.domain.repository.FocusSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class HomeUiState(
    val isBlockingEnabled: Boolean = true,
    val isGrayscaleEnabled: Boolean = false,
    val isShortsBlockingEnabled: Boolean = true,
    val blockedAppsCount: Int = 0,
    val todayFocusMinutes: Int = 0,
    val completedSessionsToday: Int = 0,
    val hasActiveSession: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val preferences: UserPreferences,
    private val grayscaleManager: GrayscaleManager,
    private val blockedAppRepo: BlockedAppRepository,
    private val sessionRepo: FocusSessionRepository
) : ViewModel() {

    private val todayMidnight: Long
        get() {
            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            return cal.timeInMillis
        }

    val uiState: StateFlow<HomeUiState> = combine(
        preferences.blockingEnabled,
        preferences.grayscaleEnabled,
        preferences.blockShortsEnabled,
        blockedAppRepo.getEnabledCount(),
        sessionRepo.getTotalFocusMinutesSince(todayMidnight),
        sessionRepo.getCompletedSessionsSince(todayMidnight),
    ) { values ->
        HomeUiState(
            isBlockingEnabled = values[0] as Boolean,
            isGrayscaleEnabled = values[1] as Boolean,
            isShortsBlockingEnabled = values[2] as Boolean,
            blockedAppsCount = values[3] as Int,
            todayFocusMinutes = (values[4] as? Int) ?: 0,
            completedSessionsToday = values[5] as Int
        )
    }.combine(preferences.activeSessionId) { state, sessionId ->
        state.copy(hasActiveSession = sessionId != null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    fun toggleBlocking(enabled: Boolean) {
        viewModelScope.launch { preferences.setBlockingEnabled(enabled) }
    }

    /**
     * @return true if applied directly, false if needs manual setup
     */
    fun toggleGrayscale(enabled: Boolean): Boolean {
        val applied = grayscaleManager.toggle(enabled)
        if (applied) {
            viewModelScope.launch { preferences.setGrayscaleEnabled(enabled) }
        }
        return applied
    }

    fun toggleShortsBlocking(enabled: Boolean) {
        viewModelScope.launch { preferences.setBlockShortsEnabled(enabled) }
    }

    fun reapplyGrayscaleIfNeeded() {
        viewModelScope.launch {
            grayscaleManager.syncPreferenceWithSystem()
            grayscaleManager.reapplyGrayscaleIfEnabled()
        }
    }
}
