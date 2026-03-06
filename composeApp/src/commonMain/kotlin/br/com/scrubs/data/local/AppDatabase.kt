package br.com.scrubs.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ReceiptEntity::class], version = 3)
abstract class AppDatabase : RoomDatabase() {
    abstract fun receiptDao(): ReceiptDao
}