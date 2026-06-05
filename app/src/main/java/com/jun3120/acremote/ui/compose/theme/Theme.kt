package com.jun3120.acremote.ui.compose.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color

object ThemeManager {
    private const val KEY = "dark_mode"
    private val prefs by lazy { com.jun3120.acremote.App.instance.getSharedPreferences("theme", 0) }
    var isDark: Boolean
        get() = prefs.getBoolean(KEY, false)
        set(value) {
            prefs.edit().putBoolean(KEY, value).apply()
            CurrentColors.colors = if (value) DarkAppColors else LightAppColors
        }
}

@Composable
fun AcRemoteTheme(content: @Composable () -> Unit) {
    val dark = ThemeManager.isDark
    CurrentColors.colors = if (dark) DarkAppColors else LightAppColors
    val c = CurrentColors.colors
    val scheme = if (dark) darkColorScheme(
        primary = c.Primary, onPrimary = Color.Black,
        primaryContainer = c.PrimaryContainer, onPrimaryContainer = c.OnPrimaryContainer,
        secondary = c.Cyan700, surface = c.Surface, onSurface = c.OnSurface,
        onSurfaceVariant = c.OnSurfaceVariant, outline = c.Outline,
        surfaceContainerLow = c.SurfaceLow, surfaceContainerHigh = c.SurfaceHigh,
        surfaceContainerHighest = c.SurfaceLowest,
    ) else lightColorScheme(
        primary = c.Primary, onPrimary = Color.White,
        primaryContainer = c.PrimaryContainer, onPrimaryContainer = c.OnPrimaryContainer,
        secondary = c.Cyan700, surface = c.Surface, onSurface = c.OnSurface,
        onSurfaceVariant = c.OnSurfaceVariant, outline = c.Outline,
        surfaceContainerLow = c.SurfaceLow, surfaceContainerHigh = c.SurfaceHigh,
        surfaceContainerHighest = c.SurfaceLowest,
    )
    MaterialTheme(colorScheme = scheme, content = content)
}
