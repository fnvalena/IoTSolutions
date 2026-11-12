package cl.inacap.iotsolutions.util

import java.text.NumberFormat
import java.util.Locale

/** Formateo de números en estilo chileno ($25.000 / 25,0 %). */
object Format {
    private val chile: Locale = Locale.forLanguageTag("es-CL")

    private val integer = NumberFormat.getNumberInstance(chile).apply {
        maximumFractionDigits = 0
    }

    fun money(value: Double): String = "$" + integer.format(value)

    fun units(value: Int): String = integer.format(value)

    fun percent(value: Double): String = String.format(chile, "%.1f %%", value)

    /** Convierte un número a texto para un campo editable (sin ceros sobrantes). */
    fun toInput(value: Double): String =
        if (value % 1.0 == 0.0) value.toLong().toString()
        else String.format(Locale.US, "%.2f", value).trimEnd('0').trimEnd('.')

    /** Lee un número escrito por el usuario (acepta coma o punto decimal). */
    fun parse(text: String): Double? = text.trim().replace(',', '.').toDoubleOrNull()
}
