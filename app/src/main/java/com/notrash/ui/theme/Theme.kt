package com.notrash.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NothingDarkColorScheme = darkColorScheme(
    primary = NothingWhite,
    onPrimary = NothingBlack,
    primaryContainer = NothingCardDark,
    onPrimaryContainer = NothingWhite,
    secondary = Color(0xFFD8D8D8),
    onSecondary = NothingBlack,
    secondaryContainer = Color(0xFF1B1B1B),
    onSecondaryContainer = NothingWhite,
    tertiary = NothingRed,
    onTertiary = NothingWhite,
    tertiaryContainer = Color(0xFF2B1113),
    onTertiaryContainer = Color(0xFFFFDAD9),
    background = Color(0xFF101010),
    surface = Color(0xFF101010),
    surfaceContainer = Color(0xFF0C0C0C),
    surfaceContainerHigh = NothingCardDark,
    surfaceContainerHighest = Color(0xFF222222),
    surfaceDim = Color(0xFF050505),
    onSurface = NothingWhite,
    onSurfaceVariant = NothingTextSecondaryDark,
    outline = NothingCardBorderDark,
    outlineVariant = Color(0xFF333333)
)

private val NothingLightColorScheme = lightColorScheme(
    primary = NothingBlack,
    onPrimary = NothingWhite,
    primaryContainer = NothingCardLight,
    onPrimaryContainer = NothingBlack,
    secondary = NothingBlack,
    onSecondary = NothingWhite,
    secondaryContainer = Color(0xFFE2E2E2),
    onSecondaryContainer = NothingBlack,
    tertiary = NothingRed,
    onTertiary = NothingWhite,
    tertiaryContainer = Color(0xFFFFDAD9),
    onTertiaryContainer = Color(0xFF410005),
    background = NothingLightSurface,
    surface = NothingLightSurface,
    surfaceContainer = NothingWhite,
    surfaceContainerHigh = NothingWhite,
    surfaceContainerHighest = Color(0xFFF9F9F9),
    surfaceDim = Color(0xFFE0E0E0),
    onSurface = NothingBlack,
    onSurfaceVariant = NothingTextSecondaryLight,
    outline = NothingCardBorderLight,
    outlineVariant = Color(0xFFE0E0E0)
)

@Composable
fun NotrashTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    highContrast: Boolean = true,
    content: @Composable () -> Unit
) {
    val base = if (highContrast) {
        if (darkTheme) NothingDarkColorScheme.copy(
            background = Color.Black,
            surface = Color.Black,
            surfaceContainerHigh = Color(0xFF202020),
            surfaceContainerHighest = Color(0xFF363636),
            onSurface = Color.White,
            onBackground = Color.White,
            onSurfaceVariant = Color(0xFFE0E0E0),
            outline = Color(0xFF9E9E9E)
        ) else NothingLightColorScheme.copy(
            background = Color.White,
            surface = Color.White,
            surfaceContainerHigh = Color(0xFFF0F0F0),
            surfaceContainerHighest = Color(0xFFDDDDDD),
            onSurface = Color.Black,
            onBackground = Color.Black,
            onSurfaceVariant = Color(0xFF303030),
            outline = Color(0xFF606060)
        )
    } else {
        if (darkTheme) NothingDarkColorScheme else NothingLightColorScheme
    }
    val colorScheme = base.copy(
        primary = if (highContrast) NothingRed else if (darkTheme) Color.White else Color(0xFF606060),
        onPrimary = if (highContrast || !darkTheme) Color.White else Color.Black,
        secondary = if (highContrast) {
            if (darkTheme) Color(0xFFF62027) else NothingRed
        } else base.onSurface,
        tertiary = NothingRed,
        onTertiary = Color.White
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = NothingTypography,
        shapes = Shapes(),
        content = content
    )
}
