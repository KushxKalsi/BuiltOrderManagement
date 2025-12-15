package com.king.builtordermanagement.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Custom Colors
val PrimaryColor = Color(0xFF6C63FF)
val PrimaryVariant = Color(0xFF5A52E0)
val SecondaryColor = Color(0xFFFF6B6B)
val AccentColor = Color(0xFF4ECDC4)
val BackgroundLight = Color(0xFFF8F9FA)
val SurfaceLight = Color(0xFFFFFFFF)
val OnSurfaceLight = Color(0xFF1A1A2E)
val TextPrimary = Color(0xFF1A1A2E)
val TextSecondary = Color(0xFF6B7280)
val SuccessColor = Color(0xFF10B981)
val WarningColor = Color(0xFFF59E0B)
val ErrorColor = Color(0xFFEF4444)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryColor,
    onPrimary = Color.White,
    primaryContainer = PrimaryColor.copy(alpha = 0.1f),
    onPrimaryContainer = PrimaryColor,
    secondary = SecondaryColor,
    onSecondary = Color.White,
    secondaryContainer = SecondaryColor.copy(alpha = 0.1f),
    onSecondaryContainer = SecondaryColor,
    tertiary = AccentColor,
    onTertiary = Color.White,
    background = BackgroundLight,
    onBackground = TextPrimary,
    surface = SurfaceLight,
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = TextSecondary,
    error = ErrorColor,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryColor,
    onPrimary = Color.White,
    primaryContainer = PrimaryColor.copy(alpha = 0.2f),
    onPrimaryContainer = PrimaryColor,
    secondary = SecondaryColor,
    onSecondary = Color.White,
    secondaryContainer = SecondaryColor.copy(alpha = 0.2f),
    onSecondaryContainer = SecondaryColor,
    tertiary = AccentColor,
    onTertiary = Color.White,
    background = Color(0xFF0F0F23),
    onBackground = Color.White,
    surface = Color(0xFF1A1A2E),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF2D2D44),
    onSurfaceVariant = Color(0xFFB0B0C0),
    error = ErrorColor,
    onError = Color.White
)

@Composable
fun StoreAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
