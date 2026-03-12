package com.foccus.app.domain.repository

import com.foccus.app.domain.model.FocusSession
import kotlinx.coroutines.flow.Flow

interface FocusSessionRepository {
    fun getAllSessions(): Flow<List<FocusSession>>
    fun getSessionsSince(since: Long): Flow<List<FocusSession>>
    suspend fun startSession(durationMinutes: Int): Long
    suspend fun finishSession(id: Long, durationMinutes: Int, completed: Boolean)
    suspend fun incrementBlockedAttempts(sessionId: Long)
    fun getTotalCompletedSessions(): Flow<Int>
    fun getTotalFocusMinutesSince(since: Long): Flow<Int?>
    fun getCompletedSessionsSince(since: Long): Flow<Int>
    fun getTotalBlockedAttemptsSince(since: Long): Flow<Int?>
}
