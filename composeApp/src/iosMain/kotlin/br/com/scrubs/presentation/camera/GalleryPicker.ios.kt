package br.com.scrubs.presentation.camera

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import kotlinx.cinterop.*
import platform.Foundation.*
import platform.PhotosUI.*
import platform.UIKit.*
import platform.darwin.NSObject
import platform.objc.OBJC_ASSOCIATION_RETAIN
import platform.objc.objc_setAssociatedObject

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun GalleryPicker(
    open: Boolean,
    onImageSelected: (ByteArray) -> Unit,
    onDismiss: () -> Unit
) {
    // remember mantém o delegate vivo enquanto o composable estiver na árvore
    val delegate = remember {
        PickerDelegate(onResult = { bytes ->
            if (bytes != null) onImageSelected(bytes) else onDismiss()
        })
    }

    LaunchedEffect(open) {
        if (!open) return@LaunchedEffect

        val rootVC = UIApplication.sharedApplication.keyWindow?.rootViewController
            ?: return@LaunchedEffect

        val config = PHPickerConfiguration()
        config.selectionLimit = 1
        config.filter = PHPickerFilter.imagesFilter

        val picker = PHPickerViewController(configuration = config)
        picker.delegate = delegate

        rootVC.presentViewController(picker, animated = true, completion = null)
    }
}

private class PickerDelegate(
    private val onResult: (ByteArray?) -> Unit
) : NSObject(), PHPickerViewControllerDelegateProtocol {

    @OptIn(ExperimentalForeignApi::class)
    override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
        picker.dismissViewControllerAnimated(true, completion = null)

        val result = didFinishPicking.firstOrNull() as? PHPickerResult
        if (result == null) {
            onResult(null)
            return
        }

        result.itemProvider.loadDataRepresentationForTypeIdentifier(
            "public.image"
        ) { data, error ->
            if (data != null && error == null) {
                val bytes = ByteArray(data.length.toInt()).also { arr ->
                    arr.usePinned { pinned ->
                        platform.posix.memcpy(pinned.addressOf(0), data.bytes, data.length)
                    }
                }
                onResult(bytes)
            } else {
                onResult(null)
            }
        }
    }
}