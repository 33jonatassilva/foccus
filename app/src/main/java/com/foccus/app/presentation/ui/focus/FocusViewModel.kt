package com.foccus.app.presentation.ui.focus

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foccus.app.data.local.preferences.UserPreferences
import com.foccus.app.domain.repository.FocusSessionRepository
import com.foccus.app.service.FocusSessionService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

enum class FocusState { IDLE, RUNNING, PAUSED, FINISHED }

data class FocusUiState(
    val state: FocusState = FocusState.IDLE,
    val totalSeconds: Int = 25 * 60,
    val remainingSeconds: Int = 25 * 60,
    val plannedDurationMinutes: Int = 25,
    val elapsedMinutes: Int = 0,
    val sessionId: Long? = null
) {
    val progress: Float
        get() = if (totalSeconds > 0) 1f - remainingSeconds.toFloat() / totalSeconds else 0f

    val timeText: String
        get() {
            val mins = remainingSeconds / 60
            val secs = remainingSeconds % 60
            return "%02d:%02d".format(mins, secs)
        }

    val isActive: Boolean get() = state == FocusState.RUNNING
}

@HiltViewModel
class FocusViewModel @Inject constructor(
    private val preferences: UserPreferences,
    private val sessionRepo: FocusSessionRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(FocusUiState())
    val uiState: StateFlow<FocusUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    val focusDuration: StateFlow<Int> = preferences.focusDuration
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 25)

    init {
        viewModelScope.launch {
            preferences.focusDuration.collect { duration ->
                if (_uiState.value.state == FocusState.IDLE) {
                    _uiState.update {
                        it.copy(
                            plannedDurationMinutes = duration,
                            totalSeconds = duration * 60,
                            remainingSeconds = duration * 60
                        )
                    }
                }
            }
        }

        // Verifica se há sessão ativa ao iniciar
        viewModelScope.launch {
            preferences.activeSessionId.first()?.let { sessionId ->
                _uiState.update { it.copy(sessionId = sessionId, state = FocusState.RUNNING) }
                resumeTimer()
            }
        }
    }

    fun startSession() {
        viewModelScope.launch {
            val duration = _uiState.value.plannedDurationMinutes
            val sessionId = sessionRepo.startSession(duration)

            _uiState.update {
                it.copy(
                    state = FocusState.RUNNING,
                    sessionId = sessionId,
                    remainingSeconds = duration * 60,
                    totalSeconds = duration * 60
                )
            }

            preferences.setActiveSessionId(sessionId)
            startServiceTimer()
            startCountdown()
        }
    }

    fun pauseSession() {
        timerJob?.cancel()
        _uiState.update { it.copy(state = FocusState.PAUSED) }
    }

    fun resumeSession() {
        _uiState.update { it.copy(state = FocusState.RUNNING) }
        startCountdown()
    }

    fun stopSession() {
        viewModelScope.launch {
            timerJob?.cancel()
            val state = _uiState.value

            state.sessionId?.let { id ->
                val elapsed = (state.totalSeconds - state.remainingSeconds) / 60
                sessionRepo.finishSession(id, elapsed, false)
            }

            preferences.setActiveSessionId(null)
            stopServiceTimer()

            _uiState.update {
                FocusUiState(
                    plannedDurationMinutes = it.plannedDurationMinutes,
                    totalSeconds = it.plannedDurationMinutes * 60,
                    remainingSeconds = it.plannedDurationMinutes * 60
                )
            }
        }
    }

    fun setDuration(minutes: Int) {
        if (_uiState.value.state == FocusState.IDLE) {
            viewModelScope.launch {
                preferences.setFocusDuration(minutes)
                _uiState.update {
                    it.copy(
                        plannedDurationMinutes = minutes,
                        totalSeconds = minutes * 60,
                        remainingSeconds = minutes * 60
                    )
                }
            }
        }
    }

    private fun startCountdown() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.remainingSeconds > 0 &&
                _uiState.value.state == FocusState.RUNNING
            ) {
                delay(1000L)
                val current = _uiState.value
                if (current.state == FocusState.RUNNING) {
                    val newRemaining = current.remainingSeconds - 1
                    val elapsed = (current.totalSeconds - newRemaining) / 60

                    if (newRemaining <= 0) {
                        onSessionComplete()
                    } else {
                        _uiState.update {
                            it.copy(
                                remainingSeconds = newRemaining,
                                elapsedMinutes = elapsed
                            )
                        }
                    }
                }
            }
        }
    }

    private fun resumeTimer() {
        startCountdown()
    }

    private suspend fun onSessionComplete() {
        val state = _uiState.value
        state.sessionId?.let { id ->
            sessionRepo.finishSession(id, state.plannedDurationMinutes, true)
        }
        preferences.setActiveSessionId(null)
        stopServiceTimer()

        _uiState.update {
            it.copy(
                state = FocusState.FINISHED,
                remainingSeconds = 0
            )
        }
    }

    private fun startServiceTimer() {
        val intent = Intent(context, FocusSessionService::class.java).apply {
            action = FocusSessionService.ACTION_START
        }
        context.startForegroundService(intent)
    }

    private fun stopServiceTimer() {
        val intent = Intent(context, FocusSessionService::class.java).apply {
            action = FocusSessionService.ACTION_STOP
        }
        context.startService(intent)
    }

    override fun onCleared() {
        timerJob?.cancel()
        super.onCleared()
    }
}
