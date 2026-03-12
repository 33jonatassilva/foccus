package com.foccus.app.domain.repository

import com.foccus.app.domain.model.AppInfo
import com.foccus.app.domain.model.BlockedApp
import kotlinx.coroutines.flow.Flow

interface BlockedAppRepository {
    fun getAllBlockedApps(): Flow<List<BlockedApp>>
    fun getEnabledBlockedApps(): Flow<List<BlockedApp>>
    suspend fun getEnabledPackageNames(): List<String>
    suspend fun addBlockedApp(app: BlockedApp)
    suspend fun removeBlockedApp(packageName: String)
    suspend fun setEnabled(packageName: String, enabled: Boolean)
    suspend fun incrementBlockedCount(packageName: String)
    fun getEnabledCount(): Flow<Int>
    fun getInstalledApps(): List<AppInfo>
}
