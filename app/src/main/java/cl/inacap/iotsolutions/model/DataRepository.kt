package cl.inacap.iotsolutions.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cl.inacap.iotsolutions.R

/**
 * "Base de datos" en memoria: todos los datos de la app se guardan en variables
 * locales dentro de este objeto (singleton), sin base de datos.
 *
 * Al ser un `object`, todas las Activities comparten la misma instancia, así que
 * un cambio hecho en una pantalla (por ejemplo, el stock) se ve en las demás.
 * Se usan estados de Compose (mutableStateOf / mutableStateListOf) para que la
 * interfaz se actualice sola cuando cambian los datos.
 */
object DataRepository {

    // ---------- Usuarios y sesión ----------

    private val users = mutableListOf(
        User(
            username = "admin",
            password = "admin123",
            name = "Juan Pérez",
            email = "juanperez@example.com",
            phone = "+56 9 6764 4321",
            userType = UserType.ADMINISTRADOR
        ),
        User(
            username = "operador",
            password = "1234",
            name = "María González",
            email = "maria.gonzalez@example.com",
            phone = "+56 9 8123 4567",
            userType = UserType.OPERADOR
        )
    )

    /** Usuario con la sesión iniciada (null si no hay sesión). */
    var currentUser by mutableStateOf<User?>(null)
        private set

    /** Valida las credenciales contra las variables locales. */
    fun login(username: String, password: String): Boolean {
        val user = users.firstOrNull { it.username == username.trim() && it.password == password }
        currentUser = user
        return user != null
    }

    fun logout() {
        currentUser = null
    }

    fun updateCurrentUser(updated: User) {
        val index = users.indexOfFirst { it.username == updated.username }
        if (index >= 0) users[index] = updated
        currentUser = updated
    }

    // ---------- Productos ----------

    val products = mutableStateListOf(
        Product(
            id = 1,
            name = "Silla moderna",
            unitCost = 20000.0,
            marginPercent = 25.0,
            stock = 100,
            images = listOf(
                ProductImage.Illustration(R.drawable.img_chair, 0xFFBDBDBD),
                ProductImage.Illustration(R.drawable.img_chair, 0xFFD7B98E),
                ProductImage.Illustration(R.drawable.img_chair, 0xFF212121)
            )
        ),
        Product(
            id = 2,
            name = "Sensor de temperatura",
            unitCost = 12000.0,
            marginPercent = 40.0,
            stock = 50,
            images = listOf(
                ProductImage.Illustration(R.drawable.img_sensor, 0xFF1565C0),
                ProductImage.Illustration(R.drawable.img_sensor, 0xFF2E7D32),
                ProductImage.Illustration(R.drawable.img_sensor, 0xFF546E7A)
            )
        ),
        Product(
            id = 3,
            name = "Lámpara LED inteligente",
            unitCost = 8000.0,
            marginPercent = 50.0,
            stock = 20,
            images = listOf(
                ProductImage.Illustration(R.drawable.img_lamp, 0xFFF9A825),
                ProductImage.Illustration(R.drawable.img_lamp, 0xFFFFFFFF),
                ProductImage.Illustration(R.drawable.img_lamp, 0xFF6A1B9A)
            )
        ),
        Product(
            id = 4,
            name = "Cámara IP de seguridad",
            unitCost = 35000.0,
            marginPercent = 30.0,
            stock = 15,
            images = listOf(
                ProductImage.Illustration(R.drawable.img_camera, 0xFF37474F),
                ProductImage.Illustration(R.drawable.img_camera, 0xFFECEFF1),
                ProductImage.Illustration(R.drawable.img_camera, 0xFFC62828)
            )
        ),
        Product(
            id = 5,
            name = "Caja organizadora",
            unitCost = 5000.0,
            marginPercent = 30.0,
            stock = 40,
            images = listOf(
                ProductImage.Illustration(R.drawable.img_box, 0xFF8D6E63),
                ProductImage.Illustration(R.drawable.img_box, 0xFF00897B),
                ProductImage.Illustration(R.drawable.img_box, 0xFF5D4037)
            )
        )
    )

    fun getProduct(id: Int): Product? = products.firstOrNull { it.id == id }

    fun updateProduct(product: Product) {
        val index = products.indexOfFirst { it.id == product.id }
        if (index >= 0) products[index] = product
    }

    // ---------- Bodega ----------

    var lightsOn by mutableStateOf(false)
    var doorOpen by mutableStateOf(false)
}
