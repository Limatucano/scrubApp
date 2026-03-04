package br.com.scrubs.presentation.camera

import br.com.scrubs.presentation.permission.AppPermission
import br.com.scrubs.presentation.permission.PermissionManager
import br.com.scrubs.presentation.permission.PermissionStatus
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.collections.get

data class CameraState(
    val isFlashOn: Boolean = false,
    val capturedImage: ByteArray? = null,
    val galleryImage: ByteArray? = null,
    val showPermissionDialog: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as CameraState

        if (isFlashOn != other.isFlashOn) return false
        if (!capturedImage.contentEquals(other.capturedImage)) return false
        if (!galleryImage.contentEquals(other.galleryImage)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = isFlashOn.hashCode()
        result = 31 * result + (capturedImage?.contentHashCode() ?: 0)
        result = 31 * result + (galleryImage?.contentHashCode() ?: 0)
        return result
    }
}

sealed class CameraEvent {
    object ToggleFlash : CameraEvent()
    data class ImageCaptured(val bytes: ByteArray) : CameraEvent() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as ImageCaptured

            if (!bytes.contentEquals(other.bytes)) return false

            return true
        }

        override fun hashCode(): Int {
            return bytes.contentHashCode()
        }
    }

    data class GalleryImageSelected(val bytes: ByteArray) : CameraEvent() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as GalleryImageSelected

            if (!bytes.contentEquals(other.bytes)) return false

            return true
        }

        override fun hashCode(): Int {
            return bytes.contentHashCode()
        }
    }
}

sealed class ActionTakeImage {
    object OpenCamera : ActionTakeImage()
    object OpenGallery : ActionTakeImage()
}

class CameraScreenModule(
    val permissionManager: PermissionManager
) : ScreenModel {

    private val _state = MutableStateFlow(CameraState())
    val state: StateFlow<CameraState> = _state.asStateFlow()

    private val _actionTakeImage = MutableSharedFlow<ActionTakeImage>()
    val actionTakeImage = _actionTakeImage.asSharedFlow()


    fun onEvent(event: CameraEvent) {
        when (event) {
            is CameraEvent.ToggleFlash -> {
                _state.update { it.copy(isFlashOn = !it.isFlashOn) }
            }
            is CameraEvent.ImageCaptured -> {
                _state.update { it.copy(capturedImage = event.bytes) }
            }
            is CameraEvent.GalleryImageSelected -> {
                _state.update { it.copy(galleryImage = event.bytes) }
            }
        }
    }

    fun openCamera() {
        requestPermission(
            permissions = listOf(AppPermission.CAMERA)
        ) {
            _actionTakeImage.emit(ActionTakeImage.OpenCamera)
        }
    }

    fun openGallery() {
        requestPermission(
            permissions = listOf(AppPermission.GALLERY)
        ) {
            _actionTakeImage.emit(ActionTakeImage.OpenGallery)
        }
    }

    fun requestPermission(
        permissions: List<AppPermission> = listOf(AppPermission.CAMERA, AppPermission.GALLERY),
        result: suspend () -> Unit = {}
    ) {
        screenModelScope.launch {
            permissionManager.request(
                permissions = permissions,
                blockSuccess = { status ->
                    val isGranted = status.values.any { it == PermissionStatus.GRANTED }
                    if (isGranted) {
                        result()
                    }
                },
                blockDenied = {
                    _state.update { it.copy(showPermissionDialog = true) }
                },
                blockDeniedAlways = {
                    _state.update { it.copy(showPermissionDialog = true) }
                }
            )
        }
    }

    fun dismissPermissionDialog() {
        _state.update { it.copy( showPermissionDialog = false) }
    }

    fun goToSetting() {
        _state.update { it.copy( showPermissionDialog = false) }
        permissionManager.openSettings()
    }
}