package com.yourname.audiobalance

import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {

    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        prefs = getSharedPreferences("audio_balance", MODE_PRIVATE)

        setContent {
            AudioBalanceTheme {
                AudioBalanceScreen(
                    prefs = prefs,
                    onApplyBalance = { left, right -> applyBalance(left, right) },
                    onReset = { resetBalance() },
                    hasPermission = hasWriteSecurePermission()
                )
            }
        }
    }

    private fun hasWriteSecurePermission(): Boolean {
        return try {
            Settings.System.putFloat(contentResolver, "master_balance_test", 0f)
            Settings.System.putFloat(contentResolver, "master_balance", 0f)
            true
        } catch (e: SecurityException) {
            false
        }
    }

    private fun applyBalance(leftPercent: Int, rightPercent: Int) {
        try {
            val total = leftPercent + rightPercent
            val balanceValue = if (total == 0) 0f
            else (rightPercent.toFloat() / total.toFloat()) * 2f - 1f
            // -1.0 = full kiri, 0.0 = tengah, 1.0 = full kanan

            Settings.System.putFloat(contentResolver, "master_balance", balanceValue)

            prefs.edit()
                .putInt("left", leftPercent)
                .putInt("right", rightPercent)
                .putBoolean("enabled", true)
                .apply()

            Toast.makeText(this, "Balance diterapkan: L$leftPercent% R$rightPercent%", Toast.LENGTH_SHORT).show()

        } catch (e: SecurityException) {
            Toast.makeText(this, "Permission belum diberikan! Jalankan perintah ADB.", Toast.LENGTH_LONG).show()
        }
    }

    private fun resetBalance() {
        try {
            Settings.System.putFloat(contentResolver, "master_balance", 0f)
            prefs.edit().putBoolean("enabled", false).apply()
            Toast.makeText(this, "Balance direset ke tengah (50/50)", Toast.LENGTH_SHORT).show()
        } catch (e: SecurityException) {
            Toast.makeText(this, "Permission belum diberikan!", Toast.LENGTH_LONG).show()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioBalanceScreen(
    prefs: SharedPreferences,
    onApplyBalance: (Int, Int) -> Unit,
    onReset: () -> Unit,
    hasPermission: Boolean
) {
    val savedLeft = prefs.getInt("left", 60)
    val savedRight = prefs.getInt("right", 40)
    val savedEnabled = prefs.getBoolean("enabled", false)

    // Slider 0..100 → 0 = full kiri, 100 = full kanan, 50 = tengah
    val initialSlider = if (savedEnabled)
        savedRight.toFloat() / (savedLeft + savedRight).toFloat() * 100f
    else 50f

    var sliderValue by remember { mutableFloatStateOf(initialSlider) }
    var isEnabled by remember { mutableStateOf(savedEnabled) }

    val leftPercent = (100 - sliderValue).roundToInt()
    val rightPercent = sliderValue.roundToInt()

    val containerColor by animateColorAsState(
        targetValue = if (isEnabled)
            MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceVariant,
        label = "containerColor"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Audio Balance",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── CARD STATUS ON/OFF ──────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = containerColor),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = if (isEnabled) Icons.Rounded.VolumeUp else Icons.Rounded.VolumeOff,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                text = if (isEnabled) "Balance Aktif" else "Balance Nonaktif",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (isEnabled) "L: $leftPercent%  |  R: $rightPercent%"
                                else "Default 50% / 50%",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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

            // ── CARD SLIDER ─────────────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text(
                        "Keseimbangan Audio",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Visual bar L/R
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Bar Kiri
                        Box(
                            modifier = Modifier
                                .weight(leftPercent.toFloat().coerceAtLeast(1f))
                                .height(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (leftPercent > rightPercent)
                                        MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "L $leftPercent%",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (leftPercent > rightPercent)
                                    MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Bar Kanan
                        Box(
                            modifier = Modifier
                                .weight(rightPercent.toFloat().coerceAtLeast(1f))
                                .height(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (rightPercent > leftPercent)
                                        MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "R $rightPercent%",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (rightPercent > leftPercent)
                                    MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Slider
                    Slider(
                        value = sliderValue,
                        onValueChange = { sliderValue = it },
                        valueRange = 0f..100f,
                        steps = 0,
                        onValueChangeFinished = {
                            if (isEnabled) onApplyBalance(leftPercent, rightPercent)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("◄ Kiri", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Kanan ►", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // ── PRESET BUTTONS ───────────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Preset Cepat",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        data class Preset(val label: String, val sliderVal: Float)
                        val presets = listOf(
                            Preset("Kiri +30%", 35f),
                            Preset("Kiri +20%", 40f),
                            Preset("Tengah", 50f),
                            Preset("Kanan +20%", 60f),
                            Preset("Kanan +30%", 65f)
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                presets.take(3).forEach { preset ->
                                    OutlinedButton(
                                        onClick = {
                                            sliderValue = preset.sliderVal
                                            val newRight = preset.sliderVal.roundToInt()
                                            val newLeft = 100 - newRight
                                            if (isEnabled) onApplyBalance(newLeft, newRight)
                                        },
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            preset.label,
                                            style = MaterialTheme.typography.labelSmall,
                                            textAlign = TextAlign.Center,
                                            maxLines = 2
                                        )
                                    }
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                presets.drop(3).forEach { preset ->
                                    OutlinedButton(
                                        onClick = {
                                            sliderValue = preset.sliderVal
                                            val newRight = preset.sliderVal.roundToInt()
                                            val newLeft = 100 - newRight
                                            if (isEnabled) onApplyBalance(newLeft, newRight)
                                        },
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            preset.label,
                                            style = MaterialTheme.typography.labelSmall,
                                            textAlign = TextAlign.Center,
                                            maxLines = 2
                                        )
                                    }
                                }
                                // Spacer agar 2 tombol tidak full width
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // ── INFO CONTROL CENTER ──────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Rounded.Notifications,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Column {
                        Text(
                            "Tambah ke Control Center",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Turunkan notifikasi → Edit panel cepat → Cari \"Audio Balance\" → Drag ke panel aktif",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            // ── WARNING PERMISSION ───────────────────────────────────────────
            if (!hasPermission) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Rounded.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                "Permission Diperlukan",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        Text(
                            "Jalankan perintah ADB ini satu kali dari PC:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "adb shell pm grant com.yourname.audiobalance android.permission.WRITE_SECURE_SETTINGS",
                                modifier = Modifier.padding(10.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        Text(
                            "Atau gunakan Shizuku tanpa PC.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}