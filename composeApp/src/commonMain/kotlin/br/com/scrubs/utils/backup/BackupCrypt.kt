package br.com.scrubs.utils.backup

import androidx.compose.runtime.Composable

// ── Criptografia ──────────────────────────────────────────────────────────────

/**
 * Encripta [plaintext] com AES-256-GCM usando chave derivada de [password] via PBKDF2.
 * Formato do resultado: [ salt(16) | iv(12) | ciphertext ]
 */
expect fun encrypt(plaintext: ByteArray, password: String): ByteArray

/**
 * Decripta bytes no formato [ salt(16) | iv(12) | ciphertext ] usando [password].
 * Lança exceção se a senha estiver errada ou o arquivo corrompido.
 */
expect fun decrypt(data: ByteArray, password: String): ByteArray

// ── Arquivo ───────────────────────────────────────────────────────────────────

/**
 * Salva [bytes] como arquivo [fileName] na pasta de Downloads (Android) ou
 * Documents (iOS) e retorna o caminho absoluto do arquivo salvo.
 */
expect fun saveBackupFile(bytes: ByteArray, fileName: String): String

/**
 * Abre o share sheet nativo para compartilhar o arquivo no [filePath].
 */
expect fun shareBackupFile(filePath: String)

/**
 * Abre o file picker para o usuário selecionar um arquivo .scrubs.
 * O conteúdo é retornado via [onFileSelected].
 */
@Composable
expect fun FilePicker(
    open: Boolean,
    onFileSelected: (ByteArray?) -> Unit,
    onDismiss: () -> Unit
)