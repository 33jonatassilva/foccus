package com.foccus.app.data.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import com.foccus.app.data.local.db.dao.BlockedAppDao
import com.foccus.app.data.local.db.entity.BlockedAppEntity
import com.foccus.app.domain.model.AppInfo
import com.foccus.app.domain.model.BlockedApp
import com.foccus.app.domain.repository.BlockedAppRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlockedAppRepositoryImpl @Inject constructor(
    private val dao: BlockedAppDao,
    @ApplicationContext private val context: Context
) : BlockedAppRepository {

    override fun getAllBlockedApps(): Flow<List<BlockedApp>> =
        dao.getAllBlockedApps().map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getEnabledBlockedApps(): Flow<List<BlockedApp>> =
        dao.getEnabledBlockedApps().map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun getEnabledPackageNames(): List<String> =
        dao.getEnabledPackageNames()

    override suspend fun addBlockedApp(app: BlockedApp) {
        dao.insertBlockedApp(app.toEntity())
    }

    override suspend fun removeBlockedApp(packageName: String) {
        dao.deleteByPackageName(packageName)
    }

    override suspend fun setEnabled(packageName: String, enabled: Boolean) {
        dao.setEnabled(packageName, enabled)
    }

    override suspend fun incrementBlockedCount(packageName: String) {
        dao.incrementBlockedCount(packageName)
    }

    override fun getEnabledCount(): Flow<Int> = dao.getEnabledCount()

    override fun getInstalledApps(): List<AppInfo> {
        val pm = context.packageManager
        val installedApps = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA.toLong()))
        } else {
            @Suppress("DEPRECATION")
            pm.getInstalledApplications(PackageManager.GET_META_DATA)
        }

        return installedApps
            .filter { appInfo ->
                appInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0 &&
                        appInfo.packageName != context.packageName
            }
            .map { appInfo ->
                AppInfo(
                    packageName = appInfo.packageName,
                    appName = pm.getApplicationLabel(appInfo).toString(),
                    icon = pm.getApplicationIcon(appInfo)
                )
            }
            .sortedBy { it.appName }
    }

    private fun BlockedAppEntity.toDomain() = BlockedApp(
        packageName = packageName,
        appName = appName,
        isEnabled = isEnabled,
        blockedCount = blockedCount,
        addedAt = addedAt
    )

    private fun BlockedApp.toEntity() = BlockedAppEntity(
        packageName = packageName,
        appName = appName,
        isEnabled = isEnabled,
        blockedCount = blockedCount,
        addedAt = addedAt
    )
}
