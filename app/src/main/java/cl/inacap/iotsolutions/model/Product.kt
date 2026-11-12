package cl.inacap.iotsolutions.model

import android.net.Uri
import androidx.annotation.DrawableRes

/** Una imagen de la galería de un producto. */
sealed interface ProductImage {
    /** Ilustración incluida en la app (drawable vectorial) pintada con un color. */
    data class Illustration(@DrawableRes val resId: Int, val tint: Long) : ProductImage

    /** Foto agregada por el usuario desde la galería del teléfono. */
    data class Photo(val uri: Uri) : ProductImage
}

/**
 * Producto de la bodega. Los valores derivados (precio de venta, % de ganancia
 * y valor total del stock) se calculan con [InventoryCalculator].
 */
data class Product(
    val id: Int,
    val name: String,
    val unitCost: Double,
    val marginPercent: Double,
    val stock: Int,
    val images: List<ProductImage>
) {
    val salePrice: Double
        get() = InventoryCalculator.salePrice(unitCost, marginPercent)

    val profitPercent: Double
        get() = InventoryCalculator.profitPercent(salePrice, unitCost)

    val totalStockValue: Double
        get() = InventoryCalculator.totalStockValue(stock, salePrice)
}
