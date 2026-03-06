package br.com.scrubs.utils

fun String.parseToDayMonth(): Triple<String, String, String> {
    val months = mapOf(
        "01" to "JAN", "02" to "FEV", "03" to "MAR",
        "04" to "ABR", "05" to "MAI", "06" to "JUN",
        "07" to "JUL", "08" to "AGO", "09" to "SET",
        "10" to "OUT", "11" to "NOV", "12" to "DEZ"
    )

    return try {
        if (contains("/")) {
            // dd/MM/yyyy
            val parts = split("/")
            Triple(parts[0], months[parts[1]] ?: parts[1], parts[2])
        } else {
            // yyyy-MM-dd
            val parts = split("-")
            Triple(parts[2], months[parts[1]] ?: parts[1], parts[2])
        }
    } catch (_: Exception) {
        Triple("--", "---", "----")
    }
}