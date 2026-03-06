package br.com.scrubs.presentation.home

import br.com.scrubs.domain.model.DateFilter
import br.com.scrubs.domain.model.Receipt
import br.com.scrubs.domain.model.Status
import br.com.scrubs.domain.repository.ReceiptRepository
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeState(
    val receipts: List<Receipt> = emptyList(),
    val selectedFilter: DateFilter = DateFilter.DAYS_30,
    val isLoading: Boolean = false
) {
    val totalPending: Double
        get() = receipts.sumOf { receipt -> if (receipt.status == Status.PENDING) receipt.value else 0.0 }

    val totalPaid: Double
        get() = receipts.sumOf { receipt -> if (receipt.status == Status.PAID) receipt.value else 0.0 }

}

sealed class HomeEvent {
    data class FilterChanged(val filter: DateFilter) : HomeEvent()
}

class HomeScreenModel(
    private val repository: ReceiptRepository
) : ScreenModel {
    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        loadReceipts()
    }

    private fun loadReceipts() {
        screenModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            repository.getAll().collect { list ->
                _state.update { it.copy(receipts = list, isLoading = false) }
            }
        }
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.FilterChanged -> {
                _state.update { it.copy(selectedFilter = event.filter) }
            }
        }
    }
}