package br.com.scrubs.data.repository

import br.com.scrubs.data.local.ReceiptDao
import br.com.scrubs.domain.mapper.toEntity
import br.com.scrubs.domain.mapper.toModels
import br.com.scrubs.domain.model.Receipt
import br.com.scrubs.domain.repository.ReceiptRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ReceiptRepositoryImpl(
    private val dao: ReceiptDao
): ReceiptRepository {
    override suspend fun getAll(): Flow<List<Receipt>> = dao.getAll().map { it.toModels() }

    override suspend fun save(receipt: Receipt) = dao.insert(receipt.toEntity())

    override suspend fun remove(receipt: Receipt) = dao.delete(receipt.toEntity())
}