package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = SageAccent,
    onPrimary = SageOnAccent,
    primaryContainer = SageContainer,
    onPrimaryContainer = OnSageContainer,
    secondary = ChalkSecondary,
    onSecondary = CharcoalBackground,
    secondaryContainer = CharcoalSurfaceVariant,
    onSecondaryContainer = ChalkPrimary,
    tertiary = GoldHintDark,
    onTertiary = CharcoalBackground,
    tertiaryContainer = GoldContainerDark,
    onTertiaryContainer = OnGoldContainerDark,
    background = CharcoalBackground,
    onBackground = ChalkPrimary,
    surface = CharcoalSurface,
    onSurface = ChalkPrimary,
    surfaceVariant = CharcoalSurfaceVariant,
    onSurfaceVariant = ChalkSecondary,
    outline = CharcoalOutline,
    outlineVariant = CharcoalOutline
)

private val LightColorScheme = lightColorScheme(
    primary = ForestAccent,
    onPrimary = ForestOnAccent,
    primaryContainer = ForestContainer,
    onPrimaryContainer = OnForestContainer,
    secondary = SlateSecondary,
    onSecondary = ForestOnAccent,
    secondaryContainer = SlateContainer,
    onSecondaryContainer = InkPrimary,
    tertiary = AmberHint,
    onTertiary = ForestOnAccent,
    tertiaryContainer = AmberContainer,
    onTertiaryContainer = OnAmberContainer,
    background = PaperBackground,
    onBackground = InkPrimary,
    surface = PaperSurface,
    onSurface = InkPrimary,
    surfaceVariant = PaperSurfaceVariant,
    onSurfaceVariant = InkSecondary,
    outline = InkOutline,
    outlineVariant = InkOutline
)

val LexiGridShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun LexiGridTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = LexiGridShapes,
        content = content
    )
}
