package com.yourname.audiobalance

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF0061A4),
    onPrimary = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
    primaryContainer = androidx.compose.ui.graphics.Color(0xFFD1E4FF),
    onPrimaryContainer = androidx.compose.ui.graphics.Color(0xFF001D36),
    secondary = androidx.compose.ui.graphics.Color(0xFF535F70),
    onSecondary = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
    secondaryContainer = androidx.compose.ui.graphics.Color(0xFFD7E3F7),
    onSecondaryContainer = androidx.compose.ui.graphics.Color(0xFF101C2B),
    surface = androidx.compose.ui.graphics.Color(0xFFFAF9FF),
    onSurface = androidx.compose.ui.graphics.Color(0xFF1A1C1E),
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFFDFE2EB),
    onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFF43474E),
)

private val DarkColors = darkColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF9ECAFF),
    onPrimary = androidx.compose.ui.graphics.Color(0xFF003258),
    primaryContainer = androidx.compose.ui.graphics.Color(0xFF00497D),
    onPrimaryContainer = androidx.compose.ui.graphics.Color(0xFFD1E4FF),
    secondary = androidx.compose.ui.graphics.Color(0xFFBBC7DB),
    onSecondary = androidx.compose.ui.graphics.Color(0xFF253140),
    secondaryContainer = androidx.compose.ui.graphics.Color(0xFF3B4858),
    onSecondaryContainer = androidx.compose.ui.graphics.Color(0xFFD7E3F7),
    surface = androidx.compose.ui.graphics.Color(0xFF111318),
    onSurface = androidx.compose.ui.graphics.Color(0xFFE2E2E9),
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFF43474E),
    onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFFC3C7CF),
)

@Composable
fun AudioBalanceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // Dynamic color (Material You) di Android 12+
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}