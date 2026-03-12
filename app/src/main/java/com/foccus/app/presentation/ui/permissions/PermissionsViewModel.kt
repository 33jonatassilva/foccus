package com.foccus.app.presentation.ui.permissions

import android.content.Context
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foccus.app.data.local.preferences.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PermissionsUiState(
    val hasOverlayPermission: Boolean = false,
    val hasUsageStatsPermission: Boolean = false,
    val hasAccessibilityEnabled: Boolean = false
)

@HiltViewModel
class PermissionsViewModel @Inject constructor(
    private val preferences: UserPreferences,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        PermissionsUiState(
            hasOverlayPermission = checkOverlayPermission(),
            hasUsageStatsPermission = checkUsageStatsPermission(),
            hasAccessibilityEnabled = checkAccessibilityService()
        )
    )
    val uiState: StateFlow<PermissionsUiState> = _uiState.asStateFlow()

    fun refreshPermissions() {
        _uiState.update {
            PermissionsUiState(
                hasOverlayPermission = checkOverlayPermission(),
                hasUsageStatsPermission = checkUsageStatsPermission(),
                hasAccessibilityEnabled = checkAccessibilityService()
            )
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            preferences.setOnboardingCompleted(true)
        }
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
