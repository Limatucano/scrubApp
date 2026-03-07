package br.com.scrubs.utils

import androidx.compose.ui.graphics.ImageBitmap

expect fun decodeByteArrayToImageBitmap(bytes: ByteArray): ImageBitmap