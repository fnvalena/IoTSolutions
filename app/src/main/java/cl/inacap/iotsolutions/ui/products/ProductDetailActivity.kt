package cl.inacap.iotsolutions.ui.products

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.inacap.iotsolutions.model.DataRepository
import cl.inacap.iotsolutions.model.InventoryCalculator
import cl.inacap.iotsolutions.model.Product
import cl.inacap.iotsolutions.model.ProductImage
import cl.inacap.iotsolutions.ui.components.AppTopBar
import cl.inacap.iotsolutions.ui.components.NumberField
import cl.inacap.iotsolutions.ui.components.ProductImageView
import cl.inacap.iotsolutions.ui.components.StatCard
import cl.inacap.iotsolutions.ui.components.animatedDouble
import cl.inacap.iotsolutions.ui.components.ensureSession
import cl.inacap.iotsolutions.ui.theme.IoTSolutionsTheme
import cl.inacap.iotsolutions.ui.theme.OkGreen
import cl.inacap.iotsolutions.util.Format
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/** Activity 4 (detalle): Mantenedor del producto con galería y cálculos. */
class ProductDetailActivity : ComponentActivity() {

    companion object {
        private const val EXTRA_PRODUCT_ID = "product_id"

        fun newIntent(context: Context, productId: Int): Intent =
            Intent(context, ProductDetailActivity::class.java)
                .putExtra(EXTRA_PRODUCT_ID, productId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!ensureSession()) return
        val product = DataRepository.getProduct(intent.getIntExtra(EXTRA_PRODUCT_ID, -1))
        if (product == null) {
            finish()
            return
        }
        enableEdgeToEdge()
        setContent {
            IoTSolutionsTheme {
                ProductDetailScreen(
                    product = product,
                    onBack = { finish() },
                    onSave = { updated ->
                        DataRepository.updateProduct(updated)
                        Toast.makeText(this, "Producto actualizado", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProductDetailScreen(product: Product, onBack: () -> Unit, onSave: (Product) -> Unit) {
    val scope = rememberCoroutineScope()

    // ----- Estado editable de la pantalla -----
    var name by rememberSaveable { mutableStateOf(product.name) }
    var costText by rememberSaveable { mutableStateOf(Format.toInput(product.unitCost)) }
    var marginText by rememberSaveable { mutableStateOf(Format.toInput(product.marginPercent)) }
    var saleText by rememberSaveable { mutableStateOf(Format.toInput(product.salePrice)) }
    var unitsText by rememberSaveable { mutableStateOf("") }
    var unitsDelta by rememberSaveable { mutableIntStateOf(0) } // unidades agregadas (+) o retiradas (−)
    var stockError by rememberSaveable { mutableStateOf<String?>(null) }
    var showErrors by rememberSaveable { mutableStateOf(false) }
    val images = remember { mutableStateListOf<ProductImage>().apply { addAll(product.images) } }
    val pagerState = rememberPagerState(pageCount = { images.size })

    // ----- Cálculos (fórmulas de la pauta, ver InventoryCalculator) -----
    val cost = Format.parse(costText) ?: 0.0
    val margin = Format.parse(marginText) ?: 0.0
    val currentStock = InventoryCalculator.currentStock(product.stock, unitsDelta)
    val salePrice = InventoryCalculator.salePrice(cost, margin)
    val profitPercent = InventoryCalculator.profitPercent(salePrice, cost)
    val totalValue = InventoryCalculator.totalStockValue(currentStock, salePrice)

    val nameError = if (name.isBlank()) "Ingresa un nombre" else null
    val costError = if (cost <= 0.0) "El costo debe ser mayor a 0" else null
    val marginError = if (Format.parse(marginText) == null) "Ingresa un margen válido" else null

    val pickPhoto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri != null) {
            images.add(ProductImage.Photo(uri))
            scope.launch { pagerState.animateScrollToPage(images.lastIndex) }
        }
    }

    /** Aplica un movimiento de stock (positivo = agregar, negativo = retirar). */
    fun moveStock(delta: Int): Boolean {
        if (currentStock + delta < 0) {
            stockError = "Solo hay $currentStock unidades para retirar"
            return false
        }
        stockError = null
        unitsDelta += delta
        return true
    }

    fun moveFromField(sign: Int) {
        val units = unitsText.toIntOrNull()
        if (units == null || units <= 0) {
            stockError = "Ingresa una cantidad mayor a 0"
        } else if (moveStock(sign * units)) {
            unitsText = ""
        }
    }

    fun updateMargin(newMarginText: String) {
        marginText = newMarginText
        val newMargin = Format.parse(newMarginText) ?: 0.0
        saleText = Format.toInput(InventoryCalculator.salePrice(cost, newMargin))
    }

    // ---------- Galería ----------
    val gallery: @Composable (Modifier) -> Unit = { modifier ->
        Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Box {
                HorizontalPager(
                    state = pagerState,
                    pageSpacing = 12.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(4f / 3f)
                        .clip(RoundedCornerShape(24.dp))
                ) { page ->
                    ProductImageView(images[page], Modifier.fillMaxSize())
                }
                // Indicador de páginas (puntos animados)
                Row(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .padding(12.dp)
                        .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(50))
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    repeat(images.size) { i ->
                        val selected = pagerState.currentPage == i
                        val width by animateDpAsState(if (selected) 20.dp else 8.dp, label = "dot")
                        Box(
                            Modifier
                                .padding(horizontal = 3.dp)
                                .height(8.dp)
                                .width(width)
                                .clip(RoundedCornerShape(50))
                                .background(if (selected) Color.White else Color.White.copy(alpha = 0.5f))
                        )
                    }
                }
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(images) { index, image ->
                    val borderColor = if (index == pagerState.currentPage) MaterialTheme.colorScheme.primary
                    else Color.Transparent
                    ProductImageView(
                        image = image,
                        modifier = Modifier
                            .size(68.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .border(BorderStroke(3.dp, borderColor), RoundedCornerShape(14.dp))
                            .clickable { scope.launch { pagerState.animateScrollToPage(index) } }
                    )
                }
                item {
                    Surface(
                        onClick = {
                            pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.size(68.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.AddPhotoAlternate, contentDescription = "Agregar foto")
                        }
                    }
                }
            }
        }
    }

    // ---------- Formulario ----------
    val form: @Composable (Modifier) -> Unit = { modifier ->
        Column(modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre del producto") },
                singleLine = true,
                isError = showErrors && nameError != null,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            SectionCard("Precio") {
                NumberField(
                    value = costText,
                    onValueChange = {
                        costText = it
                        val newCost = Format.parse(it) ?: 0.0
                        saleText = Format.toInput(InventoryCalculator.salePrice(newCost, margin))
                    },
                    label = "Costo unitario",
                    prefix = "$",
                    errorText = if (showErrors) costError else null,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    NumberField(
                        value = marginText,
                        onValueChange = { updateMargin(it) },
                        label = "Margen",
                        suffix = "%",
                        errorText = if (showErrors) marginError else null,
                        modifier = Modifier.weight(1f)
                    )
                    NumberField(
                        value = saleText,
                        onValueChange = {
                            // Si se edita el precio de venta, se recalcula el margen
                            saleText = it
                            val newSale = Format.parse(it)
                            if (newSale != null && cost > 0.0) {
                                marginText = Format.toInput(InventoryCalculator.profitPercent(newSale, cost))
                            }
                        },
                        label = "Precio de venta",
                        prefix = "$",
                        modifier = Modifier.weight(1f)
                    )
                }
                Text("Ajuste rápido del margen", style = MaterialTheme.typography.labelLarge)
                Slider(
                    value = margin.toFloat().coerceIn(0f, 200f),
                    onValueChange = { updateMargin(it.roundToInt().toString()) },
                    valueRange = 0f..200f
                )
            }

            SectionCard("Stock") {
                val animatedStock = animatedDouble(currentStock.toDouble(), 350)
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        Format.units(animatedStock.roundToInt()),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("unidades", Modifier.padding(bottom = 6.dp))
                }
                Text(
                    "Stock inicial: ${Format.units(product.stock)} · Movimiento: " +
                        (if (unitsDelta >= 0) "+" else "−") + Format.units(abs(unitsDelta)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // Botones rápidos
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(-10, -5, -1, 1, 5, 10).forEach { delta ->
                        AssistChip(
                            onClick = { moveStock(delta) },
                            label = { Text(if (delta > 0) "+$delta" else "−${abs(delta)}") }
                        )
                    }
                }
                NumberField(
                    value = unitsText,
                    onValueChange = { unitsText = it; stockError = null },
                    label = "Otra cantidad",
                    decimal = false,
                    errorText = stockError,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = { moveFromField(-1) }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Filled.Remove, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Retirar")
                    }
                    Button(onClick = { moveFromField(+1) }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Agregar")
                    }
                }
            }

            // ---------- Resultados calculados (con animación) ----------
            Text("Resumen", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    title = "Stock actual",
                    value = "${Format.units(animatedDouble(currentStock.toDouble()).roundToInt())} u.",
                    caption = "Stock inicial ± movimientos",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Precio de venta",
                    value = Format.money(animatedDouble(salePrice)),
                    caption = "Costo + (Costo × margen)",
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    title = "% de ganancia",
                    value = Format.percent(animatedDouble(profitPercent)),
                    caption = "(Venta − Costo) / Costo × 100",
                    highlight = OkGreen,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Valor total stock",
                    value = Format.money(animatedDouble(totalValue)),
                    caption = "Stock actual × Precio venta",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    Scaffold(
        topBar = { AppTopBar("Detalle del Producto", onBack = onBack) },
        bottomBar = {
            // Botón de guardar siempre visible
            Surface(shadowElevation = 12.dp) {
                Button(
                    onClick = {
                        showErrors = true
                        if (nameError == null && costError == null && marginError == null) {
                            onSave(
                                product.copy(
                                    name = name.trim(),
                                    unitCost = cost,
                                    marginPercent = margin,
                                    stock = currentStock,
                                    images = images.toList()
                                )
                            )
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp)
                        .height(54.dp)
                ) {
                    Text("GUARDAR CAMBIOS", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        // Diseño responsivo: galería y formulario lado a lado en pantallas anchas
        BoxWithConstraints(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
        ) {
            if (maxWidth > 700.dp) {
                Row(
                    Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    gallery(Modifier.weight(1f).verticalScroll(rememberScrollState()))
                    Spacer(Modifier.width(24.dp))
                    form(Modifier.weight(1f).verticalScroll(rememberScrollState()))
                }
            } else {
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    gallery(Modifier.fillMaxWidth())
                    form(Modifier.fillMaxWidth())
                }
            }
        }
    }
}

/** Tarjeta con título para agrupar campos del formulario. */
@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            content()
        }
    }
}
