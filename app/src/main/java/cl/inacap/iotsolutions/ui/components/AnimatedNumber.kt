package cl.inacap.iotsolutions.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Devuelve un número que "cuenta" suavemente hasta [target] cada vez que cambia.
 * Se interpola en Double para no perder precisión con montos grandes.
 */
@Composable
fun animatedDouble(target: Double, durationMillis: Int = 500): Double {
    var from by remember { mutableDoubleStateOf(target) }
    var to by remember { mutableDoubleStateOf(target) }
    val progress = remember { Animatable(1f) }

    LaunchedEffect(target) {
        from += (to - from) * progress.value // parte desde el valor que se estaba mostrando
        to = target
        progress.snapTo(0f)
        progress.animateTo(1f, tween(durationMillis))
    }

    return if (progress.value >= 1f) to else from + (to - from) * progress.value
}
