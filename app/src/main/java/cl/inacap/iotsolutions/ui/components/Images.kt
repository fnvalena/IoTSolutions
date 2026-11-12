package cl.inacap.iotsolutions.ui.components

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cl.inacap.iotsolutions.R
import cl.inacap.iotsolutions.model.ProductImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Carga (en segundo plano) una imagen elegida desde la galería del teléfono. */
@Composable
fun rememberUriBitmap(uri: Uri?): ImageBitmap? {
    val context = LocalContext.current
    val bitmap by produceState<ImageBitmap?>(initialValue = null, uri) {
        value = if (uri == null) null else withContext(Dispatchers.IO) { loadBitmap(context, uri) }
    }
    return bitmap
}

private fun loadBitmap(context: Context, uri: Uri, maxSize: Int = 1024): ImageBitmap? = try {
    // 1) Se leen solo las dimensiones para reducir la imagen y no gastar memoria
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
    var sample = 1
    while (bounds.outWidth / sample > maxSize || bounds.outHeight / sample > maxSize) sample *= 2
    // 2) Se decodifica la imagen reducida
    val options = BitmapFactory.Options().apply { inSampleSize = sample }
    context.contentResolver.openInputStream(uri)
        ?.use { BitmapFactory.decodeStream(it, null, options) }
        ?.asImageBitmap()
} catch (e: Exception) {
    null
}

/** Fotografía circular del usuario (o avatar por defecto si no tiene). */
@Composable
fun UserPhoto(
    uri: Uri?,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    borderColor: Color = MaterialTheme.colorScheme.primary
) {
    val bitmap = rememberUriBitmap(uri)
    val shape = modifier
        .size(size)
        .clip(CircleShape)
        .background(MaterialTheme.colorScheme.surfaceVariant)
        .border(3.dp, borderColor, CircleShape)
    if (bitmap != null) {
        Image(bitmap, "Fotografía del usuario", shape, contentScale = ContentScale.Crop)
    } else {
        Image(
            painterResource(R.drawable.ic_avatar),
            "Fotografía del usuario",
            shape,
            contentScale = ContentScale.Crop
        )
    }
}

/** Muestra una imagen de la galería de un producto. */
@Composable
fun ProductImageView(image: ProductImage, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        when (image) {
            is ProductImage.Illustration -> Image(
                painter = painterResource(image.resId),
                contentDescription = "Imagen del producto",
                colorFilter = ColorFilter.tint(Color(image.tint)),
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize().padding(12.dp)
            )

            is ProductImage.Photo -> {
                val bitmap = rememberUriBitmap(image.uri)
                if (bitmap != null) {
                    Image(bitmap, "Foto del producto", Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    CircularProgressIndicator(Modifier.size(24.dp))
                }
            }
        }
    }
}
