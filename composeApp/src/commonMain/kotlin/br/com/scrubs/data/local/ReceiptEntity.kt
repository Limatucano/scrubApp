package br.com.scrubs.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "receipts")
data class ReceiptEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val patientName: String,
    val healthPlan: String,
    val surgicalProcedure: String,
    val value: Double,
    val surgicalDate: String,
    val isPaid: Boolean,
    val paymentDate: String? = null,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    val imageBytes: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ReceiptEntity

        if (id != other.id) return false
        if (value != other.value) return false
        if (isPaid != other.isPaid) return false
        if (patientName != other.patientName) return false
        if (healthPlan != other.healthPlan) return false
        if (surgicalProcedure != other.surgicalProcedure) return false
        if (surgicalDate != other.surgicalDate) return false
        if (paymentDate != other.paymentDate) return false
        if (!imageBytes.contentEquals(other.imageBytes)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + value.hashCode()
        result = 31 * result + isPaid.hashCode()
        result = 31 * result + patientName.hashCode()
        result = 31 * result + healthPlan.hashCode()
        result = 31 * result + surgicalProcedure.hashCode()
        result = 31 * result + surgicalDate.hashCode()
        result = 31 * result + (paymentDate?.hashCode() ?: 0)
        result = 31 * result + (imageBytes?.contentHashCode() ?: 0)
        return result
    }
}