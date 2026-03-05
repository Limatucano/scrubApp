package br.com.scrubs.data.repository

import br.com.scrubs.data.local.ReceiptDao
import br.com.scrubs.domain.mapper.toEntity
import br.com.scrubs.domain.mapper.toModels
import br.com.scrubs.domain.model.Receipt
import br.com.scrubs.domain.model.Status
import br.com.scrubs.domain.repository.ReceiptRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class ReceiptRepositoryImpl(
    private val dao: ReceiptDao
): ReceiptRepository {
    override suspend fun getAll(): Flow<List<Receipt>> {
//        return dao.getAll().map { it.toModels() }

        return flowOf(
            listOf(
                Receipt(1, "Maria Silva Santos", "Unimed",       "Cirurgia Cardíaca",    750.00,  "14/03/2025", Status.PAID),
                Receipt(2, "João Pedro Oliveira",  "Bradesco",     "Cirurgia Ortopédica",  1200.00, "09/03/2025", Status.PENDING),
                Receipt(3, "Ana Carolina Souza",    "SulAmérica",   "Cirurgia Geral",       850.00,  "27/02/2025", Status.PAID),
                Receipt(4, "Carlos Eduardo Lima",  "Amil",         "Cirurgia Plástica",    1500.00, "19/02/2025", Status.PENDING),
                Receipt(5, "Fernanda Costa",       "Unimed",       "Cirurgia Bariátrica",  2200.00, "05/03/2025", Status.PAID),
                Receipt(6, "Ricardo Mendes",        "NotreDame",    "Cirurgia Vascular",    980.00,  "01/03/2025", Status.PENDING),
                Receipt(7, "Patrícia Alves",       "Bradesco",     "Cirurgia Oncológica",  3100.00, "22/02/2025", Status.PAID),
                Receipt(8, "Bruno Henrique Rocha", "Porto Seguro", "Cirurgia Neurológica", 4500.00, "11/03/2025", Status.PENDING),
            )
        )
    }

    override suspend fun save(receipt: Receipt) {
        dao.insert(receipt.toEntity())
    }

    override suspend fun remove(receipt: Receipt) {
        dao.delete(receipt.toEntity())
    }
}