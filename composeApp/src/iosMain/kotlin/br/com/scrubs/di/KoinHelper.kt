package br.com.scrubs.di

import org.koin.mp.KoinPlatformTools

fun startKoinIos() {
    if (KoinPlatformTools.defaultContext().getOrNull() == null) {
        initKoin()
    }
}
