package br.com.scrubs.utils.backup

import androidx.compose.runtime.*
import kotlinx.cinterop.*
import platform.CoreCrypto.CCCrypt
import platform.CoreCrypto.CCKeyDerivationPBKDF
import platform.CoreCrypto.kCCAlgorithmAES
import platform.CoreCrypto.kCCDecrypt
import platform.CoreCrypto.kCCEncrypt
import platform.CoreCrypto.kCCOptionPKCS7Padding
import platform.CoreCrypto.kCCPBKDF2
import platform.CoreCrypto.kCCPRFHmacAlgSHA256
import platform.CoreCrypto.kCCSuccess
import platform.Foundation.*
import platform.UIKit.*
import platform.darwin.NSObject
import platform.posix.memcpy

private const val SALT_LEN = 16
private const val IV_LEN   = 12
private const val KEY_LEN  = 32  // 256 bits

// ── arc4random evita dependência de SecRandomCopyBytes / kSecRandomDefault ────
private fun generateRandomBytes(size: Int): ByteArray =
    ByteArray(size) { (platform.posix.arc4random() % 256u).toByte() }

@OptIn(ExperimentalForeignApi::class)
private fun deriveKey(password: String, salt: ByteArray): ByteArray {
    val keyBytes      = UByteArray(KEY_LEN)
    val passwordBytes = password.encodeToByteArray()
    val saltU         = salt.asUByteArray()       // ← UByteArray

    memScoped {
        keyBytes.usePinned { keyPin ->
            passwordBytes.usePinned { passPin ->
                saltU.usePinned { saltPin ->
                    CCKeyDerivationPBKDF(
                        algorithm     = kCCPBKDF2,
                        password      = passPin.addressOf(0).toString(),
                        passwordLen   = passwordBytes.size.toULong(),
                        salt          = saltPin.addressOf(0),
                        saltLen       = salt.size.toULong(),
                        prf           = kCCPRFHmacAlgSHA256,
                        rounds        = 100_000u,
                        derivedKey    = keyPin.addressOf(0),
                        derivedKeyLen = KEY_LEN.toULong()
                    )
                }
            }
        }
    }
    return keyBytes.toByteArray()
}

@OptIn(ExperimentalForeignApi::class)
actual fun encrypt(plaintext: ByteArray, password: String): ByteArray {
    val salt = generateRandomBytes(SALT_LEN)
    val iv   = generateRandomBytes(IV_LEN)
    val key  = deriveKey(password, salt)

    val outputSize = plaintext.size + 32
    val output = ByteArray(outputSize)

    // movedVar.ptr resolve o "CValuesRef<ULongVar>? expected" — nunca passar ULong direto
    val moved = memScoped {
        val movedVar = alloc<ULongVar>()
        plaintext.usePinned { pPin ->
            key.usePinned { kPin ->
                iv.usePinned { ivPin ->
                    output.usePinned { oPin ->
                        CCCrypt(
                            op               = kCCEncrypt,
                            alg              = kCCAlgorithmAES,
                            options          = kCCOptionPKCS7Padding,
                            key              = kPin.addressOf(0),
                            keyLength        = KEY_LEN.toULong(),
                            iv               = ivPin.addressOf(0),
                            dataIn           = pPin.addressOf(0),
                            dataInLength     = plaintext.size.toULong(),
                            dataOut          = oPin.addressOf(0),
                            dataOutAvailable = outputSize.toULong(),
                            dataOutMoved     = movedVar.ptr
                        )
                    }
                }
            }
        }
        movedVar.value.toInt()
    }

    return salt + iv + output.sliceArray(0 until moved)
}

