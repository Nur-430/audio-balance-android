package com.yourname.audiobalance

import android.content.Context
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.provider.Settings

class BalanceTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        val prefs = getSharedPreferences("audio_balance", Context.MODE_PRIVATE)
        val isEnabled = prefs.getBoolean("enabled", false)
        updateTileState(isEnabled)
    }

    override fun onClick() {
        super.onClick()
        val prefs = getSharedPreferences("audio_balance", Context.MODE_PRIVATE)
        val isEnabled = prefs.getBoolean("enabled", false)

        if (isEnabled) {
            // Matikan -> reset ke tengah
            Settings.System.putFloat(contentResolver, "master_balance", 0f)
            prefs.edit().putBoolean("enabled", false).apply()
            updateTileState(false)
        } else {
            // Nyalakan -> pakai setting terakhir
            val left = prefs.getInt("left", 60)
            val right = prefs.getInt("right", 40)
            val balance = right.toFloat() / (left + right).toFloat()
            Settings.System.putFloat(
                contentResolver,
                "master_balance",
                (balance * 2f) - 1f
            )
            prefs.edit().putBoolean("enabled", true).apply()
            updateTileState(true)
        }
    }

    private fun updateTileState(isEnabled: Boolean) {
        qsTile?.apply {
            state = if (isEnabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            label = if (isEnabled) "Balance ON" else "Balance OFF"
            updateTile()
        }
    }

    companion object {
        fun updateTile(context: Context, enabled: Boolean) {
            // Tile akan update saat onStartListening dipanggil
        }
    }
}