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

    @Update
    suspend fun update(receipt: ReceiptEntity)

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
}