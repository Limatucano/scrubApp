package br.com.scrubs.presentation.camera

import br.com.scrubs.presentation.permission.AppPermission
import br.com.scrubs.presentation.permission.PermissionManager
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CameraState(
    val isFlashOn: Boolean = false,
    val isFrontCamera: Boolean = false,
    val captureTrigger: Int = 0,
    val galleryOpen: Boolean = false,
    val showPermissionDialog: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as CameraState
        return isFlashOn == other.isFlashOn &&
                isFrontCamera == other.isFrontCamera &&
                captureTrigger == other.captureTrigger &&
                galleryOpen == other.galleryOpen &&
                showPermissionDialog == other.showPermissionDialog
    }

    override fun hashCode(): Int {
        var result = isFlashOn.hashCode()
        result = 31 * result + isFrontCamera.hashCode()
        result = 31 * result + captureTrigger
        result = 31 * result + galleryOpen.hashCode()
        result = 31 * result + showPermissionDialog.hashCode()
        return result
    }
}

sealed class CameraEvent {
    object ToggleFlash : CameraEvent()
    object FlipCamera : CameraEvent()
    object Capture : CameraEvent()
    object OpenGallery : CameraEvent()
    object DismissGallery : CameraEvent()
    data class ImageCaptured(val bytes: ByteArray) : CameraEvent() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false
            return bytes.contentEquals((other as ImageCaptured).bytes)
        }
        override fun hashCode() = bytes.contentHashCode()
    }
    data class GalleryImageSelected(val bytes: ByteArray) : CameraEvent() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false
            return bytes.contentEquals((other as GalleryImageSelected).bytes)
        }
        override fun hashCode() = bytes.contentHashCode()
    }
}

class CameraScreenModule(
    val permissionManager: PermissionManager
) : ScreenModel {

    private val _state = MutableStateFlow(CameraState())
    val state: StateFlow<CameraState> = _state.asStateFlow()

    fun onEvent(event: CameraEvent) {
        when (event) {
            CameraEvent.ToggleFlash -> _state.update { it.copy(isFlashOn = !it.isFlashOn) }
            CameraEvent.FlipCamera -> _state.update { it.copy(isFrontCamera = !it.isFrontCamera) }
            CameraEvent.Capture -> openCamera()
            CameraEvent.OpenGallery -> openGallery()
            CameraEvent.DismissGallery -> _state.update { it.copy(galleryOpen = false) }
            is CameraEvent.ImageCaptured, is CameraEvent.GalleryImageSelected -> {
                _state.update { it.copy(galleryOpen = false) }
            }
        }
    }

    fun requestPermission(
        permissions: List<AppPermission> = listOf(AppPermission.CAMERA, AppPermission.GALLERY),
        result: suspend () -> Unit = {}
    ) {
        screenModelScope.launch {
            permissionManager.request(
                permissions = permissions,
                blockSuccess = {
                    result()
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

    private fun openCamera() {
        requestPermission(listOf(AppPermission.CAMERA)) {
            _state.update { it.copy(captureTrigger = it.captureTrigger + 1) }
        }
    }

    private fun openGallery() {
        screenModelScope.launch {
            _state.update { it.copy(galleryOpen = true) }
        }
    }

    fun dismissPermissionDialog() =
        _state.update { it.copy(showPermissionDialog = false) }

    fun goToSetting() {
        _state.update { it.copy(showPermissionDialog = false) }
        permissionManager.openSettings()
    }
}