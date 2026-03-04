package br.com.scrubs.domain.model

enum class DateFilter(val label: String) {
    DAYS_15("15 Dias"),
    DAYS_30("30 Dias"),
    MORE_THAN_30("> 30 Dias"),
    CUSTOM("Personalizado")
}
