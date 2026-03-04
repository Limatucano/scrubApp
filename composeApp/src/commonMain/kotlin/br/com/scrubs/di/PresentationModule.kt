package br.com.scrubs.di

import br.com.scrubs.presentation.home.HomeScreenModel
import org.koin.dsl.module
val presentationModule = module {
    factory { HomeScreenModel(repository = get()) }
}
