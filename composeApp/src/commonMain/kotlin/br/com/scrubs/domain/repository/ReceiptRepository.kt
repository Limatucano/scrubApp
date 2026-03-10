package br.com.scrubs.domain.repository

import br.com.scrubs.domain.model.DateFilter
import br.com.scrubs.domain.model.Receipt
import kotlinx.coroutines.flow.Flow

interface ReceiptRepository {
    suspend fun getReceipts(filter: DateFilter, customRange: Pair<Long, Long>? = null): Flow<List<Receipt>>
    suspend fun save(receipt: Receipt)
    suspend fun remove(receipt: Receipt)
    suspend fun getDistinctHealthPlans(): List<String>
    suspend fun getDistinctProcedures(): List<String>
    suspend fun getAll(): Flow<List<Receipt>>
}