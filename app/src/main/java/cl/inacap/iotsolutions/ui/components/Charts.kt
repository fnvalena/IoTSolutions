package cl.inacap.iotsolutions.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Indicador circular (tipo velocímetro) dibujado con Canvas.
 * El arco se anima cada vez que cambia el valor.
 */
@Composable
fun CircularGauge(
    value: Float,
    min: Float,
    max: Float,
    color: Color,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 14.dp,
    content: @Composable () -> Unit
) {
    val fraction = ((value - min) / (max - min)).coerceIn(0f, 1f)
    val animatedFraction by animateFloatAsState(fraction, tween(600), label = "gauge")
    val animatedColor by animateColorAsState(color, tween(600), label = "gaugeColor")
    val track = MaterialTheme.colorScheme.surfaceVariant

    Box(modifier.aspectRatio(1f), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            val inset = strokeWidth.toPx() / 2f
            val arcSize = Size(size.width - inset * 2f, size.height - inset * 2f)
            drawArc(
                color = track, startAngle = 135f, sweepAngle = 270f, useCenter = false,
                topLeft = Offset(inset, inset), size = arcSize, style = stroke
            )
            drawArc(
                color = animatedColor, startAngle = 135f, sweepAngle = 270f * animatedFraction,
                useCenter = false, topLeft = Offset(inset, inset), size = arcSize, style = stroke
            )
        }
        content()
    }
}

/** Gráfico de línea simple con relleno degradado (historial de lecturas). */
@Composable
fun Sparkline(points: List<Float>, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        if (points.size < 2) return@Canvas
        val minValue = points.min()
        val maxValue = points.max()
        val range = if (maxValue - minValue < 0.5f) 0.5f else maxValue - minValue
        val stepX = size.width / (points.size - 1)
        val top = size.height * 0.1f
        val usable = size.height * 0.8f

        val line = Path()
        val fill = Path()
        var lastX = 0f
        var lastY = 0f
        points.forEachIndexed { i, v ->
            val x = i * stepX
            val y = top + usable - (v - minValue) / range * usable
            if (i == 0) {
                line.moveTo(x, y)
                fill.moveTo(x, size.height)
                fill.lineTo(x, y)
            } else {
                line.lineTo(x, y)
                fill.lineTo(x, y)
            }
            lastX = x
            lastY = y
        }
        fill.lineTo(lastX, size.height)
        fill.close()

        drawPath(fill, Brush.verticalGradient(listOf(color.copy(alpha = 0.35f), Color.Transparent)))
        drawPath(line, color, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawCircle(color, radius = 5.dp.toPx(), center = Offset(lastX, lastY))
    }
}
