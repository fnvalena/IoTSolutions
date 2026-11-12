package cl.inacap.iotsolutions.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = IotBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCE7FF),
    onPrimaryContainer = IotBlueDark,
    secondary = IotCyan,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCCF3F8),
    background = Color(0xFFF5F7FB),
    surface = Color(0xFFF5F7FB),
    error = AlertRed
)

private val DarkColors = darkColorScheme(
    primary = IotBlueLight,
    onPrimary = IotBlueDark,
    primaryContainer = Color(0xFF1B3A6B),
    onPrimaryContainer = Color(0xFFDCE7FF),
    secondary = Color(0xFF4DD0E1),
    secondaryContainer = Color(0xFF004F58),
    error = Color(0xFFEF9A9A)
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp)
)

/** Tema Material 3 de la app (modo claro y oscuro). */
@Composable
fun IoTSolutionsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        shapes = AppShapes,
        content = content
    )
}
