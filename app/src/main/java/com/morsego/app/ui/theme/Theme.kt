package com.morsego.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val MorseDarkColorScheme = darkColorScheme(
    primary = MorseAmber,
    onPrimary = RadioDark,
    primaryContainer = MorseAmberDark,
    onPrimaryContainer = TextPrimary,
    secondary = MorseCyan,
    onSecondary = RadioDark,
    secondaryContainer = MorseCyanDark,
    onSecondaryContainer = TextPrimary,
    tertiary = MorseGreen,
    background = RadioDark,
    onBackground = TextPrimary,
    surface = RadioSurface,
    onSurface = TextPrimary,
    surfaceVariant = RadioSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = RadioBorder
)

@Composable
fun MorseGOTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = RadioDark.toArgb()
            window.navigationBarColor = RadioDark.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = MorseDarkColorScheme,
        typography = Typography,
        content = content
    )
}
