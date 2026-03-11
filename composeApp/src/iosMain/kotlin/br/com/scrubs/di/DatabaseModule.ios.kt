package br.com.scrubs.di

import br.com.scrubs.data.local.AppDatabase
import br.com.scrubs.getDatabase
import br.com.scrubs.presentation.biometric.BiometricAuthManager
import org.koin.dsl.module

actual fun databaseModule() = module {
    single<AppDatabase> { getDatabase() }
    single { BiometricAuthManager() }
}
