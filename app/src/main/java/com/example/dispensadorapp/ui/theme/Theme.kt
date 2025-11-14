package com.example.dispensadorapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = AmarilloPrimario,
    onPrimary = MarronOscuro,
    secondary = MarronOscuro,
    background = AmarilloPrimario,
    surface = AmarilloPrimario,
    onBackground = MarronOscuro,
    onSurface = MarronOscuro,
)

@Composable
fun DispensadorAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}
