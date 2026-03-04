package br.com.scrubs.domain.model

data class Receipt(
    val id: Long = 0,
    val patientName: String,
    val gender: String,
    val age: Int,
    val healthPlan: String,
    val surgicalProcedure: String,
    val value: Double,
    val surgicalDate: String,
    val status: Status
)
