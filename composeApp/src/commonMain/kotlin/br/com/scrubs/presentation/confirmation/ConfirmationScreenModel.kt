package br.com.scrubs.presentation.confirmation

import androidx.compose.ui.graphics.ImageBitmap
import br.com.scrubs.domain.model.Receipt
import br.com.scrubs.domain.model.Status
import br.com.scrubs.domain.repository.ReceiptRepository
import br.com.scrubs.presentation.confirmation.components.normalize
import br.com.scrubs.utils.decodeByteArrayToImageBitmap
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class FormField {
    SURGICAL_DATE, PATIENT_NAME, PROCEDURE, HEALTH_PLAN, VALUE, PAYMENT_DATE, COMPANY
}

data class ConfirmationState(
    val imageBitmap: ImageBitmap? = null,
    val imageBytes: ByteArray? = null,
    val surgicalDate: String = "",
    val patientName: String = "",
    val procedure: String = "",
    val healthPlan: String = "",
    val value: String = "",
    val isPaid: Boolean = false,
    val paymentDate: String = "",
    val errors: Map<FormField, String> = emptyMap(),
    val isSaving: Boolean = false,
    val isLoading: Boolean = false,
    val healthPlanSuggestions: List<String> = emptyList(),
    val procedureSuggestions: List<String> = emptyList(),
    val companySuggestions: List<String> = emptyList(),
    val company: String = "",
    // Média de valor para a combinação healthPlan + procedure atual
    val suggestedValue: Double? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as ConfirmationState
        return isPaid == other.isPaid && isSaving == other.isSaving && isLoading == other.isLoading &&
                imageBitmap == other.imageBitmap && imageBytes.contentEquals(other.imageBytes) &&
                surgicalDate == other.surgicalDate && patientName == other.patientName &&
                procedure == other.procedure && healthPlan == other.healthPlan &&
                value == other.value && paymentDate == other.paymentDate &&
                errors == other.errors && healthPlanSuggestions == other.healthPlanSuggestions &&
                procedureSuggestions == other.procedureSuggestions &&
                companySuggestions == other.companySuggestions && company == other.company && suggestedValue == other.suggestedValue
    }

    override fun hashCode(): Int {
        var result = isPaid.hashCode()
        result = 31 * result + isSaving.hashCode()
        result = 31 * result + (imageBytes?.contentHashCode() ?: 0)
        result = 31 * result + surgicalDate.hashCode()
        result = 31 * result + procedure.hashCode()
        result = 31 * result + healthPlan.hashCode()
        result = 31 * result + value.hashCode()
        result = 31 * result + (suggestedValue?.hashCode() ?: 0)
        return result
    }
}

sealed class ConfirmationEvent {
    data class SurgicalDateChanged(val value: String) : ConfirmationEvent()
    data class PatientNameChanged(val value: String) : ConfirmationEvent()
    data class ProcedureChanged(val value: String) : ConfirmationEvent()
    data class HealthPlanChanged(val value: String) : ConfirmationEvent()
    data class ValueChanged(val value: String) : ConfirmationEvent()
    data class IsPaidChanged(val value: Boolean) : ConfirmationEvent()
    data class PaymentDateChanged(val value: String) : ConfirmationEvent()
    data class CompanyChanged(val value: String) : ConfirmationEvent()
    object ApplySuggestedValue : ConfirmationEvent()
    object Save : ConfirmationEvent()
    object RetakePhoto : ConfirmationEvent()
    object Delete : ConfirmationEvent()
}

sealed class ConfirmationNavigation {
    object GoBack : ConfirmationNavigation()
    object RetakePhoto : ConfirmationNavigation()
}

