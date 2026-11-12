package cl.inacap.iotsolutions.model

/**
 * Fórmulas del mantenedor de productos, tal como las pide la pauta.
 * Están separadas de la interfaz para que sean fáciles de revisar y probar.
 */
object InventoryCalculator {

    /** Stock Actual = Stock inicial ± Unidades agregadas o retiradas. */
    fun currentStock(initialStock: Int, unitsDelta: Int): Int =
        initialStock + unitsDelta

    /** Precio de venta = Costo unitario + (Costo unitario × margen de ganancia). */
    fun salePrice(unitCost: Double, marginPercent: Double): Double =
        unitCost + unitCost * (marginPercent / 100.0)

    /** Porcentaje de ganancia = ((Precio venta − Costo unitario) / Costo unitario) × 100. */
    fun profitPercent(salePrice: Double, unitCost: Double): Double =
        if (unitCost <= 0.0) 0.0 else (salePrice - unitCost) / unitCost * 100.0

    /** Valor Total Stock = Stock actual × Precio de venta. */
    fun totalStockValue(stock: Int, salePrice: Double): Double =
        stock * salePrice
}
