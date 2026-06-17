package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.example.viewmodel.ThemeMode

private val DayColorScheme = lightColorScheme(
    primary = DayPrimary,
    onPrimary = DayOnPrimary,
    primaryContainer = DayPrimary,
    onPrimaryContainer = DayOnPrimary,
    secondary = DaySecondary,
    onSecondary = DayOnSecondary,
    tertiary = DayTertiary,
    background = DayBackground,
    surface = DaySurface,
    onBackground = DayOnBackground,
    onSurface = DayOnSurface
)

private val NightColorScheme = darkColorScheme(
    primary = NightPrimary,
    onPrimary = NightOnPrimary,
    primaryContainer = NightPrimary,
    onPrimaryContainer = NightOnPrimary,
    secondary = NightSecondary,
    onSecondary = NightOnSecondary,
    tertiary = NightTertiary,
    background = NightBackground,
    surface = NightSurface,
    onBackground = NightOnBackground,
    onSurface = NightOnSurface
)

@Composable
fun LunaraLibraryTheme(
    mode: ThemeMode = ThemeMode.AUTO,
    systemDark: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val darkTheme = when (mode) {
        ThemeMode.AUTO -> systemDark
        ThemeMode.DAY -> false
        ThemeMode.NIGHT -> true
    }

    val colorScheme = if (darkTheme) NightColorScheme else DayColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
