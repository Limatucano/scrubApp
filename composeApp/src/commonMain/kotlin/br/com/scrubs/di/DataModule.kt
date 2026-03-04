package br.com.scrubs.di

import br.com.scrubs.data.local.AppDatabase
import br.com.scrubs.data.repository.ReceiptRepositoryImpl
import br.com.scrubs.domain.repository.ReceiptRepository
import org.koin.dsl.module

val dataModule = module {
    single { get<AppDatabase>().receiptDao() }
    single<ReceiptRepository> { ReceiptRepositoryImpl(dao = get()) }
}