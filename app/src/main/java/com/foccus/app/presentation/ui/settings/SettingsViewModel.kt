package com.foccus.app.presentation.ui.settings

import android.content.Context
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foccus.app.data.grayscale.GrayscaleManager
import com.foccus.app.data.local.preferences.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val focusDuration: Int = 25,
    val breakDuration: Int = 5,
    val grayscaleEnabled: Boolean = false,
    val blockingEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val blockShortsEnabled: Boolean = true,
    val hasOverlayPermission: Boolean = false,
    val hasUsageStatsPermission: Boolean = false,
    val hasAccessibilityEnabled: Boolean = false,
    val showGrayscaleGuide: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferences: UserPreferences,
    private val grayscaleManager: GrayscaleManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _permissionsState = MutableStateFlow(
        SettingsUiState(
            hasOverlayPermission = checkOverlayPermission(),
            hasUsageStatsPermission = checkUsageStatsPermission(),
            hasAccessibilityEnabled = checkAccessibilityService()
        )
    )

    val uiState: StateFlow<SettingsUiState> = combine(
        preferences.focusDuration,
        preferences.breakDuration,
        preferences.grayscaleEnabled,
        preferences.blockingEnabled,
        preferences.notificationsEnabled,
        preferences.blockShortsEnabled,
    ) { values ->
        SettingsUiState(
            focusDuration = values[0] as Int,
            breakDuration = values[1] as Int,
            grayscaleEnabled = values[2] as Boolean,
            blockingEnabled = values[3] as Boolean,
            notificationsEnabled = values[4] as Boolean,
            blockShortsEnabled = values[5] as Boolean
        )
    }.combine(_permissionsState) { prefs, perms ->
        prefs.copy(
            hasOverlayPermission = perms.hasOverlayPermission,
            hasUsageStatsPermission = perms.hasUsageStatsPermission,
            hasAccessibilityEnabled = perms.hasAccessibilityEnabled,
            showGrayscaleGuide = perms.showGrayscaleGuide
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    fun refreshPermissions() {
        viewModelScope.launch {
            grayscaleManager.syncPreferenceWithSystem()
        }
        _permissionsState.update {
            it.copy(
                hasOverlayPermission = checkOverlayPermission(),
                hasUsageStatsPermission = checkUsageStatsPermission(),
                hasAccessibilityEnabled = checkAccessibilityService()
            )
        }
    }

    fun setFocusDuration(minutes: Int) {
        viewModelScope.launch { preferences.setFocusDuration(minutes) }
    }

    fun setBreakDuration(minutes: Int) {
        viewModelScope.launch { preferences.setBreakDuration(minutes) }
    }

    fun toggleGrayscale(enabled: Boolean) {
        viewModelScope.launch {
            val applied = grayscaleManager.toggle(enabled)
            if (applied) {
                preferences.setGrayscaleEnabled(enabled)
            } else if (enabled) {
                _permissionsState.update { it.copy(showGrayscaleGuide = true) }
            } else {
                _permissionsState.update { it.copy(showGrayscaleGuide = true) }
            }
        }
    }

    fun dismissGrayscaleGuide() {
        _permissionsState.update { it.copy(showGrayscaleGuide = false) }
    }

    fun openColorCorrectionSettings(context: Context) {
        grayscaleManager.openColorCorrectionSettings(context)
    }

    fun toggleBlocking(enabled: Boolean) {
        viewModelScope.launch { preferences.setBlockingEnabled(enabled) }
    }

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch { preferences.setNotificationsEnabled(enabled) }
    }

    fun toggleBlockShorts(enabled: Boolean) {
        viewModelScope.launch { preferences.setBlockShortsEnabled(enabled) }
    }

    private fun checkOverlayPermission(): Boolean {
        return Settings.canDrawOverlays(context)
    }

    private fun checkUsageStatsPermission(): Boolean {
        val appOps = context.getSystemService(android.app.AppOpsManager::class.java)
        val mode = appOps.unsafeCheckOpNoThrow(
            android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
        return mode == android.app.AppOpsManager.MODE_ALLOWED
    }

    private fun checkAccessibilityService(): Boolean {
        val enabled = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabled.contains("${context.packageName}/com.foccus.app.service.BlockerAccessibilityService")
    }
}
