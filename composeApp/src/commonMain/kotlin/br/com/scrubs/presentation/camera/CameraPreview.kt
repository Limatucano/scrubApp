package br.com.scrubs.presentation.camera

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Preview nativo da câmera.
 *
 * @param modifier         Modifier do composable
 * @param isFlashOn        Liga/desliga tocha
 * @param isFrontCamera    Qual câmera usar
 * @param captureRequestId   Incrementa para disparar captura
 * @param onImageCaptured  Retorna ByteArray da imagem capturada
 */
@Composable
expect fun CameraPreview(
    modifier: Modifier = Modifier,
    isFlashOn: Boolean = false,
    isFrontCamera: Boolean = false,
    captureRequestId: Int = 0,
    onImageCaptured: (ByteArray) -> Unit
)