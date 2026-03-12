package com.foccus.app.data.repository

import com.foccus.app.data.local.db.dao.FocusSessionDao
import com.foccus.app.data.local.db.entity.FocusSessionEntity
import com.foccus.app.domain.model.FocusSession
import com.foccus.app.domain.repository.FocusSessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FocusSessionRepositoryImpl @Inject constructor(
    private val dao: FocusSessionDao
) : FocusSessionRepository {

    override fun getAllSessions(): Flow<List<FocusSession>> =
        dao.getAllSessions().map { entities -> entities.map { it.toDomain() } }

    override fun getSessionsSince(since: Long): Flow<List<FocusSession>> =
        dao.getSessionsSince(since).map { entities -> entities.map { it.toDomain() } }

    override suspend fun startSession(durationMinutes: Int): Long {
        val session = FocusSessionEntity(
            startTime = System.currentTimeMillis(),
            plannedDurationMinutes = durationMinutes
        )
        return dao.insertSession(session)
    }

    override suspend fun finishSession(id: Long, durationMinutes: Int, completed: Boolean) {
        dao.finishSession(
            id = id,
            endTime = System.currentTimeMillis(),
            duration = durationMinutes,
            completed = completed
        )
    }

    override suspend fun incrementBlockedAttempts(sessionId: Long) {
        dao.incrementBlockedAttempts(sessionId)
    }

    override fun getTotalCompletedSessions(): Flow<Int> = dao.getTotalCompletedSessions()

    override fun getTotalFocusMinutesSince(since: Long): Flow<Int?> =
        dao.getTotalFocusMinutesSince(since)

    override fun getCompletedSessionsSince(since: Long): Flow<Int> =
        dao.getCompletedSessionsSince(since)

    override fun getTotalBlockedAttemptsSince(since: Long): Flow<Int?> =
        dao.getTotalBlockedAttemptsSince(since)

    private fun FocusSessionEntity.toDomain() = FocusSession(
        id = id,
        startTime = startTime,
        endTime = endTime,
        plannedDurationMinutes = plannedDurationMinutes,
        actualDurationMinutes = actualDurationMinutes,
        wasCompleted = wasCompleted,
        blockedAttempts = blockedAttempts
    )
}
