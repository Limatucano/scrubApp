package br.com.scrubs.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import java.io.ByteArrayOutputStream

actual fun cropAndRotateImage(
    bytes: ByteArray,
    frameCenterX: Float,
    frameCenterY: Float,
    frameWidthRatio: Float,
    frameHeightRatio: Float
): ByteArray {
    val original = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        ?: return bytes

    val imgW = original.width
    val imgH = original.height

    val cropX = ((frameCenterX - frameWidthRatio / 2f) * imgW).toInt().coerceIn(0, imgW)
    val cropY = ((frameCenterY - frameHeightRatio / 2f) * imgH).toInt().coerceIn(0, imgH)
    val cropW = (frameWidthRatio * imgW).toInt().coerceIn(1, imgW - cropX)
    val cropH = (frameHeightRatio * imgH).toInt().coerceIn(1, imgH - cropY)

    val cropped = Bitmap.createBitmap(original, cropX, cropY, cropW, cropH)
    original.recycle()

    val matrix = Matrix().apply { postRotate(90f) }
    val rotated = Bitmap.createBitmap(cropped, 0, 0, cropped.width, cropped.height, matrix, true)
    cropped.recycle()

    val output = ByteArrayOutputStream()
    rotated.compress(Bitmap.CompressFormat.JPEG, 90, output)
    rotated.recycle()

    return output.toByteArray()
}