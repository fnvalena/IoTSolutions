package cl.inacap.iotsolutions.ui.components

import android.app.Activity
import android.content.Intent
import cl.inacap.iotsolutions.model.DataRepository
import cl.inacap.iotsolutions.ui.login.LoginActivity

/**
 * Verifica que haya una sesión iniciada. Si no la hay (por ejemplo, porque Android
 * cerró la app y se borraron las variables en memoria), vuelve al login.
 * @return true si se puede seguir mostrando la pantalla.
 */
fun Activity.ensureSession(): Boolean {
    if (DataRepository.currentUser != null) return true
    goToLogin()
    return false
}

/** Abre el login y cierra todas las demás pantallas. */
fun Activity.goToLogin() {
    startActivity(
        Intent(this, LoginActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    )
    finish()
}
