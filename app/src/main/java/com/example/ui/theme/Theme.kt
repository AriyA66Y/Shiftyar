package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

private val DarkColorScheme = darkColorScheme(
    primary = MedicalTealPrimaryDark,
    onPrimary = MedicalTealOnPrimaryDark,
    primaryContainer = MedicalTealPrimaryContainerDark,
    onPrimaryContainer = MedicalTealOnPrimaryContainerDark,
    secondary = MedicalSecondaryDark,
    secondaryContainer = MedicalSecondaryContainerDark,
    tertiary = MedicalTertiaryDark,
    tertiaryContainer = MedicalTertiaryContainerDark,
    background = MedicalBackgroundDark,
    surface = MedicalSurfaceDark,
    surfaceVariant = MedicalSurfaceVariantDark
)

private val LightColorScheme = lightColorScheme(
    primary = MedicalTealPrimary,
    onPrimary = MedicalTealOnPrimary,
    primaryContainer = MedicalTealPrimaryContainerContainer(MedicalTealPrimaryContainer),
    onPrimaryContainer = MedicalTealOnPrimaryContainer,
    secondary = MedicalSecondary,
    secondaryContainer = MedicalSecondaryContainer,
    tertiary = MedicalTertiary,
    tertiaryContainer = MedicalTertiaryContainer,
    background = MedicalBackground,
    surface = MedicalSurface,
    surfaceVariant = MedicalSurfaceVariant
)

private fun MedicalTealPrimaryContainerContainer(color: androidx.compose.ui.graphics.Color) = color

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
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

    // Wrap the app in Right-to-Left (RTL) layout direction since the user requested Persian (فارسی)
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
