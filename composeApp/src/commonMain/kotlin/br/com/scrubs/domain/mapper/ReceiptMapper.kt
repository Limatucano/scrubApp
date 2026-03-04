package br.com.scrubs.domain.mapper

import br.com.scrubs.data.local.ReceiptEntity
import br.com.scrubs.domain.model.Receipt
import br.com.scrubs.domain.model.Status
import br.com.scrubs.utils.formatCurrency
import br.com.scrubs.utils.parseToDayMonth

fun ReceiptEntity.toModel(): Receipt {
    return Receipt(
        id = id,
        patientName = patientName,
        gender = gender,
        age = age,
        healthPlan = healthPlan,
        surgicalProcedure = surgicalProcedure,
        value = value,
        surgicalDate = surgicalDate,
        status = if (isPaid) Status.PAID else Status.PENDING
    )
}

fun List<ReceiptEntity>.toModels(): List<Receipt> {
    return map { it.toModel() }
}

fun Receipt.toEntity(): ReceiptEntity {
    return ReceiptEntity(
        id = id,
        patientName = patientName,
        gender = gender,
        age = age,
        healthPlan = healthPlan,
        surgicalProcedure = surgicalProcedure,
        value = value,
        surgicalDate = surgicalDate,
        isPaid = status == Status.PAID
    )
}