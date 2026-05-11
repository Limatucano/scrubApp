package br.com.scrubs.presentation.camera

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.runtime.*
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.*
import platform.AVFoundation.*
import platform.CoreGraphics.CGRectZero
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.QuartzCore.CATransaction
import platform.UIKit.UIView
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun CameraPreview(
    modifier: Modifier,
    isFlashOn: Boolean,
    isFrontCamera: Boolean,
    captureRequestId: Int,
    onImageCaptured: (ByteArray) -> Unit
) {
    val session = remember { AVCaptureSession() }
    val photoOutput = remember { AVCapturePhotoOutput() }
    var currentDevice by remember { mutableStateOf<AVCaptureDevice?>(null) }

    LaunchedEffect(isFrontCamera) {
        session.beginConfiguration()
        var configured = false
        try {
            session.inputs.forEach { session.removeInput(it as AVCaptureInput) }

            val position = if (isFrontCamera) AVCaptureDevicePositionFront
            else AVCaptureDevicePositionBack

            val device = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)
                ?: return@LaunchedEffect

            currentDevice = device

            val input = AVCaptureDeviceInput.deviceInputWithDevice(device, null)
                ?: return@LaunchedEffect

            if (session.canAddInput(input)) session.addInput(input)
            if (!session.outputs.contains(photoOutput) && session.canAddOutput(photoOutput)) {
                session.addOutput(photoOutput)
            }
            configured = true
        } finally {
            session.commitConfiguration()
        }

        if (configured && !session.isRunning()) {
            session.startRunning()
        }
    }

    LaunchedEffect(isFlashOn) {
        currentDevice?.let { device ->
            if (device.hasTorch) {
                try {
                    device.lockForConfiguration(null)
                    device.torchMode = if (isFlashOn) AVCaptureTorchModeOn else AVCaptureTorchModeOff
                    device.unlockForConfiguration()
                } catch (_: Exception) {}
            }
        }
    }

    LaunchedEffect(captureRequestId) {
        if (captureRequestId == 0) return@LaunchedEffect
        val settings = AVCapturePhotoSettings.photoSettings()
        val delegate = PhotoDelegate { bytes -> onImageCaptured(bytes) }
        photoOutput.capturePhotoWithSettings(settings, delegate)
    }

    UIKitView(
        modifier = modifier,
        factory = {
            val container = UIView(frame = CGRectZero.readValue())

            val previewLayer = AVCaptureVideoPreviewLayer(session = session)
            previewLayer.videoGravity = AVLayerVideoGravityResizeAspectFill

            CATransaction.begin()
            CATransaction.setDisableActions(true)
            container.layer.addSublayer(previewLayer)
            CATransaction.commit()

            container.setNeedsLayout()
            container
        },
        update = { view ->
            view.layer.sublayers
                ?.filterIsInstance<AVCaptureVideoPreviewLayer>()
                ?.firstOrNull()
                ?.setFrame(view.bounds)
        },
        onRelease = {
            if (session.isRunning()) {
                session.stopRunning()
            }
        }
    )
}

private class PhotoDelegate(
    private val onCapture: (ByteArray) -> Unit
) : NSObject(), AVCapturePhotoCaptureDelegateProtocol {

    @OptIn(ExperimentalForeignApi::class)
    override fun captureOutput(
        output: AVCapturePhotoOutput,
        didFinishProcessingPhoto: AVCapturePhoto,
        error: NSError?
    ) {
        if (error != null) return
        val data: NSData = didFinishProcessingPhoto.fileDataRepresentation() ?: return
        val bytes = ByteArray(data.length.toInt()).also { bytes ->
            bytes.usePinned { pinned ->
                platform.posix.memcpy(pinned.addressOf(0), data.bytes, data.length)
            }
        }
        onCapture(bytes)
    }
}