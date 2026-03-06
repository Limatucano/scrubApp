package br.com.scrubs.utils

import kotlinx.cinterop.*
import platform.Foundation.*
import platform.Photos.*
import platform.UIKit.*

actual fun cropAndRotateImage(bytes: ByteArray, isFront: Boolean): ByteArray {
    return bytes
}

@OptIn(ExperimentalForeignApi::class)
actual fun shareImage(bytes: ByteArray, fileName: String) {
    val nsData = bytes.usePinned { NSData.dataWithBytes(it.addressOf(0), bytes.size.toULong()) }
    val image = UIImage.imageWithData(nsData) ?: return

    val activityVC = UIActivityViewController(
        activityItems = listOf(image),
        applicationActivities = null
    )

    val rootVC = UIApplication.sharedApplication.keyWindow?.rootViewController ?: return
    rootVC.presentViewController(activityVC, animated = true, completion = null)
}

@OptIn(ExperimentalForeignApi::class)
actual fun saveImageToGallery(bytes: ByteArray, fileName: String): Boolean {
    val nsData = bytes.usePinned { NSData.dataWithBytes(it.addressOf(0), bytes.size.toULong()) }
    val image = UIImage.imageWithData(nsData) ?: return false

    var success = false
    PHPhotoLibrary.sharedPhotoLibrary().performChanges(
        changeBlock = { PHAssetCreationRequest.creationRequestForAssetFromImage(image) },
        completionHandler = { ok, _ -> success = ok }
    )
    return success
}