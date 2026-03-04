package br.com.scrubs.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "receipts")
data class ReceiptEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val patientName: String,
    val gender: String,
    val age: Int,
    val healthPlan: String,
    val surgicalProcedure: String,
    val value: Double,
    val surgicalDate: String,
    val isPaid: Boolean
)