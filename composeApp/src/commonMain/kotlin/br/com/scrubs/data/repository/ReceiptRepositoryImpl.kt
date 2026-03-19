package br.com.scrubs.data.repository

import br.com.scrubs.data.local.ReceiptDao
import br.com.scrubs.domain.mapper.toEntity
import br.com.scrubs.domain.mapper.toModels
import br.com.scrubs.domain.model.DateFilter
import br.com.scrubs.domain.model.Receipt
import br.com.scrubs.domain.repository.ReceiptRepository
import br.com.scrubs.utils.backup.BackupReceipt
import br.com.scrubs.utils.backup.toReceipt
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Clock

class ReceiptRepositoryImpl(
    private val dao: ReceiptDao
) : ReceiptRepository {

    override suspend fun getReceipts(
        filter: DateFilter,
        customRange: Pair<Long, Long>?
    ): Flow<List<Receipt>> {
        if (filter == DateFilter.ALL) return getAll()

        val today = Clock.System.now().toEpochMilliseconds()
        val todayIso = today.toIsoDate()

        val (start, end) = when (filter) {
            DateFilter.DAYS_30 -> (today - 30.daysMillis()).toIsoDate() to todayIso
            DateFilter.CUSTOM  -> {
                val (s, e) = customRange ?: ((today - 30.daysMillis()) to today)
                s.toIsoDate() to e.toIsoDate()
            }
        }

        return dao.getByDateRange(start, end).map { it.toModels() }
    }

    override suspend fun getAll(): Flow<List<Receipt>> = dao.getAll().map { it.toModels() }
    override suspend fun saveAll(receipts: List<BackupReceipt>) = dao.insertAll(receipts.map { it.toReceipt().toEntity() })
    override suspend fun save(receipt: Receipt) = dao.insert(receipt.toEntity())
    override suspend fun remove(receipt: Receipt) = dao.delete(receipt.toEntity())
    override suspend fun getDistinctHealthPlans() = dao.getDistinctHealthPlans()
    override suspend fun getDistinctProcedures() = dao.getDistinctProcedures()
    override suspend fun getDistinctCompanies() = dao.getDistinctCompanies()

    private fun Int.daysMillis(): Long = this * 24L * 60 * 60 * 1000

    private fun Long.toIsoDate(): String {
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
        for (len in ml) { if (days < len) break; days -= len; month++ }
        return "$year-${month.toString().padStart(2, '0')}-${(days + 1).toString().padStart(2, '0')}"
    }

    private fun isLeap(y: Int) = (y % 4 == 0 && y % 100 != 0) || (y % 400 == 0)
}