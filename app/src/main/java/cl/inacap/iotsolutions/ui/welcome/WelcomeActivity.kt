package cl.inacap.iotsolutions.ui.welcome

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cl.inacap.iotsolutions.model.DataRepository
import cl.inacap.iotsolutions.model.User
import cl.inacap.iotsolutions.ui.components.AppLogo
import cl.inacap.iotsolutions.ui.components.UserPhoto
import cl.inacap.iotsolutions.ui.components.animatedDouble
import cl.inacap.iotsolutions.ui.components.ensureSession
import cl.inacap.iotsolutions.ui.components.goToLogin
import cl.inacap.iotsolutions.ui.products.ProductListActivity
import cl.inacap.iotsolutions.ui.profile.ProfileActivity
import cl.inacap.iotsolutions.ui.theme.BrandGradient
import cl.inacap.iotsolutions.ui.theme.IoTSolutionsTheme
import cl.inacap.iotsolutions.ui.theme.IotBlue
import cl.inacap.iotsolutions.ui.theme.IotOrange
import cl.inacap.iotsolutions.ui.theme.IotPurple
import cl.inacap.iotsolutions.ui.theme.IotTeal
import cl.inacap.iotsolutions.ui.theme.LightOn
import cl.inacap.iotsolutions.ui.theme.OkGreen
import cl.inacap.iotsolutions.ui.warehouse.WarehouseActivity
import cl.inacap.iotsolutions.util.Format
import java.time.LocalTime

/** Activity 2: Bienvenida con nombre, foto, logo, resumen y menú de navegación. */
class WelcomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!ensureSession()) return
        enableEdgeToEdge()

        // Botones de navegación: cada uno abre otra Activity con un Intent
        val menu = listOf(
            MenuOption("Listado de Productos", "Precios y stock", Icons.AutoMirrored.Filled.List, IotBlue) {
                startActivity(ProductListActivity.newIntent(this, stockMode = false))
            },
            MenuOption("Perfil de Usuario", "Tus datos y foto", Icons.Filled.Person, IotPurple) {
                startActivity(Intent(this, ProfileActivity::class.java))
            },
            MenuOption("Actualizar Stock", "Entradas y salidas", Icons.Filled.SwapVert, IotTeal) {
                startActivity(ProductListActivity.newIntent(this, stockMode = true))
            },
            MenuOption("Monitorear Bodega", "Sensores y controles", Icons.Filled.Thermostat, IotOrange) {
                startActivity(Intent(this, WarehouseActivity::class.java))
            }
        )

        setContent {
            IoTSolutionsTheme {
                val user = DataRepository.currentUser
                if (user != null) {
                    WelcomeScreen(user = user, menu = menu, onLogout = { logout() })
                }
            }
        }
    }

    private fun logout() {
        DataRepository.logout()
        goToLogin()
    }
}

data class MenuOption(
    val label: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color,
    val onClick: () -> Unit
)

/** Saludo según la hora del día. */
private fun greeting(): String = when (LocalTime.now().hour) {
    in 6..11 -> "Buenos días"
    in 12..19 -> "Buenas tardes"
    else -> "Buenas noches"
}

@Composable
fun WelcomeScreen(user: User, menu: List<MenuOption>, onLogout: () -> Unit) {
    val products = DataRepository.products
    val inventoryValue = animatedDouble(products.sumOf { it.totalStockValue }, 900)
    val totalUnits = animatedDouble(products.sumOf { it.stock }.toDouble(), 900)
    val lowStock = products.count { it.stock < 20 }

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Diseño responsivo: más columnas en pantallas anchas (horizontal / tablet)
        val columns = if (maxWidth > 600.dp) 4 else 2

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
        ) {
            // ---------- Encabezado con degradado ----------
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp))
                    .background(BrandGradient)
                    .statusBarsPadding()
                    .padding(start = 20.dp, end = 8.dp, top = 8.dp, bottom = 64.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AppLogo(color = Color.White, iconSize = 36.dp, modifier = Modifier.weight(1f))
                        IconButton(onClick = onLogout) {
                            Icon(
                                Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Cerrar sesión",
                                tint = Color.White
                            )
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        UserPhoto(
                            uri = user.photoUri,
                            size = 88.dp,
                            borderColor = Color.White
                        )
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                "${greeting()},",
                                color = Color.White.copy(alpha = 0.85f),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                user.name,
                                color = Color.White,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(6.dp))
                            Surface(shape = RoundedCornerShape(50), color = Color.White.copy(alpha = 0.2f)) {
                                Text(
                                    user.userType.label,
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // ---------- Indicadores rápidos (se superponen al encabezado) ----------
            Row(
                Modifier
                    .offset(y = (-40).dp)
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickStat("Productos", products.size.toString(), Modifier.weight(1f))
                QuickStat("Unidades", Format.units(totalUnits.toInt()), Modifier.weight(1f))
                QuickStat(
                    "Stock bajo",
                    lowStock.toString(),
                    Modifier.weight(1f),
                    valueColor = if (lowStock > 0) MaterialTheme.colorScheme.error else OkGreen
                )
            }

            Column(
                Modifier
                    .offset(y = (-24).dp)
                    .padding(horizontal = 16.dp)
                    .widthIn(max = 900.dp)
                    .align(Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Valor del inventario: ${Format.money(inventoryValue)}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "¿Qué quieres hacer?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                // ---------- Menú en mosaicos ----------
                menu.chunked(columns).forEachIndexed { rowIndex, rowItems ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        rowItems.forEachIndexed { i, option ->
                            MenuTile(option, index = rowIndex * columns + i, modifier = Modifier.weight(1f))
                        }
                        repeat(columns - rowItems.size) { Spacer(Modifier.weight(1f)) }
                    }
                }

                WarehouseStatusCard()
            }
        }
    }
}

@Composable
private fun QuickStat(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.primary
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = valueColor)
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Botón de menú con animación de entrada escalonada. */
@Composable
private fun MenuTile(option: MenuOption, index: Int, modifier: Modifier = Modifier) {
    val visible = remember { MutableTransitionState(false).apply { targetState = true } }
    AnimatedVisibility(
        visibleState = visible,
        modifier = modifier,
        enter = fadeIn(tween(450, delayMillis = 90 * index)) +
            slideInVertically(tween(450, delayMillis = 90 * index)) { it / 3 }
    ) {
        Card(
            onClick = option.onClick,
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = option.color.copy(alpha = 0.12f)),
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Box(
                    Modifier
                        .size(48.dp)
                        .background(option.color, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(option.icon, contentDescription = null, tint = Color.White)
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    option.label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    option.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/** Estado actual de luces y puerta (se actualiza en vivo desde el repositorio). */
@Composable
private fun WarehouseStatusCard() {
    val lightsOn = DataRepository.lightsOn
    val doorOpen = DataRepository.doorOpen
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Filled.Lightbulb,
                contentDescription = null,
                tint = if (lightsOn) LightOn else MaterialTheme.colorScheme.outline
            )
            Text(if (lightsOn) "Luces encendidas" else "Luces apagadas", Modifier.weight(1f))
            Icon(
                if (doorOpen) Icons.Filled.MeetingRoom else Icons.Filled.Lock,
                contentDescription = null,
                tint = if (doorOpen) OkGreen else MaterialTheme.colorScheme.outline
            )
            Text(if (doorOpen) "Puerta abierta" else "Puerta cerrada", Modifier.weight(1f))
        }
    }
}
