package com.foccus.app.data.local.db.dao

import androidx.room.*
import com.foccus.app.data.local.db.entity.BlockedAppEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockedAppDao {

    @Query("SELECT * FROM blocked_apps ORDER BY appName ASC")
    fun getAllBlockedApps(): Flow<List<BlockedAppEntity>>

    @Query("SELECT * FROM blocked_apps WHERE isEnabled = 1")
    fun getEnabledBlockedApps(): Flow<List<BlockedAppEntity>>

    @Query("SELECT packageName FROM blocked_apps WHERE isEnabled = 1")
    suspend fun getEnabledPackageNames(): List<String>

    @Query("SELECT * FROM blocked_apps WHERE packageName = :packageName")
    suspend fun getBlockedApp(packageName: String): BlockedAppEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlockedApp(app: BlockedAppEntity)

    @Delete
    suspend fun deleteBlockedApp(app: BlockedAppEntity)

    @Query("DELETE FROM blocked_apps WHERE packageName = :packageName")
    suspend fun deleteByPackageName(packageName: String)

    @Query("UPDATE blocked_apps SET isEnabled = :isEnabled WHERE packageName = :packageName")
    suspend fun setEnabled(packageName: String, isEnabled: Boolean)

    @Query("UPDATE blocked_apps SET blockedCount = blockedCount + 1 WHERE packageName = :packageName")
    suspend fun incrementBlockedCount(packageName: String)

    @Query("SELECT COUNT(*) FROM blocked_apps WHERE isEnabled = 1")
    fun getEnabledCount(): Flow<Int>
}
