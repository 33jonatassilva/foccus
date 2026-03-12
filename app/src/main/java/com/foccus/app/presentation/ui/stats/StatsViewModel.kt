package com.foccus.app.presentation.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foccus.app.domain.repository.FocusSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.util.Calendar
import javax.inject.Inject

data class StatsUiState(
    val todayMinutes: Int = 0,
    val weekMinutes: Int = 0,
    val monthMinutes: Int = 0,
    val totalCompleted: Int = 0,
    val weekCompleted: Int = 0,
    val totalBlockedAttempts: Int = 0
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val sessionRepo: FocusSessionRepository
) : ViewModel() {

    private val todayStart: Long get() = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private val weekStart: Long get() = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private val monthStart: Long get() = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val uiState: StateFlow<StatsUiState> = combine(
        sessionRepo.getTotalFocusMinutesSince(todayStart),
        sessionRepo.getTotalFocusMinutesSince(weekStart),
        sessionRepo.getTotalFocusMinutesSince(monthStart),
        sessionRepo.getTotalCompletedSessions(),
        sessionRepo.getCompletedSessionsSince(weekStart)
    ) { today, week, month, totalCompleted, weekCompleted ->
        StatsUiState(
            todayMinutes = today ?: 0,
            weekMinutes = week ?: 0,
            monthMinutes = month ?: 0,
            totalCompleted = totalCompleted,
            weekCompleted = weekCompleted
        )
    }.combine(sessionRepo.getTotalBlockedAttemptsSince(weekStart)) { state, blocked ->
        state.copy(totalBlockedAttempts = blocked ?: 0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatsUiState())
}
