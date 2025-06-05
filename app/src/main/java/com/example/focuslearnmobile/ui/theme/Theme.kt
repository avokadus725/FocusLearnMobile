// app/src/main/java/com/example/focuslearnmobile/ui/theme/Theme.kt
package com.example.focuslearnmobile.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Світла тема FocusLearn
private val FocusLearnLightColorScheme = lightColorScheme(
    // Primary colors
    primary = FocusGreen400,
    onPrimary = White,
    primaryContainer = FocusGreen100,
    onPrimaryContainer = FocusGreen800,

    // Secondary colors
    secondary = FocusMint400,
    onSecondary = Neutral800,
    secondaryContainer = FocusMint100,
    onSecondaryContainer = Neutral800,

    // Tertiary colors
    tertiary = FocusGreen600,
    onTertiary = White,
    tertiaryContainer = FocusGreen200,
    onTertiaryContainer = FocusGreen900,

    // Error colors
    error = ErrorColor,
    onError = White,
    errorContainer = ErrorBackground,
    onErrorContainer = ErrorText,

    // Background colors
    background = FocusGreen50,
    onBackground = Neutral800,
    surface = White,
    onSurface = Neutral800,
    surfaceVariant = Neutral100,
    onSurfaceVariant = Neutral600,

    // Outline colors
    outline = Neutral300,
    outlineVariant = Neutral200,
    scrim = Black,
    inverseSurface = Neutral800,
    inverseOnSurface = White,
    inversePrimary = FocusGreen200,

    // Surface tones
    surfaceDim = Neutral100,
    surfaceBright = White,
    surfaceContainerLowest = White,
    surfaceContainerLow = Neutral50,
    surfaceContainer = Neutral100,
    surfaceContainerHigh = Neutral200,
    surfaceContainerHighest = Neutral300
)

// Темна тема FocusLearn
private val FocusLearnDarkColorScheme = darkColorScheme(
    // Primary colors
    primary = FocusGreen300,
    onPrimary = FocusGreen900,
    primaryContainer = FocusGreen700,
    onPrimaryContainer = FocusGreen100,

    // Secondary colors
    secondary = FocusMint300,
    onSecondary = Neutral800,
    secondaryContainer = FocusGreen800,
    onSecondaryContainer = FocusMint100,

    // Tertiary colors
    tertiary = FocusGreen400,
    onTertiary = FocusGreen900,
    tertiaryContainer = FocusGreen800,
    onTertiaryContainer = FocusGreen200,

    // Error colors
    error = androidx.compose.ui.graphics.Color(0xFFFFB4AB),
    onError = androidx.compose.ui.graphics.Color(0xFF690005),
    errorContainer = androidx.compose.ui.graphics.Color(0xFF93000A),
    onErrorContainer = androidx.compose.ui.graphics.Color(0xFFFFDAD6),

    // Background colors
    background = androidx.compose.ui.graphics.Color(0xFF10140F),
    onBackground = Neutral200,
    surface = androidx.compose.ui.graphics.Color(0xFF10140F),
    onSurface = Neutral200,
    surfaceVariant = Neutral700,
    onSurfaceVariant = Neutral400,

    // Outline colors
    outline = Neutral600,
    outlineVariant = Neutral700,
    scrim = Black,
    inverseSurface = Neutral200,
    inverseOnSurface = Neutral800,
    inversePrimary = FocusGreen600
)

@Composable
fun FocusLearnMobileTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Відключаємо Dynamic Color для збереження брендової палітри
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> FocusLearnDarkColorScheme
        else -> FocusLearnLightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = FocusLearnTypography,
        shapes = FocusLearnShapes,
        content = content
    )
}