package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val HisabLightColorScheme = lightColorScheme(
    primary = BrandDark,
    onPrimary = SurfaceWhite,
    primaryContainer = SurfaceSubtle,
    onPrimaryContainer = BrandDark,
    secondary = ProfitEmerald,
    onSecondary = SurfaceWhite,
    secondaryContainer = ProfitEmeraldBg,
    onSecondaryContainer = ProfitEmerald,
    tertiary = RevenueSapphire,
    onTertiary = SurfaceWhite,
    tertiaryContainer = RevenueSapphireBg,
    onTertiaryContainer = RevenueSapphire,
    background = BackgroundLight,
    onBackground = TextPrimary,
    surface = SurfaceWhite,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceSubtle,
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle,
    outlineVariant = BorderSubtle
)

private val HisabDarkColorScheme = darkColorScheme(
    primary = SurfaceWhite,
    onPrimary = BrandDark,
    primaryContainer = BrandNavy,
    onPrimaryContainer = SurfaceWhite,
    secondary = ProfitEmeraldBright,
    onSecondary = BrandDark,
    secondaryContainer = BrandDark,
    onSecondaryContainer = ProfitEmeraldBright,
    tertiary = RevenueSapphireBright,
    onTertiary = SurfaceWhite,
    background = BrandDark,
    onBackground = SurfaceWhite,
    surface = Color(0xFF132238),
    onSurface = SurfaceWhite,
    surfaceVariant = Color(0xFF1E2E45),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF334155)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Default to stunning light theme requested by user
    dynamicColor: Boolean = false, // Keep cohesive custom brand aesthetic
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) HisabDarkColorScheme else HisabLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
