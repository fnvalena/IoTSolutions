package cl.inacap.iotsolutions.ui.warehouse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.inacap.iotsolutions.model.DataRepository
import cl.inacap.iotsolutions.ui.components.AppTopBar
import cl.inacap.iotsolutions.ui.components.CircularGauge
import cl.inacap.iotsolutions.ui.components.Sparkline
import cl.inacap.iotsolutions.ui.components.ensureSession
import cl.inacap.iotsolutions.ui.theme.AlertRed
import cl.inacap.iotsolutions.ui.theme.IoTSolutionsTheme
import cl.inacap.iotsolutions.ui.theme.IotCyan
import cl.inacap.iotsolutions.ui.theme.IotOrange
import cl.inacap.iotsolutions.ui.theme.LightOn
import cl.inacap.iotsolutions.ui.theme.OkGreen
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.random.Random

/** Activity 5: Monitoreo de la bodega (sensores simulados y actuadores). */
class WarehouseActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!ensureSession()) return
        enableEdgeToEdge()
        setContent {
            IoTSolutionsTheme {
                WarehouseScreen(onBack = { finish() })
            }
        }
    }
}

private val chile: Locale = Locale.forLanguageTag("es-CL")
private val timeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

/** Redondea a un decimal. */
private fun Float.oneDecimal(): Float = (this * 10f).roundToInt() / 10f

