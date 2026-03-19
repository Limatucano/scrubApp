package br.com.scrubs.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ReceiptDao {
    @Query("SELECT * FROM receipts ORDER BY id DESC")
    fun getAll(): Flow<List<ReceiptEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(receipt: ReceiptEntity)

    @Delete
    suspend fun delete(receipt: ReceiptEntity)

    @Query("""
        SELECT * FROM receipts
        WHERE (
            SUBSTR(surgicalDate, 7, 4) || '-' ||
            SUBSTR(surgicalDate, 4, 2) || '-' ||
            SUBSTR(surgicalDate, 1, 2)
        ) BETWEEN :startDate AND :endDate
        ORDER BY surgicalDate DESC
    """)
    fun getByDateRange(startDate: String, endDate: String): Flow<List<ReceiptEntity>>

    @Query("SELECT DISTINCT healthPlan FROM receipts WHERE healthPlan != '' ORDER BY healthPlan ASC")
    suspend fun getDistinctHealthPlans(): List<String>

    @Query("SELECT DISTINCT surgicalProcedure FROM receipts WHERE surgicalProcedure != '' ORDER BY surgicalProcedure ASC")
    suspend fun getDistinctProcedures(): List<String>

    @Query("SELECT DISTINCT company FROM receipts WHERE company != '' ORDER BY company ASC")
    suspend fun getDistinctCompanies(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(receipts: List<ReceiptEntity>)
}