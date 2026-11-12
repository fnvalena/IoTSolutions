package cl.inacap.iotsolutions.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.inacap.iotsolutions.R

/** Logo de la app: ícono de conectividad + texto "IoT SOLUTIONS". */
@Composable
fun AppLogo(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    iconSize: Dp = 44.dp
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(R.drawable.ic_logo),
            contentDescription = "Logo IoT Solutions",
            colorFilter = ColorFilter.tint(color),
            modifier = Modifier.size(iconSize)
        )
        Spacer(Modifier.width(8.dp))
        Column {
            Text("IoT", color = color, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 24.sp)
            Text("SOLUTIONS", color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        }
    }
}

/** Logo circular con ondas que "laten" (animación infinita), usado en el login. */
@Composable
fun PulsingLogo(size: Dp = 110.dp) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Restart),
        label = "pulseProgress"
    )
    Box(Modifier.size(size * 1.6f), contentAlignment = Alignment.Center) {
        // Dos anillos desfasados que crecen y se desvanecen
        listOf(progress, (progress + 0.5f) % 1f).forEach { p ->
            Box(
                Modifier
                    .size(size)
                    .graphicsLayer {
                        scaleX = 1f + p * 0.6f
                        scaleY = 1f + p * 0.6f
                        alpha = 1f - p
                    }
                    .background(Color.White.copy(alpha = 0.35f), CircleShape)
            )
        }
        Surface(shape = CircleShape, color = Color.White, shadowElevation = 10.dp, modifier = Modifier.size(size)) {
            Box(contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(R.drawable.ic_logo),
                    contentDescription = "Logo IoT Solutions",
                    modifier = Modifier.size(size * 0.55f)
                )
            }
        }
    }
}
