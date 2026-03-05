package br.com.scrubs

import android.content.Context
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import br.com.scrubs.data.local.AppDatabase

fun getDatabase(context: Context): AppDatabase {
    return Room.databaseBuilder<AppDatabase>(
        context,
        name = "receipts.db"
    )
        .setDriver(BundledSQLiteDriver())
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()
}