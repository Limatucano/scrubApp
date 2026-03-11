package br.com.scrubs.domain.model

data class SummaryData(
    val total: Double,
    val count: Int
)

fun List<Receipt>.calculate(status: Status): SummaryData {
    val filtered = this.filter { it.status == status }
    val total = filtered.sumOf { it.value }
    return SummaryData(total, filtered.size)
}
