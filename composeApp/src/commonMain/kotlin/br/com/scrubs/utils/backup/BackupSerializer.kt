package br.com.scrubs.utils.backup

import br.com.scrubs.domain.model.Receipt
import br.com.scrubs.domain.model.Status
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

// ── Formato do arquivo .scrubs ────────────────────────────────────────────────
// [ salt(16 bytes) ][ iv(12 bytes) ][ AES-256-GCM encrypted JSON ]
// A imagem é serializada como Base64 dentro do JSON

@OptIn(ExperimentalEncodingApi::class)
@Serializable
data class BackupReceipt(
    val id: Long,
    val patientName: String,
    val healthPlan: String,
    val surgicalProcedure: String,
    val value: Double,
    val surgicalDate: String,
    val status: String,
    val paymentDate: String? = null,
    val company: String = "",
    val imageBase64: String? = null   // ByteArray codificado em Base64
)

@Serializable
data class BackupFile(
    val version: Int = 1,
    val exportedAt: String,
    val receipts: List<BackupReceipt>
)

@OptIn(ExperimentalEncodingApi::class)
fun Receipt.toBackupReceipt() = BackupReceipt(
    id = id,
    patientName = patientName,
    healthPlan = healthPlan,
    surgicalProcedure = surgicalProcedure,
    value = value,
    surgicalDate = surgicalDate,
    status = status.name,
    paymentDate = paymentDate,
    company = company,
    imageBase64 = image?.let { Base64.encode(it) }  // ByteArray → Base64 String
)

@OptIn(ExperimentalEncodingApi::class)
fun BackupReceipt.toReceipt() = Receipt(
    id = 0, // Room gera novo id ao importar
    patientName = patientName,
    healthPlan = healthPlan,
    surgicalProcedure = surgicalProcedure,
    value = value,
    surgicalDate = surgicalDate,
    status = runCatching { Status.valueOf(status) }.getOrDefault(Status.PENDING),
    paymentDate = paymentDate,
    company = company,
    image = imageBase64?.let { Base64.decode(it) }  // Base64 String → ByteArray
)

val backupJson = Json { ignoreUnknownKeys = true; prettyPrint = false }

fun BackupFile.toJson(): String = backupJson.encodeToString(this)
fun String.toBackupFile(): BackupFile = backupJson.decodeFromString(this)