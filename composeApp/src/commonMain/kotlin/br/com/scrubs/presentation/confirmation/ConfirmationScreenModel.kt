package br.com.scrubs.presentation.confirmation

import androidx.compose.ui.graphics.ImageBitmap
import br.com.scrubs.domain.model.Receipt
import br.com.scrubs.domain.model.Status
import br.com.scrubs.domain.repository.ReceiptRepository
import br.com.scrubs.utils.decodeByteArrayToImageBitmap
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class FormField {
    SURGICAL_DATE,
    PATIENT_NAME,
    PROCEDURE,
    HEALTH_PLAN,
    VALUE,
    PAYMENT_DATE
}

data class ConfirmationState(
    val imageBitmap: ImageBitmap? = null,
    val surgicalDate: String = "",
    val patientName: String = "",
    val procedure: String = "",
    val healthPlan: String = "",
    val age: String = "",
    val gender: String = "",
    val value: String = "",
    val isPaid: Boolean = false,
    val paymentDate: String = "",
    val errors: Map<FormField, String> = emptyMap(),
    val isSaving: Boolean = false
)

sealed class ConfirmationEvent {
    data class SurgicalDateChanged(val value: String) : ConfirmationEvent()
    data class PatientNameChanged(val value: String) : ConfirmationEvent()
    data class ProcedureChanged(val value: String) : ConfirmationEvent()
    data class HealthPlanChanged(val value: String) : ConfirmationEvent()
    data class ValueChanged(val value: String) : ConfirmationEvent()
    data class IsPaidChanged(val value: Boolean) : ConfirmationEvent()
    data class PaymentDateChanged(val value: String) : ConfirmationEvent()
    object Save : ConfirmationEvent()
}

sealed class ConfirmationNavigation {
    object GoBack : ConfirmationNavigation()
}

class ConfirmationScreenModel(
    private val imageBytes: ByteArray,
    private val repository: ReceiptRepository
) : ScreenModel {

    private val _state = MutableStateFlow(ConfirmationState())
    val state: StateFlow<ConfirmationState> = _state.asStateFlow()

    private val _navigation = MutableSharedFlow<ConfirmationNavigation>()
    val navigation = _navigation.asSharedFlow()

    // Channel para foco — garante entrega mesmo se a UI ainda não está coletando
    private val _focusEvent = Channel<FormField>(Channel.BUFFERED)
    val focusEvent = _focusEvent.receiveAsFlow()

    init {
        decodeImage()
    }

    private fun decodeImage() {
        screenModelScope.launch {
            val bitmap = decodeByteArrayToImageBitmap(imageBytes)
            _state.update { it.copy(imageBitmap = bitmap) }
        }
    }

    fun onEvent(event: ConfirmationEvent) {
        when (event) {
            is ConfirmationEvent.SurgicalDateChanged ->
                _state.update { it.copy(surgicalDate = event.value, errors = it.errors - FormField.SURGICAL_DATE) }
            is ConfirmationEvent.PatientNameChanged ->
                _state.update { it.copy(patientName = event.value, errors = it.errors - FormField.PATIENT_NAME) }
            is ConfirmationEvent.ProcedureChanged ->
                _state.update { it.copy(procedure = event.value, errors = it.errors - FormField.PROCEDURE) }
            is ConfirmationEvent.HealthPlanChanged ->
                _state.update { it.copy(healthPlan = event.value, errors = it.errors - FormField.HEALTH_PLAN) }
            is ConfirmationEvent.ValueChanged ->
                _state.update { it.copy(value = event.value, errors = it.errors - FormField.VALUE) }
            is ConfirmationEvent.IsPaidChanged ->
                _state.update { it.copy(isPaid = event.value, paymentDate = if (!event.value) "" else it.paymentDate) }
            is ConfirmationEvent.PaymentDateChanged ->
                _state.update { it.copy(paymentDate = event.value, errors = it.errors - FormField.PAYMENT_DATE) }
            ConfirmationEvent.Save -> save()
        }
    }

    private fun save() {
        val current = _state.value
        val errors = validate(current)

        if (errors.isNotEmpty()) {
            _state.update { it.copy(errors = errors) }
            val firstError = FormField.entries.firstOrNull { it in errors }
            if (firstError != null) {
                screenModelScope.launch { _focusEvent.send(firstError) }
            }
            return
        }

        screenModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            runCatching {
                repository.save(current.toReceipt())
            }.onSuccess {
                _navigation.emit(ConfirmationNavigation.GoBack)
            }.onFailure {
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
        else if (!isValidDate(state.surgicalDate))
            errors[FormField.SURGICAL_DATE] = "Data inválida. Use dd/MM/aaaa"

        if (state.patientName.isBlank())
            errors[FormField.PATIENT_NAME] = "Nome do paciente obrigatório"

        if (state.procedure.isBlank())
            errors[FormField.PROCEDURE] = "Procedimento obrigatório"

        if (state.healthPlan.isBlank())
            errors[FormField.HEALTH_PLAN] = "Plano de saúde obrigatório"

        if (state.value.isBlank() || state.value.toLongOrNull() == null)
            errors[FormField.VALUE] = "Valor obrigatório"

        if (state.isPaid) {
            if (state.paymentDate.isBlank())
                errors[FormField.PAYMENT_DATE] = "Data do pagamento obrigatória"
            else if (!isValidDate(state.paymentDate))
                errors[FormField.PAYMENT_DATE] = "Data inválida. Use dd/MM/aaaa"
        }

        return errors
    }

    private fun isValidDate(date: String): Boolean {
        if (!date.matches(Regex("""\d{2}/\d{2}/\d{4}"""))) return false
        val parts = date.split("/")
        val day = parts[0].toIntOrNull() ?: return false
        val month = parts[1].toIntOrNull() ?: return false
        val year = parts[2].toIntOrNull() ?: return false
        return day in 1..31 && month in 1..12 && year in 1900..2100
    }

    private fun ConfirmationState.toReceipt() = Receipt(
        patientName = patientName.trim(),
        healthPlan = healthPlan.trim(),
        surgicalProcedure = procedure.trim(),
        value = (value.toLongOrNull() ?: 0L) / 100.0,
        surgicalDate = surgicalDate,
        status = if (isPaid) Status.PAID else Status.PENDING
    )
}