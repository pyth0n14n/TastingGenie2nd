package io.github.pyth0n14n.tastinggenie.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme =
    darkColorScheme(
        primary = TastingPrimaryContainer,
        onPrimary = TastingOnPrimaryContainer,
        primaryContainer = TastingPrimary,
        onPrimaryContainer = TastingOnPrimary,
        secondary = TastingSecondaryContainer,
        onSecondary = TastingOnSecondaryContainer,
        tertiary = TastingTertiaryContainer,
        onTertiary = TastingOnTertiaryContainer,
        background = TastingOnBackground,
        onBackground = TastingBackground,
        surface = TastingOnSurface,
        onSurface = TastingSurface,
        surfaceVariant = TastingOnSurfaceVariant,
        onSurfaceVariant = TastingSurfaceVariant,
        outline = TastingOutlineVariant,
        outlineVariant = TastingOutline,
        error = TastingErrorContainer,
        onError = TastingOnErrorContainer,
        errorContainer = TastingError,
        onErrorContainer = TastingOnError,
    )

private val LightColorScheme =
    lightColorScheme(
        primary = TastingPrimary,
        onPrimary = TastingOnPrimary,
        primaryContainer = TastingPrimaryContainer,
        onPrimaryContainer = TastingOnPrimaryContainer,
        secondary = TastingSecondary,
        onSecondary = TastingOnSecondary,
        secondaryContainer = TastingSecondaryContainer,
        onSecondaryContainer = TastingOnSecondaryContainer,
        tertiary = TastingTertiary,
        onTertiary = TastingOnTertiary,
        tertiaryContainer = TastingTertiaryContainer,
        onTertiaryContainer = TastingOnTertiaryContainer,
        background = TastingBackground,
        onBackground = TastingOnBackground,
        surface = TastingSurface,
        onSurface = TastingOnSurface,
        surfaceVariant = TastingSurfaceVariant,
        onSurfaceVariant = TastingOnSurfaceVariant,
        surfaceContainer = TastingSurfaceContainer,
        surfaceContainerHigh = TastingSurfaceContainerHigh,
        outline = TastingOutline,
        outlineVariant = TastingOutlineVariant,
        error = TastingError,
        onError = TastingOnError,
        errorContainer = TastingErrorContainer,
        onErrorContainer = TastingOnErrorContainer,
    )

@Composable
@Suppress("UNUSED_PARAMETER")
fun TastingGenie2ndAndroidTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
