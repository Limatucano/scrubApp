package br.com.scrubs.utils

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import org.jetbrains.skia.Image
import platform.Foundation.NSData
import platform.Foundation.create
import androidx.compose.ui.graphics.asSkiaBitmap

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual fun decodeByteArrayToImageBitmap(bytes: ByteArray): ImageBitmap {
    val data = bytes.usePinned { pinned ->
        NSData.create(bytes = pinned.addressOf(0), length = bytes.size.toULong())
    }
    return Image.makeFromEncoded(bytes).toComposeImageBitmap()
}

actual fun decodeImageBitmapToByteArray(image: ImageBitmap): ByteArray {
    val skiaBitmap = image.asSkiaBitmap()
    val image = Image.makeFromBitmap(skiaBitmap)
    val data = image.encodeToData() ?: error("Falha ao converter bitmap")
    return data.bytes
}