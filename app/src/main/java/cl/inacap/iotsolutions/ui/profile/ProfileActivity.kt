package cl.inacap.iotsolutions.ui.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import cl.inacap.iotsolutions.ui.theme.BrandGradient
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cl.inacap.iotsolutions.model.DataRepository
import cl.inacap.iotsolutions.model.User
import cl.inacap.iotsolutions.model.UserType
import cl.inacap.iotsolutions.ui.components.AppTopBar
import cl.inacap.iotsolutions.ui.components.UserPhoto
import cl.inacap.iotsolutions.ui.components.ensureSession
import cl.inacap.iotsolutions.ui.theme.IoTSolutionsTheme

/** Activity 3: Visualización y edición del perfil del usuario. */
class ProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!ensureSession()) return
        enableEdgeToEdge()
        setContent {
            IoTSolutionsTheme {
                val user = DataRepository.currentUser
                if (user != null) {
                    ProfileScreen(
                        user = user,
                        onBack = { finish() },
                        onSave = { updated ->
                            DataRepository.updateCurrentUser(updated)
                            Toast.makeText(this, "Perfil actualizado", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(user: User, onBack: () -> Unit, onSave: (User) -> Unit) {
    val context = LocalContext.current

    // Copia editable de los datos (se guarda en el repositorio solo al presionar "Guardar")
    var name by rememberSaveable { mutableStateOf(user.name) }
    var email by rememberSaveable { mutableStateOf(user.email) }
    var phone by rememberSaveable { mutableStateOf(user.phone) }
    var userType by rememberSaveable { mutableStateOf(user.userType) }
    var photoUri by rememberSaveable { mutableStateOf(user.photoUri) }
    var showErrors by rememberSaveable { mutableStateOf(false) }

    // Selector de fotos del sistema (no requiere permisos de almacenamiento)
    val pickPhoto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: SecurityException) {
                // Algunos selectores no entregan permiso persistente; igual se puede leer en la sesión
            }
            photoUri = uri
        }
    }

    val nameError = if (name.isBlank()) "El nombre es obligatorio" else null
    val emailError = if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) "Correo no válido" else null
    val phoneDigits = phone.count { it.isDigit() }
    val phoneError = if (phoneDigits < 8) "Teléfono no válido" else null

    Scaffold(topBar = { AppTopBar("Perfil de Usuario", onBack = onBack) }) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 560.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Encabezado con degradado: fotografía (con botón para cambiarla), nombre y tipo
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .background(BrandGradient)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box {
                        UserPhoto(uri = photoUri, size = 120.dp, borderColor = Color.White)
                        SmallFloatingActionButton(
                            onClick = {
                                pickPhoto.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier.align(Alignment.BottomEnd)
                        ) {
                            Icon(Icons.Filled.PhotoCamera, contentDescription = "Cambiar fotografía")
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        name.ifBlank { "Sin nombre" },
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${userType.label} · @${user.username}",
                        color = Color.White.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                if (photoUri != null) {
                    TextButton(onClick = { photoUri = null }) { Text("Quitar fotografía") }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    leadingIcon = { Icon(Icons.Filled.AccountCircle, null) },
                    singleLine = true,
                    isError = showErrors && nameError != null,
                    supportingText = if (showErrors && nameError != null) { { Text(nameError) } } else null,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo electrónico") },
                    leadingIcon = { Icon(Icons.Filled.Email, null) },
                    singleLine = true,
                    isError = showErrors && emailError != null,
                    supportingText = if (showErrors && emailError != null) { { Text(emailError) } } else null,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { text -> phone = text.filter { it.isDigit() || it == '+' || it == ' ' } },
                    label = { Text("Teléfono") },
                    leadingIcon = { Icon(Icons.Filled.Phone, null) },
                    singleLine = true,
                    isError = showErrors && phoneError != null,
                    supportingText = if (showErrors && phoneError != null) { { Text(phoneError) } } else null,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    "Tipo de usuario",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    UserType.entries.forEach { type ->
                        FilterChip(
                            selected = userType == type,
                            onClick = { userType = type },
                            label = { Text(type.label) }
                        )
                    }
                }

                Button(
                    shape = RoundedCornerShape(16.dp),
                    onClick = {
                        showErrors = true
                        if (nameError == null && emailError == null && phoneError == null) {
                            onSave(
                                user.copy(
                                    name = name.trim(),
                                    email = email.trim(),
                                    phone = phone.trim(),
                                    userType = userType,
                                    photoUri = photoUri
                                )
                            )
                            showErrors = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text("GUARDAR CAMBIOS", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
