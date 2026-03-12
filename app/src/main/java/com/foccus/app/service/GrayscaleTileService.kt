package com.foccus.app.service

import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.foccus.app.data.grayscale.GrayscaleManager
import com.foccus.app.data.local.preferences.UserPreferences
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@AndroidEntryPoint
class GrayscaleTileService : TileService() {

    @Inject
    lateinit var preferences: UserPreferences

    @Inject
    lateinit var grayscaleManager: GrayscaleManager

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onStartListening() {
        super.onStartListening()
        updateTile()
    }

    override fun onClick() {
        super.onClick()
        scope.launch {
            val current = preferences.grayscaleEnabled.first()
            val newState = !current
            preferences.setGrayscaleEnabled(newState)
            val applied = grayscaleManager.toggle(newState)
            if (!applied) {
                grayscaleManager.openColorCorrectionSettings()
            }
            updateTile()
        }
    }

    private fun updateTile() {
        scope.launch {
            val enabled = preferences.grayscaleEnabled.first()
            val tile = qsTile ?: return@launch
            tile.state = if (enabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            tile.label = "Escala de cinza"
            tile.subtitle = if (enabled) "Ativo" else "Inativo"
            tile.icon = Icon.createWithResource(
                this@GrayscaleTileService,
                android.R.drawable.ic_menu_view
            )
            tile.updateTile()
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