class ConfirmationScreenModel(
    private val initialReceipt: Receipt,
    private val repository: ReceiptRepository
) : ScreenModel {

    private val _state = MutableStateFlow(ConfirmationState())
    val state: StateFlow<ConfirmationState> = _state.asStateFlow()

    private val _navigation = MutableSharedFlow<ConfirmationNavigation>()
    val navigation = _navigation.asSharedFlow()

    private val _focusEvent = Channel<FormField>(Channel.BUFFERED)
    val focusEvent = _focusEvent.receiveAsFlow()

    // Todos os registros carregados uma vez — usados apenas para calcular média localmente
    private var allReceipts: List<Receipt> = emptyList()

    init {
        screenModelScope.launch {
            val bitmap = initialReceipt.image?.let { decodeByteArrayToImageBitmap(it) }
            val healthPlans = repository.getDistinctHealthPlans()
            val procedures = repository.getDistinctProcedures()

            // .first() coleta apenas a primeira emissão — não mantém a coroutine aberta
            allReceipts = repository.getAll().first()

            _state.update {
                it.copy(
                    imageBitmap = bitmap,
                    imageBytes = initialReceipt.image,
                    surgicalDate = initialReceipt.surgicalDate.filter { c -> c.isDigit() },
                    patientName = initialReceipt.patientName,
                    procedure = initialReceipt.surgicalProcedure,
                    healthPlan = initialReceipt.healthPlan,
                    value = (initialReceipt.value * 100).toLong().toString(),
                    isPaid = initialReceipt.status == Status.PAID,
                    paymentDate = initialReceipt.paymentDate?.filter { c -> c.isDigit() } ?: "",
                    healthPlanSuggestions = healthPlans,
                    procedureSuggestions = procedures,
                    companySuggestions = repository.getDistinctCompanies(),
                    company = initialReceipt.company,
                    isLoading = false
                )
            }

            // Calcula sugestão inicial caso receipt já tenha healthPlan + procedure preenchidos
            updateSuggestedValue()
        }
    }

    fun onEvent(event: ConfirmationEvent) {
        when (event) {
            is ConfirmationEvent.SurgicalDateChanged ->
                _state.update {
                    it.copy(
                        surgicalDate = event.value,
                        errors = it.errors - FormField.SURGICAL_DATE
                    )
                }

            is ConfirmationEvent.PatientNameChanged ->
                _state.update {
                    it.copy(
                        patientName = event.value,
                        errors = it.errors - FormField.PATIENT_NAME
                    )
                }

            is ConfirmationEvent.ProcedureChanged -> {
                _state.update {
                    it.copy(
                        procedure = event.value,
                        errors = it.errors - FormField.PROCEDURE
                    )
                }
                updateSuggestedValue()
            }

            is ConfirmationEvent.HealthPlanChanged -> {
                _state.update {
                    it.copy(
                        healthPlan = event.value,
                        errors = it.errors - FormField.HEALTH_PLAN
                    )
                }
                updateSuggestedValue()
            }

            is ConfirmationEvent.ValueChanged ->
                _state.update { it.copy(value = event.value, errors = it.errors - FormField.VALUE) }

            is ConfirmationEvent.IsPaidChanged ->
                _state.update {
                    it.copy(
                        isPaid = event.value,
                        paymentDate = if (!event.value) "" else it.paymentDate
                    )
                }

            is ConfirmationEvent.CompanyChanged ->
                _state.update {
                    it.copy(
                        company = event.value,
                        errors = it.errors - FormField.COMPANY
                    )
                }

            is ConfirmationEvent.PaymentDateChanged ->
                _state.update {
                    it.copy(
                        paymentDate = event.value,
                        errors = it.errors - FormField.PAYMENT_DATE
                    )
                }

            // Aplica o valor sugerido convertendo Double → centavos como String
            ConfirmationEvent.ApplySuggestedValue -> {
                val suggested = _state.value.suggestedValue ?: return
                val cents = (suggested * 100).toLong().toString()
                _state.update { it.copy(value = cents, errors = it.errors - FormField.VALUE) }
            }

            ConfirmationEvent.Save -> save()
            ConfirmationEvent.RetakePhoto -> screenModelScope.launch {
                _navigation.emit(ConfirmationNavigation.RetakePhoto)
            }

            ConfirmationEvent.Delete -> delete()
        }
    }

    /**
     * Calcula a média de valor dos registros que têm o mesmo healthPlan E procedure
     * (comparação normalizada — sem acentos, sem case).
     * Limpa a sugestão se um dos campos estiver vazio ou sem correspondência.
     */
    private fun updateSuggestedValue() {
        val current = _state.value
        val healthPlan = current.healthPlan.trim()
        val procedure = current.procedure.trim()

        if (healthPlan.isBlank() || procedure.isBlank()) {
            _state.update { it.copy(suggestedValue = null) }
            return
        }

        val matching = allReceipts.filter { receipt ->
            receipt.healthPlan.normalize() == healthPlan.normalize() &&
                    receipt.surgicalProcedure.normalize() == procedure.normalize() &&
                    receipt.id != initialReceipt.id  // exclui o próprio registro em caso de edição
        }

        val average = if (matching.isNotEmpty()) matching.sumOf { it.value } / matching.size
        else null

        _state.update { it.copy(suggestedValue = average) }
    }

    fun currentReceipt(): Receipt = _state.value.let { s ->
        initialReceipt.copy(
            patientName = s.patientName.trim(),
            healthPlan = s.healthPlan.trim(),
            surgicalProcedure = s.procedure.trim(),
            company = s.company.trim(),
            value = (s.value.toLongOrNull() ?: 0L) / 100.0,
            surgicalDate = s.surgicalDate.toFormattedDate(),
            paymentDate = if (s.isPaid) s.paymentDate.toFormattedDate() else null,
            status = if (s.isPaid) Status.PAID else Status.PENDING,
            image = s.imageBytes
        )
    }

    private fun delete() {
        screenModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            runCatching { repository.remove(currentReceipt()) }
                .onSuccess { _navigation.emit(ConfirmationNavigation.GoBack) }
                .onFailure {
                    _state.update { s ->
                        s.copy(
                            isSaving = false,
                            errors = mapOf(FormField.PATIENT_NAME to "Erro ao deletar. Tente novamente.")
                        )
                    }
                }
        }
    }

    private fun save() {
        val current = _state.value
        val errors = validate(current)

        if (errors.isNotEmpty()) {
            _state.update { it.copy(errors = errors) }
            val firstError = FormField.entries.firstOrNull { it in errors }
            if (firstError != null) screenModelScope.launch { _focusEvent.send(firstError) }
            return
        }

        screenModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            runCatching { repository.save(currentReceipt()) }
                .onSuccess { _navigation.emit(ConfirmationNavigation.GoBack) }
                .onFailure {
                    _state.update { s ->
                        s.copy(
                            isSaving = false,
                            errors = mapOf(FormField.PATIENT_NAME to "Erro ao salvar. Tente novamente.")
                        )
                    }
                }
        }
    }

    private fun validate(state: ConfirmationState): Map<FormField, String> {
        val errors = mutableMapOf<FormField, String>()
        if (state.surgicalDate.isBlank())
            errors[FormField.SURGICAL_DATE] = "Data da cirurgia obrigatória"
        else if (!isValidDateDigits(state.surgicalDate))
            errors[FormField.SURGICAL_DATE] = "Data inválida. Use dd/MM/aaaa"
        if (state.patientName.isBlank()) errors[FormField.PATIENT_NAME] =
            "Nome do paciente obrigatório"
        if (state.procedure.isBlank()) errors[FormField.PROCEDURE] = "Procedimento obrigatório"
        if (state.healthPlan.isBlank()) errors[FormField.HEALTH_PLAN] = "Plano de saúde obrigatório"
        if (state.company.isBlank()) errors[FormField.COMPANY] = "Empresa obrigatório"
        if (state.value.isBlank() || state.value.toLongOrNull() == null)
            errors[FormField.VALUE] = "Valor obrigatório"
        if (state.isPaid) {
            if (state.paymentDate.isBlank())
                errors[FormField.PAYMENT_DATE] = "Data do pagamento obrigatória"
            else if (!isValidDateDigits(state.paymentDate))
                errors[FormField.PAYMENT_DATE] = "Data inválida. Use dd/MM/aaaa"
        }
        return errors
    }

    private fun isValidDateDigits(digits: String): Boolean {
        if (digits.length != 8) return false
        val day = digits.substring(0, 2).toIntOrNull() ?: return false
        val month = digits.substring(2, 4).toIntOrNull() ?: return false
        val year = digits.substring(4, 8).toIntOrNull() ?: return false
        return day in 1..31 && month in 1..12 && year in 1900..2100
    }

    private fun String.toFormattedDate(): String =
        if (length == 8) "${substring(0, 2)}/${substring(2, 4)}/${substring(4, 8)}" else this
}