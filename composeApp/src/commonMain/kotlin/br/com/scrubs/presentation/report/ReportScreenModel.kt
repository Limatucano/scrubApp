package br.com.scrubs.presentation.report

import br.com.scrubs.domain.model.Receipt
import br.com.scrubs.domain.repository.ReceiptRepository
import br.com.scrubs.presentation.confirmation.components.normalize
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MonthEntry(
    val yearMonth: String,   // "2026/03"
    val count: Int,
    val total: Double
)

data class HealthPlanReport(
    val healthPlan: String,
    val generalAverage: Double,
    val totalSurgeries: Int,
    val months: List<MonthEntry>
)

data class ReportState(
    val query: String = "",
    val procedureSuggestions: List<String> = emptyList(),
    val plans: List<HealthPlanReport> = emptyList(),
    val totalSurgeries: Int = 0,
    val isLoading: Boolean = false,
    val hasSearched: Boolean = false
)

class ReportScreenModel(
    private val repository: ReceiptRepository
) : ScreenModel {

    private val _state = MutableStateFlow(ReportState())
    val state: StateFlow<ReportState> = _state.asStateFlow()

    private var allReceipts: List<Receipt> = emptyList()

    init {
        screenModelScope.launch {
            allReceipts = repository.getAll().first()
            val suggestions = repository.getDistinctProcedures()
            _state.update { it.copy(procedureSuggestions = suggestions) }
        }
    }

    fun onQueryChanged(query: String) {
        _state.update { it.copy(query = query) }
        if (query.isBlank()) {
            _state.update { it.copy(plans = emptyList(), totalSurgeries = 0, hasSearched = false) }
            return
        }
        search(query)
    }

    private fun search(query: String) {
        _state.update { it.copy(isLoading = true, hasSearched = true) }

        val matched = allReceipts.filter {
            it.surgicalProcedure.normalize().contains(query.normalize())
        }

        // Group by health plan
        val byPlan = matched.groupBy { it.healthPlan }

        val plans = byPlan.map { (plan, receipts) ->
            val generalAverage = receipts.sumOf { it.value } / receipts.size

            // Group by year/month from surgicalDate "dd/MM/yyyy"
            val byMonth = receipts
                .groupBy { receipt ->
                    val parts = receipt.surgicalDate.split("/")
                    if (parts.size == 3) "${parts[2]}/${parts[1]}" else "??/????"
                }
                .entries
                .sortedByDescending { it.key }
                .map { (yearMonth, monthReceipts) ->
                    MonthEntry(
                        yearMonth = yearMonth,
                        count = monthReceipts.size,
                        total = monthReceipts.sumOf { it.value }
                    )
                }

            HealthPlanReport(
                healthPlan = plan,
                generalAverage = generalAverage,
                totalSurgeries = receipts.size,
                months = byMonth
            )
        }.sortedByDescending { it.totalSurgeries }

        _state.update {
            it.copy(
                plans = plans,
                totalSurgeries = matched.size,
                isLoading = false
            )
        }
    }
}