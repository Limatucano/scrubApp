package br.com.scrubs.domain.model

enum class DateFilter(val label: String) {
    ALL("Todas"),
    DAYS_30("30 Dias"),
    CUSTOM("Personalizado");

    companion object {
        fun customLabel(startMillis: Long, endMillis: Long): String =
            "${startMillis.toFormattedDate()} - ${endMillis.toFormattedDate()}"

        fun Long.toFormattedDate(): String {
            val (day, month, year) = toDateParts()
            return "${day.padStart(2, '0')}/${month.padStart(2, '0')}/$year"
        }

        private fun Long.toDateParts(): Triple<String, String, String> {
            var days = this / (24L * 60 * 60 * 1000)
            var year = 1970
            while (true) {
                val diy = if (isLeap(year)) 366 else 365
                if (days < diy) break
                days -= diy
                year++
            }
            val ml = intArrayOf(31, if (isLeap(year)) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
            var month = 1
            for (len in ml) {
                if (days < len) break
                days -= len
                month++
            }
            return Triple((days + 1).toString(), month.toString(), year.toString())
        }

        private fun isLeap(y: Int) = (y % 4 == 0 && y % 100 != 0) || (y % 400 == 0)
    }
}