package com.foccus.app.data.local.db.dao

import androidx.room.*
import com.foccus.app.data.local.db.entity.FocusSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusSessionDao {

    @Query("SELECT * FROM focus_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<FocusSessionEntity>>

    @Query("SELECT * FROM focus_sessions WHERE startTime >= :since ORDER BY startTime DESC")
    fun getSessionsSince(since: Long): Flow<List<FocusSessionEntity>>

    @Query("SELECT * FROM focus_sessions WHERE id = :id")
    suspend fun getSession(id: Long): FocusSessionEntity?

    @Insert
    suspend fun insertSession(session: FocusSessionEntity): Long

    @Update
    suspend fun updateSession(session: FocusSessionEntity)

    @Query("UPDATE focus_sessions SET endTime = :endTime, actualDurationMinutes = :duration, wasCompleted = :completed WHERE id = :id")
    suspend fun finishSession(id: Long, endTime: Long, duration: Int, completed: Boolean)

    @Query("UPDATE focus_sessions SET blockedAttempts = blockedAttempts + 1 WHERE id = :id")
    suspend fun incrementBlockedAttempts(id: Long)

    @Query("SELECT COUNT(*) FROM focus_sessions WHERE wasCompleted = 1")
    fun getTotalCompletedSessions(): Flow<Int>

    @Query("SELECT SUM(actualDurationMinutes) FROM focus_sessions WHERE startTime >= :since")
    fun getTotalFocusMinutesSince(since: Long): Flow<Int?>

    @Query("SELECT COUNT(*) FROM focus_sessions WHERE wasCompleted = 1 AND startTime >= :since")
    fun getCompletedSessionsSince(since: Long): Flow<Int>

    @Query("SELECT SUM(blockedAttempts) FROM focus_sessions WHERE startTime >= :since")
    fun getTotalBlockedAttemptsSince(since: Long): Flow<Int?>
}
