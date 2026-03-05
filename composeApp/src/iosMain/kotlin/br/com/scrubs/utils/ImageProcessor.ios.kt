package br.com.scrubs.utils

actual fun cropAndRotateImage(
    bytes: ByteArray,
    frameCenterX: Float,
    frameCenterY: Float,
    frameWidthRatio: Float,
    frameHeightRatio: Float
): ByteArray {
    return bytes
}