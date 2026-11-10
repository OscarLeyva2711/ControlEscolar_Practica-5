package com.example.controlescolar.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EsquemaClaro = lightColorScheme(
    primary = Color(0xFF174D91),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCE7F9),
    onPrimaryContainer = Color(0xFF174D91),
    secondaryContainer = Color(0xFFDCE7F9),
    onSecondaryContainer = Color(0xFF174D91),
    background = Color(0xFFF7F8FC),
    onBackground = Color(0xFF191C20),
    surface = Color(0xFFF7F8FC),
    onSurface = Color(0xFF191C20),
    surfaceVariant = Color(0xFFEDF1F8),
    onSurfaceVariant = Color(0xFF545B69),
    surfaceContainerLowest = Color.White,
    outline = Color(0xFF737A88),
    outlineVariant = Color(0xFFD7DCE6),
    error = Color(0xFFB3261E),
    onError = Color.White
)

@Composable
fun ControlEscolarTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EsquemaClaro,
        typography = Typography(), // Roboto por defecto en Android
        content = content
    )
}