@Composable
fun WarehouseScreen(onBack: () -> Unit) {
    // Valores simulados de los sensores
    var temperature by rememberSaveable { mutableFloatStateOf(22.5f) }
    var humidity by rememberSaveable { mutableFloatStateOf(55f) }
    var liveMode by rememberSaveable { mutableStateOf(true) }
    var lastUpdate by remember { mutableStateOf(LocalTime.now().format(timeFormat)) }
    val history = remember { mutableStateListOf<Float>() }

    // Modo en vivo: cada 2 segundos los sensores varían un poco al azar
    LaunchedEffect(liveMode) {
        if (liveMode) {
            while (true) {
                delay(2000)
                temperature = (temperature + Random.nextFloat() * 1.6f - 0.8f).coerceIn(-10f, 45f).oneDecimal()
                humidity = (humidity + Random.nextFloat() * 4f - 2f).coerceIn(0f, 100f).oneDecimal()
            }
        }
    }

    // Historial de temperatura para el gráfico (últimas 30 lecturas)
    LaunchedEffect(Unit) {
        snapshotFlow { temperature to humidity }.collect { (t, _) ->
            history.add(t)
            if (history.size > 30) history.removeAt(0)
            lastUpdate = LocalTime.now().format(timeFormat)
        }
    }

    val tempColor = when {
        temperature > 30f -> AlertRed
        temperature < 10f -> IotCyan
        else -> OkGreen
    }
    val humidityColor = when {
        humidity > 80f -> AlertRed
        humidity < 30f -> IotOrange
        else -> IotCyan
    }

    Scaffold(topBar = { AppTopBar("Monitorear Bodega", onBack = onBack) }) { innerPadding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                Modifier
                    .widthIn(max = 760.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ---------- Encabezado: modo en vivo ----------
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Sensores", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(
                            "Última lectura: $lastUpdate",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (liveMode) LiveBadge()
                    Spacer(Modifier.width(8.dp))
                    Switch(checked = liveMode, onCheckedChange = { liveMode = it })
                }

                // ---------- Gauges ----------
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SensorCard(
                        title = "Temperatura",
                        icon = Icons.Filled.Thermostat,
                        value = temperature,
                        min = -10f,
                        max = 45f,
                        display = String.format(chile, "%.1f°C", temperature),
                        color = tempColor,
                        onChange = { temperature = it.oneDecimal() },
                        modifier = Modifier.weight(1f)
                    )
                    SensorCard(
                        title = "Humedad",
                        icon = Icons.Filled.Opacity,
                        value = humidity,
                        min = 0f,
                        max = 100f,
                        display = String.format(chile, "%.0f%%", humidity),
                        color = humidityColor,
                        onChange = { humidity = it.oneDecimal() },
                        modifier = Modifier.weight(1f)
                    )
                }

                // ---------- Gráfico en vivo ----------
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Historial de temperatura", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            if (history.isEmpty()) "Sin datos" else
                                String.format(
                                    chile, "Mín %.1f°C · Máx %.1f°C",
                                    history.minOrNull() ?: 0f, history.maxOrNull() ?: 0f
                                ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(12.dp))
                        Sparkline(
                            points = history,
                            color = tempColor,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        )
                    }
                }

                Button(
                    onClick = {
                        temperature = (15f + Random.nextFloat() * 20f).oneDecimal()
                        humidity = (30f + Random.nextFloat() * 60f).oneDecimal()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Generar valores aleatorios")
                }

                if (temperature > 30f || humidity > 80f) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.errorContainer)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Warning, null, tint = MaterialTheme.colorScheme.onErrorContainer)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Condiciones fuera de rango: revisa la ventilación de la bodega.",
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }

                // ---------- Controles ----------
                Text("Controles", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    val lightsOn = DataRepository.lightsOn
                    ControlCard(
                        title = "LUCES",
                        active = lightsOn,
                        activeColor = LightOn,
                        iconOn = Icons.Filled.Lightbulb,
                        iconOff = Icons.Filled.Lightbulb,
                        stateText = if (lightsOn) "Encendidas" else "Apagadas",
                        buttonText = if (lightsOn) "APAGAR" else "ENCENDER",
                        onClick = { DataRepository.lightsOn = !lightsOn },
                        modifier = Modifier.weight(1f)
                    )
                    val doorOpen = DataRepository.doorOpen
                    ControlCard(
                        title = "PUERTA",
                        active = doorOpen,
                        activeColor = OkGreen,
                        iconOn = Icons.Filled.MeetingRoom,
                        iconOff = Icons.Filled.Lock,
                        stateText = if (doorOpen) "Abierta" else "Cerrada",
                        buttonText = if (doorOpen) "CERRAR" else "ABRIR",
                        onClick = { DataRepository.doorOpen = !doorOpen },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/** Indicador rojo parpadeante de "EN VIVO". */
@Composable
private fun LiveBadge() {
    val transition = rememberInfiniteTransition(label = "live")
    val blink by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "liveAlpha"
    )
    Row(
        Modifier
            .clip(RoundedCornerShape(50))
            .background(AlertRed.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(8.dp)
                .graphicsLayer { alpha = blink }
                .background(AlertRed, CircleShape)
        )
        Spacer(Modifier.width(6.dp))
        Text("EN VIVO", color = AlertRed, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SensorCard(
    title: String,
    icon: ImageVector,
    value: Float,
    min: Float,
    max: Float,
    display: String,
    color: Color,
    onChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = color)
                Spacer(Modifier.width(4.dp))
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(8.dp))
            CircularGauge(
                value = value,
                min = min,
                max = max,
                color = color,
                strokeWidth = 12.dp,
                modifier = Modifier.fillMaxWidth(0.85f)
            ) {
                Text(display, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
            }
            // Slider para simular la lectura manualmente
            Slider(
                value = value,
                onValueChange = onChange,
                valueRange = min..max,
                colors = SliderDefaults.colors(thumbColor = color, activeTrackColor = color)
            )
        }
    }
}

@Composable
private fun ControlCard(
    title: String,
    active: Boolean,
    activeColor: Color,
    iconOn: ImageVector,
    iconOff: ImageVector,
    stateText: String,
    buttonText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // El fondo, el color y el tamaño del ícono cambian con animación
    val background by animateColorAsState(
        if (active) activeColor.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surface,
        tween(400), label = "controlBg"
    )
    val iconColor by animateColorAsState(
        if (active) activeColor else MaterialTheme.colorScheme.outline,
        tween(400), label = "controlIcon"
    )
    val iconScale by animateFloatAsState(if (active) 1.15f else 1f, spring(dampingRatio = 0.4f), label = "iconScale")

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = background),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            AnimatedContent(
                targetState = active,
                transitionSpec = { (fadeIn() + scaleIn()).togetherWith(fadeOut()) },
                label = "controlIconSwap"
            ) { isActive ->
                IconButton(onClick = onClick, modifier = Modifier.size(72.dp)) {
                    Icon(
                        if (isActive) iconOn else iconOff,
                        contentDescription = title,
                        tint = iconColor,
                        modifier = Modifier
                            .size(56.dp)
                            .graphicsLayer {
                                scaleX = iconScale
                                scaleY = iconScale
                            }
                    )
                }
            }
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(stateText, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (active) activeColor else MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(buttonText, fontWeight = FontWeight.Bold)
            }
        }
    }
}
