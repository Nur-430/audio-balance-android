package com.yourname.audiobalance

import android.content.Context
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

/**
 * Quick Settings Tile yang muncul di Control Center / Panel Cepat.
 * Ketuk tile untuk toggle ON/OFF balance audio.
 */
class BalanceTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        val prefs = getSharedPreferences("audio_balance", Context.MODE_PRIVATE)
        val isEnabled = prefs.getBoolean("enabled", false)
        val left = prefs.getInt("left", 60)
        val right = prefs.getInt("right", 40)
        updateTileState(isEnabled, left, right)
    }

    override fun onStopListening() {
        super.onStopListening()
    }

    override fun onClick() {
        super.onClick()
        val prefs = getSharedPreferences("audio_balance", Context.MODE_PRIVATE)
        val isEnabled = prefs.getBoolean("enabled", false)
        val left = prefs.getInt("left", 60)
        val right = prefs.getInt("right", 40)

        try {
            if (isEnabled) {
                // Matikan → reset ke tengah
                Settings.System.putFloat(contentResolver, "master_balance", 0f)
                prefs.edit().putBoolean("enabled", false).apply()
                updateTileState(false, 50, 50)
            } else {
                // Nyalakan → pakai setting terakhir
                val total = left + right
                val balanceValue = if (total == 0) 0f
                else (right.toFloat() / total.toFloat()) * 2f - 1f

                Settings.System.putFloat(contentResolver, "master_balance", balanceValue)
                prefs.edit().putBoolean("enabled", true).apply()
                updateTileState(true, left, right)
            }
        } catch (e: SecurityException) {
            // Permission belum di-grant
            updateTileState(false, 50, 50)
        }
    }

    private fun updateTileState(isEnabled: Boolean, left: Int, right: Int) {
        qsTile?.apply {
            state = if (isEnabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            label = "Audio Balance"
            subtitle = if (isEnabled) "L:$left R:$right" else "Off"
            updateTile()
        }
    }
}