package br.com.scrubs.utils

fun Double.formatCurrency(): String {
    val absValue = kotlin.math.abs(this)
    val intPart = absValue.toLong()
    val decimalPart = ((absValue - intPart) * 100).toInt()

    val intFormatted = intPart.toString()
        .reversed()
        .chunked(3)
        .joinToString(".")
        .reversed()

    val decimalFormatted = decimalPart.toString().padStart(2, '0')
    return "R\$ $intFormatted,$decimalFormatted"
}