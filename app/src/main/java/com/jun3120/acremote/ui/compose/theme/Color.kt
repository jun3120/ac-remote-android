package com.jun3120.acremote.ui.compose.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

@Immutable
data class AppColors(
    val Primary: Color, val PrimaryActive: Color,
    val Surface: Color, val SurfaceLowest: Color, val SurfaceLow: Color, val SurfaceHigh: Color,
    val OnSurface: Color, val OnSurfaceVariant: Color, val Outline: Color,
    val PrimaryContainer: Color, val OnPrimaryContainer: Color,
    val Blue100: Color, val Cyan100: Color, val Cyan700: Color,
    val Gray100: Color, val Gray500: Color, val Orange100: Color, val Orange600: Color,
    val Blue50: Color, val Green500: Color, val TestRemoteGradientEnd: Color,
)

val LightAppColors = AppColors(
    Primary = Color(0xFF0058BC), PrimaryActive = Color(0xFF004BA0),
    Surface = Color(0xFFFAF9FE), SurfaceLowest = Color(0xFFFFFFFF),
    SurfaceLow = Color(0xFFF4F3F8), SurfaceHigh = Color(0xFFE9E7ED),
    OnSurface = Color(0xFF1A1B1F), OnSurfaceVariant = Color(0xFF414755),
    Outline = Color(0xFFC1C6D7), PrimaryContainer = Color(0xFFD8E2FF),
    OnPrimaryContainer = Color(0xFF001A41), Blue100 = Color(0xFFDBEAFE),
    Cyan100 = Color(0xFFCFFAFE), Cyan700 = Color(0xFF0E7490),
    Gray100 = Color(0xFFF3F4F6), Gray500 = Color(0xFF6B7280),
    Orange100 = Color(0xFFFFEDD5), Orange600 = Color(0xFFEA580C),
    Blue50 = Color(0xFFEFF6FF), Green500 = Color(0xFF22C55E),
    TestRemoteGradientEnd = Color(0x80EFF6FF),
)

val DarkAppColors = AppColors(
    Primary = Color(0xFFADC6FF), PrimaryActive = Color(0xFFC8D9FF),
    Surface = Color(0xFF111318), SurfaceLowest = Color(0xFF1A1C20),
    SurfaceLow = Color(0xFF25272D), SurfaceHigh = Color(0xFF2F3137),
    OnSurface = Color(0xFFE2E3E8), OnSurfaceVariant = Color(0xFFC1C6D7),
    Outline = Color(0xFF8B909A), PrimaryContainer = Color(0xFF1E3A6E),
    OnPrimaryContainer = Color(0xFFD8E2FF), Blue100 = Color(0xFF1E3058),
    Cyan100 = Color(0xFF1A3038), Cyan700 = Color(0xFF80D4F5),
    Gray100 = Color(0xFF2A2D35), Gray500 = Color(0xFF9B9FAA),
    Orange100 = Color(0xFF3A2818), Orange600 = Color(0xFFFFB876),
    Blue50 = Color(0xFF1A2840), Green500 = Color(0xFF4ADE80),
    TestRemoteGradientEnd = Color(0x80111318),
)

val LocalAppColors = compositionLocalOf { LightAppColors }

object CurrentColors {
    var colors: AppColors by mutableStateOf(LightAppColors)
}

// Top-level getters - all screens use these directly
val Primary: Color get() = CurrentColors.colors.Primary
val PrimaryActive: Color get() = CurrentColors.colors.PrimaryActive
val Surface: Color get() = CurrentColors.colors.Surface
val SurfaceLowest: Color get() = CurrentColors.colors.SurfaceLowest
val SurfaceLow: Color get() = CurrentColors.colors.SurfaceLow
val SurfaceHigh: Color get() = CurrentColors.colors.SurfaceHigh
val OnSurface: Color get() = CurrentColors.colors.OnSurface
val OnSurfaceVariant: Color get() = CurrentColors.colors.OnSurfaceVariant
val Outline: Color get() = CurrentColors.colors.Outline
val PrimaryContainer: Color get() = CurrentColors.colors.PrimaryContainer
val OnPrimaryContainer: Color get() = CurrentColors.colors.OnPrimaryContainer
val Blue100: Color get() = CurrentColors.colors.Blue100
val Cyan100: Color get() = CurrentColors.colors.Cyan100
val Cyan700: Color get() = CurrentColors.colors.Cyan700
val Gray100: Color get() = CurrentColors.colors.Gray100
val Gray500: Color get() = CurrentColors.colors.Gray500
val Orange100: Color get() = CurrentColors.colors.Orange100
val Orange600: Color get() = CurrentColors.colors.Orange600
val Blue50: Color get() = CurrentColors.colors.Blue50
val Green500: Color get() = CurrentColors.colors.Green500
val TestRemoteGradientEnd: Color get() = CurrentColors.colors.TestRemoteGradientEnd
