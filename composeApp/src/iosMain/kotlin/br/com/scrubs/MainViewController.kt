package br.com.scrubs

import androidx.compose.ui.window.ComposeUIViewController
import br.com.scrubs.di.startKoinIos

fun MainViewController(): platform.UIKit.UIViewController {
    startKoinIos()
    return ComposeUIViewController { App() }
}