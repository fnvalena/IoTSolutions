package cl.inacap.iotsolutions.model

import android.net.Uri

/** Tipos de usuario disponibles en la app. */
enum class UserType(val label: String) {
    ADMINISTRADOR("Administrador"),
    OPERADOR("Operador"),
    BODEGUERO("Bodeguero")
}

/**
 * Usuario de la app. Las credenciales viven en variables locales
 * (ver [DataRepository]); esta versión no usa base de datos.
 */
data class User(
    val username: String,
    val password: String,
    val name: String,
    val email: String,
    val phone: String,
    val userType: UserType,
    /** Foto elegida desde la galería; si es null se muestra un avatar por defecto. */
    val photoUri: Uri? = null
)
