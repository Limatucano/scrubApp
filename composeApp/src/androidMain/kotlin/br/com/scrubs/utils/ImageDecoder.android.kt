package br.com.scrubs.utils

import android.R.attr.bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap

actual fun decodeByteArrayToImageBitmap(bytes: ByteArray): ImageBitmap {
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size).asImageBitmap()
}

actual fun decodeImageBitmapToByteArray(image: ImageBitmap): ByteArray {
    val androidBitmap = image.asAndroidBitmap()
    val stream = java.io.ByteArrayOutputStream()
    androidBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, stream)
    return stream.toByteArray()

}