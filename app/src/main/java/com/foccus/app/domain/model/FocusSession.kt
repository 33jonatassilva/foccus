package com.foccus.app.domain.model

data class FocusSession(
    val id: Long = 0,
    val startTime: Long,
    val endTime: Long? = null,
    val plannedDurationMinutes: Int,
    val actualDurationMinutes: Int = 0,
    val wasCompleted: Boolean = false,
    val blockedAttempts: Int = 0
) {
    val isActive: Boolean get() = endTime == null
    val completionPercentage: Float
        get() = if (plannedDurationMinutes > 0)
            (actualDurationMinutes.toFloat() / plannedDurationMinutes).coerceIn(0f, 1f)
        else 0f
}

data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: android.graphics.drawable.Drawable? = null
)

data class StatsData(
    val todayFocusMinutes: Int = 0,
    val weekFocusMinutes: Int = 0,
    val totalSessions: Int = 0,
    val completedSessions: Int = 0,
    val blockedAttempts: Int = 0,
    val currentStreak: Int = 0
)
