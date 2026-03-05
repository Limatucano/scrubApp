package br.com.scrubs.utils

import androidx.compose.material3.Surface

/**
 * Recorta a imagem na região do scan frame e rotaciona 90° no sentido horário.
 *
 * @param bytes          ByteArray da imagem capturada
 * @param frameCenterX   Centro X do frame como fração da tela (0.0–1.0)
 * @param frameCenterY   Centro Y do frame como fração da tela (0.0–1.0)
 * @param frameWidthRatio  Largura do frame como fração da tela (0.0–1.0)
 * @param frameHeightRatio Altura do frame como fração da tela (0.0–1.0)
 */
expect fun cropAndRotateImage(
    bytes: ByteArray,
    frameCenterX: Float,
    frameCenterY: Float,
    frameWidthRatio: Float,
    frameHeightRatio: Float
): ByteArray