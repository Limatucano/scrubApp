package br.com.scrubs

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import br.com.scrubs.data.local.AppDatabase
import platform.Foundation.NSHomeDirectory

fun getDatabase(): AppDatabase {
    val dbPath = NSHomeDirectory() + "/receipts.db"
    return Room.databaseBuilder<AppDatabase>(
        name = dbPath
    )
        .setDriver(BundledSQLiteDriver())
        .build()
}