package br.com.scrubs.presentation.backup

import br.com.scrubs.domain.repository.ReceiptRepository
import br.com.scrubs.presentation.home.components.toFormattedDate
import br.com.scrubs.utils.backup.BackupFile
import br.com.scrubs.utils.backup.decrypt
import br.com.scrubs.utils.backup.encrypt
import br.com.scrubs.utils.backup.saveBackupFile
import br.com.scrubs.utils.backup.toBackupFile
import br.com.scrubs.utils.backup.toBackupReceipt
import br.com.scrubs.utils.backup.toJson
import br.com.scrubs.utils.backup.toReceipt
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock

data class BackupState(
    val exportPassword: String = "",
    val exportPasswordVisible: Boolean = false,
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    val exportedFilePath: String? = null,   // não nulo = exportação concluída
    val importSuccess: Boolean = false,
    val filePickerOpen: Boolean = false,
    val importCount: Int = 0,
    val error: String? = null
) {
    val exportEnabled: Boolean get() = exportPassword.length >= 6 && !isExporting
}

sealed class BackupEvent {
    data class ExportPasswordChanged(val value: String) : BackupEvent()
    object TogglePasswordVisibility : BackupEvent()
    object Export : BackupEvent()
    data class ImportFileSelected(val bytes: ByteArray, val password: String) : BackupEvent() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as ImportFileSelected

            if (!bytes.contentEquals(other.bytes)) return false
            if (password != other.password) return false

            return true
        }

        override fun hashCode(): Int {
            var result = bytes.contentHashCode()
            result = 31 * result + password.hashCode()
            return result
        }
    }

    object DismissError : BackupEvent()
    object DismissImportSuccess : BackupEvent()
    object DismissFilePicker : BackupEvent()
    object OpenFilePicker : BackupEvent()
}

class BackupScreenModel(
    private val repository: ReceiptRepository
) : ScreenModel {

    private val _state = MutableStateFlow(BackupState())
    val state: StateFlow<BackupState> = _state.asStateFlow()

    fun onEvent(event: BackupEvent) {
        when (event) {
            is BackupEvent.ExportPasswordChanged ->
                _state.update { it.copy(exportPassword = event.value, error = null) }

            BackupEvent.TogglePasswordVisibility ->
                _state.update { it.copy(exportPasswordVisible = !it.exportPasswordVisible) }

            BackupEvent.Export -> export()

            is BackupEvent.ImportFileSelected -> importBackup(event.bytes, event.password)

            BackupEvent.DismissError ->
                _state.update { it.copy(error = null) }

            BackupEvent.DismissImportSuccess ->
                _state.update { it.copy(importSuccess = false, importCount = 0) }

            BackupEvent.DismissFilePicker ->
                _state.update { it.copy(filePickerOpen = false) }
            BackupEvent.OpenFilePicker -> _state.update { it.copy(filePickerOpen = true) }
        }
    }

    private fun export() {
        screenModelScope.launch {
            _state.update { it.copy(isExporting = true, error = null, exportedFilePath = null) }

            runCatching {
                val receipts = repository.getAll().first()

                val dateStr = Clock.System.now()
                    .toEpochMilliseconds()
                    .toFormattedDate("-")

                val backup = BackupFile(
                    exportedAt = dateStr,
                    receipts = receipts.map { it.toBackupReceipt() }
                )

                val json = backup.toJson()
                val encrypted = encrypt(json.encodeToByteArray(), _state.value.exportPassword)
                val fileName = "scrubs_backup_$dateStr.scrubs"

                saveBackupFile(encrypted, fileName)
            }
                .onSuccess { filePath ->
                    _state.update {
                        it.copy(isExporting = false, exportedFilePath = filePath, exportPassword = "")
                    }
                }
                .onFailure { e ->
                    _state.update { it.copy(isExporting = false, error = e.message ?: "Erro ao exportar") }
                }
        }
    }

    private fun importBackup(bytes: ByteArray, password: String) {
        screenModelScope.launch {
            _state.update { it.copy(isImporting = true, error = null) }

            runCatching {
                val decrypted = decrypt(bytes, password)
                val json = decrypted.decodeToString()
                val backup = json.toBackupFile()
                backup.receipts.forEach { repository.save(it.toReceipt()) }
                backup.receipts.size
            }
                .onSuccess { count ->
                    _state.update {
                        it.copy(isImporting = false, importSuccess = true, importCount = count)
                    }
                }
                .onFailure { e ->
                    val message = when {
                        e.message?.contains("Senha") == true -> "Senha incorreta. Verifique e tente novamente."
                        else -> "Arquivo inválido ou corrompido."
                    }
                    _state.update { it.copy(isImporting = false, error = message) }
                }
        }
    }
}