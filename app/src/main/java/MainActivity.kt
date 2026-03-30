package com.yourname.audiobalance

import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {

    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = getSharedPreferences("audio_balance", MODE_PRIVATE)

        setContent {
            AudioBalanceTheme {
                AudioBalanceScreen(
                    onApplyBalance = { left, right -> applyBalance(left, right) },
                    onReset = { resetBalance() },
                    prefs = prefs
                )
            }
        }
    }

    // Nilai balance: 0.0 (full kiri) sampai 1.0 (full kanan), 0.5 = tengah
    private fun applyBalance(leftPercent: Int, rightPercent: Int) {
        try {
            // Konversi ke format Android (0-1 float)
            // Kiri dominan = nilai < 0.5, kanan dominan = nilai > 0.5
            val balanceValue = rightPercent.toFloat() / (leftPercent + rightPercent).toFloat()

            Settings.System.putFloat(
                contentResolver,
                "master_balance", // key aksesibilitas Android
                (balanceValue * 2f) - 1f // -1.0 full kiri, 0.0 tengah, 1.0 full kanan
            )

            // Simpan state
            prefs.edit()
                .putInt("left", leftPercent)
                .putInt("right", rightPercent)
                .putBoolean("enabled", true)
                .apply()

            // Update tile
            BalanceTileService.updateTile(this, true)

        } catch (e: SecurityException) {
            // Perlu grant permission via ADB
        }
    }

    private fun resetBalance() {
        Settings.System.putFloat(contentResolver, "master_balance", 0f)
        prefs.edit().putBoolean("enabled", false).apply()
        BalanceTileService.updateTile(this, false)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioBalanceScreen(
    onApplyBalance: (Int, Int) -> Unit,
    onReset: () -> Unit,
    prefs: SharedPreferences
) {
    val savedLeft = prefs.getInt("left", 60)
    val savedRight = prefs.getInt("right", 40)
    val savedEnabled = prefs.getBoolean("enabled", false)

    // Slider: 0 = full kiri, 100 = full kanan, 50 = tengah
    var sliderValue by remember { mutableFloatStateOf(
        if (savedEnabled) savedRight.toFloat() / (savedLeft + savedRight) * 100f
        else 50f
    )}
    var isEnabled by remember { mutableStateOf(savedEnabled) }

    val leftPercent = (100 - sliderValue).roundToInt()
    val rightPercent = sliderValue.roundToInt()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Audio Balance") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            Spacer(modifier = Modifier.height(16.dp))

            // Toggle ON/OFF
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isEnabled)
                        MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Balance Kustom",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            if (isEnabled) "Aktif" else "Nonaktif (50/50)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { enabled ->
                            isEnabled = enabled
                            if (enabled) {
                                onApplyBalance(leftPercent, rightPercent)
                            } else {
                                onReset()
                            }
                        }
                    )
                }
            }

            // Slider Balance
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Keseimbangan Audio",
                        style = MaterialTheme.typography.titleMedium
                    )

                    // Indikator L/R
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Kiri: $leftPercent%",
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (leftPercent > rightPercent)
                                MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Kanan: $rightPercent%",
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (rightPercent > leftPercent)
                                MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Slider(
                        value = sliderValue,
                        onValueChange = { sliderValue = it },
                        valueRange = 0f..100f,
                        onValueChangeFinished = {
                            if (isEnabled) {
                                onApplyBalance(leftPercent, rightPercent)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("◄ Kiri", style = MaterialTheme.typography.labelSmall)
                        Text("Kanan ►", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            // Preset Buttons
            Text("Preset Cepat", style = MaterialTheme.typography.labelLarge)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "Kiri +20%" to 20f,
                    "Tengah" to 50f,
                    "Kanan +20%" to 80f
                ).forEach { (label, value) ->
                    OutlinedButton(
                        onClick = {
                            sliderValue = value
                            if (isEnabled) onApplyBalance(
                                (100 - value).roundToInt(),
                                value.roundToInt()
                            )
                        }
                    ) {
                        Text(label, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            // Info ADB
            if (!hasWriteSecurePermission()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "⚠️ Permission Diperlukan",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Jalankan perintah ADB ini:\nadb shell pm grant com.yourname.audiobalance android.permission.WRITE_SECURE_SETTINGS",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}