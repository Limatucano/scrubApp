package br.com.scrubs.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import java.io.ByteArrayOutputStream
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import org.koin.java.KoinJavaComponent.getKoin
import java.io.File

actual fun cropAndRotateImage(bytes: ByteArray, isFront: Boolean): ByteArray {
    val original = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return bytes

    val matrix = Matrix().apply { postRotate(if (isFront) -90f else 90f) }
    val rotated = Bitmap.createBitmap(original, 0, 0, original.width, original.height, matrix, true)
    original.recycle()

    val output = ByteArrayOutputStream()
    rotated.compress(Bitmap.CompressFormat.JPEG, 90, output)
    rotated.recycle()

    return output.toByteArray()
}

actual fun shareImage(bytes: ByteArray, fileName: String) {
    val context: Context = getKoin().get()

    val cacheFile = File(context.cacheDir, fileName).also { it.writeBytes(bytes) }
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", cacheFile)

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/jpeg"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(Intent.createChooser(intent, "Compartilhar etiqueta").apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    })
}

actual fun saveImageToGallery(bytes: ByteArray, fileName: String): Boolean {
    val context: Context = getKoin().get()
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Scrubs")
            }
            val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            uri?.let { context.contentResolver.openOutputStream(it)?.use { stream -> stream.write(bytes) } }
            uri != null
        } else {
            val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Scrubs")
            dir.mkdirs()
            File(dir, fileName).writeBytes(bytes)
            true
        }
    } catch (e: Exception) {
        false
    }
}