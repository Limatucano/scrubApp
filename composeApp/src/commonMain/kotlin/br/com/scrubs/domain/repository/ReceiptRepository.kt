package br.com.scrubs.domain.repository

import br.com.scrubs.domain.model.Receipt
import kotlinx.coroutines.flow.Flow

interface ReceiptRepository {
    suspend fun getAll(): Flow<List<Receipt>>
    suspend fun save(receipt: Receipt)
    suspend fun remove(receipt: Receipt)
}