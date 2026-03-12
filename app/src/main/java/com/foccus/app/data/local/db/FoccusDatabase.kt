package com.foccus.app.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.foccus.app.data.local.db.dao.BlockedAppDao
import com.foccus.app.data.local.db.dao.FocusSessionDao
import com.foccus.app.data.local.db.entity.BlockedAppEntity
import com.foccus.app.data.local.db.entity.FocusSessionEntity

@Database(
    entities = [BlockedAppEntity::class, FocusSessionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class FoccusDatabase : RoomDatabase() {
    abstract fun blockedAppDao(): BlockedAppDao
    abstract fun focusSessionDao(): FocusSessionDao

    companion object {
        const val DATABASE_NAME = "foccus_db"
    }
}