@OptIn(ExperimentalForeignApi::class)
actual fun decrypt(data: ByteArray, password: String): ByteArray {
    require(data.size > SALT_LEN + IV_LEN) { "Arquivo inválido ou corrompido" }

    val salt       = data.sliceArray(0 until SALT_LEN)
    val iv         = data.sliceArray(SALT_LEN until SALT_LEN + IV_LEN)
    val ciphertext = data.sliceArray(SALT_LEN + IV_LEN until data.size)
    val key        = deriveKey(password, salt)
    val outBuf     = ByteArray(ciphertext.size)

    val moved = memScoped {
        val movedVar = alloc<ULongVar>()
        ciphertext.usePinned { cPin ->
            key.usePinned { kPin ->
                iv.usePinned { ivPin ->
                    outBuf.usePinned { oPin ->
                        val status = CCCrypt(
                            op               = kCCDecrypt,
                            alg              = kCCAlgorithmAES,
                            options          = kCCOptionPKCS7Padding,
                            key              = kPin.addressOf(0),
                            keyLength        = KEY_LEN.toULong(),
                            iv               = ivPin.addressOf(0),
                            dataIn           = cPin.addressOf(0),
                            dataInLength     = ciphertext.size.toULong(),
                            dataOut          = oPin.addressOf(0),
                            dataOutAvailable = outBuf.size.toULong(),
                            dataOutMoved     = movedVar.ptr
                        )
                        if (status != kCCSuccess)
                            throw IllegalArgumentException("Senha incorreta ou arquivo corrompido")
                    }
                }
            }
        }
        movedVar.value.toInt()
    }

    return outBuf.sliceArray(0 until moved)
}

actual fun saveBackupFile(bytes: ByteArray, fileName: String): String {
    val docs = NSFileManager.defaultManager.URLsForDirectory(
        NSDocumentDirectory, NSUserDomainMask
    ).firstOrNull() as? NSURL ?: throw IllegalStateException("Sem acesso ao Documents")

    val fileUrl = docs.URLByAppendingPathComponent(fileName)!!
    bytes.toNSData().writeToURL(fileUrl, atomically = true)
    return fileUrl.path ?: fileName
}

actual fun shareBackupFile(filePath: String) {
    val url = NSURL.fileURLWithPath(filePath)
    val controller = UIActivityViewController(listOf(url), null)
    UIApplication.sharedApplication.keyWindow?.rootViewController
        ?.presentViewController(controller, animated = true, completion = null)
}

// ── FilePicker ────────────────────────────────────────────────────────────────

@Composable
actual fun FilePicker(
    open: Boolean,
    onFileSelected: (ByteArray?) -> Unit,
    onDismiss: () -> Unit
) {
    // remember mantém o delegate vivo enquanto o composable existir
    val delegate = remember {
        object : NSObject(), UIDocumentPickerDelegateProtocol {
            override fun documentPicker(
                controller: UIDocumentPickerViewController,
                didPickDocumentsAtURLs: List<*>
            ) {
                val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL
                if (url == null) { onDismiss(); return }

                val scoped = url.startAccessingSecurityScopedResource()
                val data   = NSData.dataWithContentsOfURL(url)
                if (scoped) url.stopAccessingSecurityScopedResource()

                onFileSelected(data?.toByteArray())
            }

            override fun documentPickerWasCancelled(
                controller: UIDocumentPickerViewController
            ) { onDismiss() }
        }
    }

    LaunchedEffect(open) {
        if (!open) return@LaunchedEffect

        val picker = UIDocumentPickerViewController(
            documentTypes = listOf("public.data"),
            inMode = UIDocumentPickerMode.UIDocumentPickerModeImport
        ).apply { this.delegate = delegate }

        UIApplication.sharedApplication.keyWindow
            ?.rootViewController
            ?.presentViewController(picker, animated = true, completion = null)
    }
}

// ── Extensões ─────────────────────────────────────────────────────────────────

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray =
    ByteArray(length.toInt()).apply {
        usePinned { memcpy(it.addressOf(0), bytes, length) }
    }

@OptIn(ExperimentalForeignApi::class)
private fun ByteArray.toNSData(): NSData =
    usePinned { NSData.dataWithBytes(it.addressOf(0), size.toULong()) }