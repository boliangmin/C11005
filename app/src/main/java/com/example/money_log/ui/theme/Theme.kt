package com.example.money_log.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val AppColorScheme = lightColorScheme(
    primary = AppPrimary,
    onPrimary = AppOnPrimary,
    primaryContainer = AppPrimaryContainer,
    onPrimaryContainer = AppOnPrimaryContainer,
    secondary = AppPrimary,
    onSecondary = AppOnPrimary,
    secondaryContainer = AppPrimaryContainer,
    onSecondaryContainer = AppOnPrimaryContainer,
    tertiary = AppPrimary,
    onTertiary = AppOnPrimary,
    tertiaryContainer = AppPrimaryContainer,
    onTertiaryContainer = AppOnPrimaryContainer,
)

@Composable
fun Money_LogTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = Typography,
        content = content
    )
}
