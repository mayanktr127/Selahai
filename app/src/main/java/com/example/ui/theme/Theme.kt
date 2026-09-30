package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Ember,
    onPrimary = Color.White,
    primaryContainer = Dawn100,
    onPrimaryContainer = EmberDeep,
    secondary = Sky400,
    onSecondary = Color.White,
    secondaryContainer = Sky100,
    onSecondaryContainer = Ink,
    tertiary = RoseWash,
    onTertiary = Ink,
    background = Dawn50,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = Dawn100,
    onSurfaceVariant = InkSoft,
    outline = GlassBorderLight
)

private val DarkColorScheme = darkColorScheme(
    primary = Ember,
    onPrimary = Color.White,
    primaryContainer = NightIndigo,
    onPrimaryContainer = Dawn100,
    secondary = Sky400,
    onSecondary = Color.White,
    secondaryContainer = NightSurface,
    onSecondaryContainer = Sky100,
    tertiary = LavenderWash,
    onTertiary = Ink,
    background = NightBase,
    onBackground = NightText,
    surface = NightSurface,
    onSurface = NightText,
    surfaceVariant = NightIndigo,
    onSurfaceVariant = NightTextSoft,
    outline = NightGlassBorder
)

@Composable
fun SelahTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SelahTypography,
        content = content
    )
}
