package br.com.scrubs.presentation.camera

import android.content.Context
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.util.concurrent.Executors

@Composable
actual fun CameraPreview(
    modifier: Modifier,
    isFlashOn: Boolean,
    isFrontCamera: Boolean,
    captureRequestId: Int,
    onImageCaptured: (ByteArray) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }
    val imageCapture = remember { ImageCapture.Builder().build() }
    val executor = remember { Executors.newSingleThreadExecutor() }
    val camera = remember { mutableStateOf<androidx.camera.core.Camera?>(null) }

    LaunchedEffect(isFrontCamera) {
        bindCamera(
            context = context,
            lifecycleOwner = lifecycleOwner,
            previewView = previewView,
            imageCapture = imageCapture,
            isFrontCamera = isFrontCamera,
            onCameraReady = { camera.value = it }
        )
    }

    LaunchedEffect(isFlashOn) { camera.value?.cameraControl?.enableTorch(isFlashOn) }

    LaunchedEffect(captureRequestId) {
        if (captureRequestId == 0) return@LaunchedEffect
        imageCapture.takePicture(
            executor,
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    val bytes = image.planes[0].buffer.let { buffer ->
                        ByteArray(buffer.remaining()).also { buffer.get(it) }
                    }
                    image.close()
                    onImageCaptured(bytes)
                }
                override fun onError(exception: ImageCaptureException) {
                    Log.e("CameraPreview", "Capture error", exception)
                }
            }
        )
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier
    )
}

private fun bindCamera(
    context: Context,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    previewView: PreviewView,
    imageCapture: ImageCapture,
    isFrontCamera: Boolean,
    onCameraReady: (androidx.camera.core.Camera) -> Unit
) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    cameraProviderFuture.addListener({
        val cameraProvider = cameraProviderFuture.get()

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        val selector = if (isFrontCamera) CameraSelector.DEFAULT_FRONT_CAMERA
        else CameraSelector.DEFAULT_BACK_CAMERA

        try {
            cameraProvider.unbindAll()
            val camera = cameraProvider.bindToLifecycle(lifecycleOwner, selector, preview, imageCapture)
            onCameraReady(camera)
        } catch (e: Exception) {
            Log.e("CameraPreview", "Bind error", e)
        }
    }, ContextCompat.getMainExecutor(context))
}