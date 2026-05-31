package io.github.pyth0n14n.tastinggenie.ui.theme

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
        secondaryContainer = TastingSecondary,
        onSecondaryContainer = TastingOnSecondary,
        tertiary = TastingTertiaryContainer,
        onTertiary = TastingOnTertiaryContainer,
        tertiaryContainer = TastingTertiary,
        onTertiaryContainer = TastingOnTertiary,
        background = TastingOnBackground,
        onBackground = TastingBackground,
        surface = TastingOnSurface,
        onSurface = TastingSurface,
        surfaceVariant = TastingOnSurfaceVariant,
        onSurfaceVariant = TastingSurfaceVariant,
        surfaceContainerLowest = TastingOnSurface,
        surfaceContainerLow = TastingOnSurface,
        surfaceContainer = TastingOnSurface,
        surfaceContainerHigh = TastingOnSurfaceVariant,
        surfaceContainerHighest = TastingOnSurfaceVariant,
        outline = TastingOutlineVariant,
        outlineVariant = TastingOutline,
        error = TastingErrorContainer,
        onError = TastingOnErrorContainer,
        errorContainer = TastingError,
        onErrorContainer = TastingOnError,
        inverseSurface = TastingSurfaceContainerHighest,
        inverseOnSurface = TastingOnSurface,
        inversePrimary = TastingPrimary,
        scrim = TastingScrim,
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
        surfaceContainerLowest = TastingSurfaceContainerLowest,
        surfaceContainerLow = TastingSurfaceContainerLow,
        surfaceContainer = TastingSurfaceContainer,
        surfaceContainerHigh = TastingSurfaceContainerHigh,
        surfaceContainerHighest = TastingSurfaceContainerHighest,
        outline = TastingOutline,
        outlineVariant = TastingOutlineVariant,
        error = TastingError,
        onError = TastingOnError,
        errorContainer = TastingErrorContainer,
        onErrorContainer = TastingOnErrorContainer,
        inverseSurface = TastingInverseSurface,
        inverseOnSurface = TastingInverseOnSurface,
        inversePrimary = TastingInversePrimary,
        scrim = TastingScrim,
    )

@Composable
@Suppress("UNUSED_PARAMETER")
fun TastingGenie2ndAndroidTheme(
    darkTheme: Boolean = false,
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
