package br.com.scrubs.presentation.home

import br.com.scrubs.domain.model.DateFilter
import br.com.scrubs.domain.model.Receipt
import br.com.scrubs.domain.model.Status
import br.com.scrubs.domain.repository.ReceiptRepository
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeState(
    val receipts: List<Receipt> = emptyList(),
    val selectedFilter: DateFilter = DateFilter.DAYS_30,
    val customDateRange: Pair<Long, Long>? = null,
    val customFilterLabel: String = "Personalizado",
    val showDatePicker: Boolean = false,
    val isLoading: Boolean = false
) {
    val totalPending: Double
        get() = receipts.sumOf { receipt -> if (receipt.status == Status.PENDING) receipt.value else 0.0 }

    val totalPaid: Double
        get() = receipts.sumOf { receipt -> if (receipt.status == Status.PAID) receipt.value else 0.0 }

}

sealed class HomeEvent {
    data class FilterChanged(val filter: DateFilter) : HomeEvent()
    data class CustomDateSelected(val startMillis: Long, val endMillis: Long) : HomeEvent()
}

@OptIn(ExperimentalCoroutinesApi::class)
class HomeScreenModel(
    private val repository: ReceiptRepository
) : ScreenModel {
    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()
    private val selectedFilter = MutableStateFlow(DateFilter.DAYS_30)
    private val customDateRange = MutableStateFlow<Pair<Long, Long>?>(null)
    init {
        combine(selectedFilter, customDateRange) { filter, range -> filter to range }
            .flatMapLatest { (filter, range) ->
                repository.getReceipts(filter, range)
            }
            .onEach { receipts ->
                _state.update { it.copy(receipts = receipts, isLoading = false) }
            }
            .launchIn(screenModelScope)
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.FilterChanged -> {
                if (event.filter == DateFilter.CUSTOM) {
                    _state.update { it.copy(selectedFilter = event.filter, showDatePicker = true) }
                    selectedFilter.value = event.filter
                } else {
                    customDateRange.value = null
                    selectedFilter.value = event.filter
                    _state.update {
                        it.copy(
                            selectedFilter = event.filter,
                            customDateRange = null,
                            customFilterLabel = "Personalizado",
                            isLoading = true,
                            showDatePicker = false
                        )
                    }
                }
            }

            is HomeEvent.CustomDateSelected -> {
                val range = Pair(event.startMillis, event.endMillis)
                val label = DateFilter.customLabel(event.startMillis, event.endMillis)
                customDateRange.value = range
                _state.update {
                    it.copy(
                        showDatePicker = false,
                        customDateRange = range,
                        customFilterLabel = label,
                        isLoading = true
                    )
                }
            }
        }
    }

    fun dismissDatePicker() {
        val hasRange = customDateRange.value != null
        if (!hasRange) {
            selectedFilter.value = DateFilter.DAYS_30
            _state.update {
                it.copy(
                    showDatePicker = false,
                    selectedFilter = DateFilter.DAYS_30
                )
            }
        } else {
            _state.update { it.copy(showDatePicker = false) }
        }
    }
}