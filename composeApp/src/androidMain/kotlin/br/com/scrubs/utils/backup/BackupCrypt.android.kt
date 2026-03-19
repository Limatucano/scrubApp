package br.com.scrubs.utils.backup

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.FileProvider
import org.koin.java.KoinJavaComponent.getKoin
import java.io.File
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext

private const val SALT_LEN    = 16
private const val IV_LEN      = 12
private const val KEY_LEN     = 256   // bits
private const val ITERATIONS  = 100_000
private const val GCM_TAG_LEN = 128   // bits
private const val ALGORITHM   = "AES/GCM/NoPadding"
private const val KEY_FACTORY = "PBKDF2WithHmacSHA256"

private fun deriveKey(password: String, salt: ByteArray): SecretKeySpec {
    val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LEN)
    val factory = SecretKeyFactory.getInstance(KEY_FACTORY)
    val keyBytes = factory.generateSecret(spec).encoded
    return SecretKeySpec(keyBytes, "AES")
}

actual fun encrypt(plaintext: ByteArray, password: String): ByteArray {
    val salt = ByteArray(SALT_LEN).also { SecureRandom().nextBytes(it) }
    val iv   = ByteArray(IV_LEN).also  { SecureRandom().nextBytes(it) }
    val key  = deriveKey(password, salt)

    val cipher = Cipher.getInstance(ALGORITHM)
    cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LEN, iv))
    val ciphertext = cipher.doFinal(plaintext)

    return salt + iv + ciphertext
}

actual fun decrypt(data: ByteArray, password: String): ByteArray {
    require(data.size > SALT_LEN + IV_LEN) { "Arquivo inválido ou corrompido" }

    val salt       = data.sliceArray(0 until SALT_LEN)
    val iv         = data.sliceArray(SALT_LEN until SALT_LEN + IV_LEN)
    val ciphertext = data.sliceArray(SALT_LEN + IV_LEN until data.size)
    val key        = deriveKey(password, salt)

    val cipher = Cipher.getInstance(ALGORITHM)
    cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LEN, iv))

    return try {
        cipher.doFinal(ciphertext)
    } catch (e: Exception) {
        throw IllegalArgumentException("Senha incorreta ou arquivo corrompido")
    }
}

actual fun saveBackupFile(bytes: ByteArray, fileName: String): String {
    val context: Context = getKoin().get()

    // Salva no cache para poder compartilhar via FileProvider
    val file = File(context.filesDir, fileName).also { it.writeBytes(bytes) }
    return file.absolutePath
}

actual fun shareBackupFile(filePath: String) {
    val context: Context = getKoin().get()
    val file = File(filePath)

    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/octet-stream"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, "Backup Scrubs")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(
        Intent.createChooser(intent, "Compartilhar backup").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    )
}

@Composable
actual fun FilePicker(
    open: Boolean,
    onFileSelected: (ByteArray?) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            onFileSelected(bytes)
        } else {
            onDismiss()
        }
    }

    LaunchedEffect(open) {
        if (open) {
            launcher.launch("*/*") // "*/*" abre todos os tipos de arquivo
        }
    }
}