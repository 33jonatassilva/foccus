package com.foccus.app.data.grayscale

import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.foccus.app.data.local.preferences.UserPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GrayscaleManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferences: UserPreferences
) {

    fun canControlDirectly(): Boolean {
        return context.checkCallingOrSelfPermission(
            android.Manifest.permission.WRITE_SECURE_SETTINGS
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    fun isSystemGrayscaleActive(): Boolean {
        return try {
            val cr = context.contentResolver
            val enabled = Settings.Secure.getInt(cr, "accessibility_display_daltonizer_enabled", 0)
            val mode = Settings.Secure.getInt(cr, "accessibility_display_daltonizer", -1)
            enabled == 1 && mode == 0
        } catch (_: Exception) {
            false
        }
    }

    fun toggle(enabled: Boolean): Boolean {
        if (canControlDirectly()) {
            applySystemGrayscale(enabled)
            return true
        }
        return false
    }

    private fun applySystemGrayscale(enabled: Boolean) {
        try {
            val cr = context.contentResolver
            if (enabled) {
                Settings.Secure.putInt(cr, "accessibility_display_daltonizer_enabled", 1)
                Settings.Secure.putInt(cr, "accessibility_display_daltonizer", 0)
            } else {
                Settings.Secure.putInt(cr, "accessibility_display_daltonizer_enabled", 0)
            }
        } catch (_: SecurityException) { }
    }

    suspend fun syncPreferenceWithSystem() {
        val systemActive = isSystemGrayscaleActive()
        val prefEnabled = preferences.grayscaleEnabled.first()
        if (systemActive != prefEnabled) {
            preferences.setGrayscaleEnabled(systemActive)
        }
    }

    suspend fun reapplyGrayscaleIfEnabled() {
        val enabled = preferences.grayscaleEnabled.first()
        if (enabled && canControlDirectly()) {
            applySystemGrayscale(true)
        }
    }

    fun openColorCorrectionSettings(context: Context = this.context) {
        try {
            val intent = Intent("android.settings.ACCESSIBILITY_COLOR_SPACE_SETTINGS")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        } catch (_: Exception) {
            try {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            } catch (_: Exception) { }
        }
    }
}
