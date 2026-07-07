package com.example.noduckingway

import android.content.Intent
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.core.content.ContextCompat

class NoDuckingTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()
        val running = NoDuckingService.isRunning(this)
        val intent = Intent(this, NoDuckingService::class.java)
        if (running) {
            stopService(intent)
        } else {
            ContextCompat.startForegroundService(this, intent)
        }
        updateTileState()
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        val active = NoDuckingService.isRunning(this)
        tile.state = if (active) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = if (active) "NoDucking ON" else "NoDucking OFF"
        tile.contentDescription = if (active) {
            "No ducking mode is on"
        } else {
            "No ducking mode is off"
        }
        tile.updateTile()
    }
}