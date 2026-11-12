package cl.inacap.iotsolutions.ui.products

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cl.inacap.iotsolutions.model.DataRepository
import cl.inacap.iotsolutions.model.Product
import cl.inacap.iotsolutions.ui.components.AppTopBar
import cl.inacap.iotsolutions.ui.components.ProductImageView
import cl.inacap.iotsolutions.ui.components.animatedDouble
import cl.inacap.iotsolutions.ui.components.ensureSession
import cl.inacap.iotsolutions.ui.theme.BrandGradient
import cl.inacap.iotsolutions.ui.theme.IoTSolutionsTheme
import cl.inacap.iotsolutions.ui.theme.OkGreen
import cl.inacap.iotsolutions.util.Format

/**
 * Activity 4: Listado de productos (nombre, precio y stock).
 * También se abre desde el botón "Actualizar Stock" de la bienvenida.
 */
class ProductListActivity : ComponentActivity() {

    companion object {
        private const val EXTRA_STOCK_MODE = "stock_mode"

        fun newIntent(context: Context, stockMode: Boolean): Intent =
            Intent(context, ProductListActivity::class.java)
                .putExtra(EXTRA_STOCK_MODE, stockMode)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!ensureSession()) return
        enableEdgeToEdge()
        val stockMode = intent.getBooleanExtra(EXTRA_STOCK_MODE, false)

        setContent {
            IoTSolutionsTheme {
                ProductListScreen(
                    products = DataRepository.products,
                    stockMode = stockMode,
                    onBack = { finish() },
                    onProductClick = { product ->
                        // Al seleccionar un producto se abre su mantenedor
                        startActivity(ProductDetailActivity.newIntent(this, product.id))
                    }
                )
            }
        }
    }
}

/** Criterios de orden de la lista. */
enum class SortOption(val label: String) { NAME("Nombre"), PRICE("Precio"), STOCK("Stock") }

private const val LOW_STOCK = 20

@Composable
fun ProductListScreen(
    products: List<Product>,
    stockMode: Boolean,
    onBack: () -> Unit,
    onProductClick: (Product) -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    var sort by rememberSaveable { mutableStateOf(if (stockMode) SortOption.STOCK else SortOption.NAME) }

    val visible = products
        .filter { it.name.contains(query.trim(), ignoreCase = true) }
        .let { list ->
            when (sort) {
                SortOption.NAME -> list.sortedBy { it.name }
                SortOption.PRICE -> list.sortedByDescending { it.salePrice }
                SortOption.STOCK -> list.sortedBy { it.stock }
            }
        }
    val maxStock = (products.maxOfOrNull { it.stock } ?: 1).coerceAtLeast(1)
    val inventoryValue = animatedDouble(products.sumOf { it.totalStockValue }, 800)

    Scaffold(
        topBar = { AppTopBar(if (stockMode) "Actualizar Stock" else "Listado de Productos", onBack = onBack) }
    ) { innerPadding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 720.dp)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Resumen con degradado
                item {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(BrandGradient)
                            .padding(20.dp)
                    ) {
                        Text("Valor total del inventario", color = Color.White.copy(alpha = 0.85f))
                        Text(
                            Format.money(inventoryValue),
                            color = Color.White,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            "${products.size} productos · ${Format.units(products.sumOf { it.stock })} unidades",
                            color = Color.White.copy(alpha = 0.85f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (stockMode) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "Toca un producto para agregar o retirar unidades.",
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                // Buscador y orden
                item {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("Buscar producto…") },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { query = "" }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Limpiar búsqueda")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(28.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Row(
                        Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Ordenar por:", style = MaterialTheme.typography.labelLarge)
                        SortOption.entries.forEach { option ->
                            FilterChip(
                                selected = sort == option,
                                onClick = { sort = option },
                                label = { Text(option.label) }
                            )
                        }
                    }
                }

                if (visible.isEmpty()) {
                    item {
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Filled.Search,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Text("No hay productos que coincidan", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                items(visible, key = { it.id }) { product ->
                    ProductRow(
                        product = product,
                        maxStock = maxStock,
                        onClick = { onProductClick(product) },
                        modifier = Modifier.animateItem() // anima reordenamientos y filtros
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductRow(product: Product, maxStock: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val low = product.stock < LOW_STOCK
    val stockColor = if (low) MaterialTheme.colorScheme.error else OkGreen

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            ProductImageView(
                image = product.images.first(),
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    Format.money(product.salePrice),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LinearProgressIndicator(
                        progress = { product.stock.toFloat() / maxStock },
                        color = stockColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(50))
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Stock: ${Format.units(product.stock)}" + if (low) " · bajo" else "",
                        style = MaterialTheme.typography.labelMedium,
                        color = stockColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}
