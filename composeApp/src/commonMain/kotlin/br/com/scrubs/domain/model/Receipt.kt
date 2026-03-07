package br.com.scrubs.domain.model

data class Receipt(
    val id: Long = 0,
    val patientName: String,
    val healthPlan: String,
    val surgicalProcedure: String,
    val value: Double,
    val surgicalDate: String,
    val status: Status,
    val paymentDate: String? = null,
    val image: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Receipt

        if (id != other.id) return false
        if (value != other.value) return false
        if (patientName != other.patientName) return false
        if (healthPlan != other.healthPlan) return false
        if (surgicalProcedure != other.surgicalProcedure) return false
        if (surgicalDate != other.surgicalDate) return false
        if (status != other.status) return false
        if (paymentDate != other.paymentDate) return false
        if (!image.contentEquals(other.image)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + value.hashCode()
        result = 31 * result + patientName.hashCode()
        result = 31 * result + healthPlan.hashCode()
        result = 31 * result + surgicalProcedure.hashCode()
        result = 31 * result + surgicalDate.hashCode()
        result = 31 * result + status.hashCode()
        result = 31 * result + (paymentDate?.hashCode() ?: 0)
        result = 31 * result + (image?.contentHashCode() ?: 0)
        return result
    }

}
