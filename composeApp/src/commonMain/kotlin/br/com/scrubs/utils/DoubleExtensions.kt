package br.com.scrubs.utils

import kotlin.math.roundToInt

fun Double.formatCurrency(): String {
    val absValue = kotlin.math.abs(this)
    val intPart = absValue.toLong()
    val decimalPart = ((absValue - intPart) * 100).roundToInt()

    val intFormatted = intPart.toString()
        .reversed()
        .chunked(3)
        .joinToString(".")
        .reversed()

    return "R\$ $intFormatted,${decimalPart.toString().padStart(2, '0')}"
}