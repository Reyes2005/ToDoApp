package com.angel.todoapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * Esquema de color Material 3 que aplica la paleta verde esmeralda y neutros claros.
 */
private val ToDoColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = EmeraldOnPrimary,
    primaryContainer = EmeraldPrimaryContainer,
    onPrimaryContainer = EmeraldOnPrimaryContainer,
    secondary = SageSecondary,
    onSecondary = SageOnSecondary,
    secondaryContainer = SageSecondaryContainer,
    onSecondaryContainer = SageOnSecondaryContainer,
    tertiary = MintTertiary,
    tertiaryContainer = MintContainer,
    background = NeutralBackground,
    onBackground = NeutralOnSurface,
    surface = NeutralSurface,
    onSurface = NeutralOnSurface,
    surfaceVariant = NeutralSurfaceVariant,
    onSurfaceVariant = NeutralOnSurfaceVariant,
    outline = NeutralOutline,
    error = DangerRed,
    errorContainer = DangerRedContainer
)

/**
 * Tema visual principal de la aplicacion ToDoApp.
 */
@Composable
fun ToDoAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ToDoColorScheme,
        typography = ToDoTypography,
        content = content
    )
}
