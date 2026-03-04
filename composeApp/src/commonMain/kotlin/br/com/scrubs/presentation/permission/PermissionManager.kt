package br.com.scrubs.presentation.permission

import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionState
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.camera.CAMERA
import dev.icerock.moko.permissions.gallery.GALLERY

enum class AppPermission {
    CAMERA,
    GALLERY
}

enum class PermissionStatus {
    GRANTED,
    DENIED,
    NOT_DETERMINED,
    DENIED_ALWAYS
}

interface PermissionManager {
    val permissionsController: PermissionsController
    suspend fun request(
        permissions: List<AppPermission>,
        blockSuccess: suspend (Map<AppPermission, PermissionStatus>) -> Unit,
        blockDenied: () -> Unit,
        blockDeniedAlways: () -> Unit
    )
    fun openSettings()
}

class PermissionManagerImpl(
    override val permissionsController: PermissionsController
) : PermissionManager {

    private fun AppPermission.toMoko(): Permission = when(this) {
        AppPermission.CAMERA -> Permission.CAMERA
        AppPermission.GALLERY -> Permission.GALLERY
    }

    private fun PermissionState.toPermissionStatus(): PermissionStatus = when(this) {
        PermissionState.Granted -> PermissionStatus.GRANTED
        PermissionState.Denied,
        PermissionState.NotGranted -> PermissionStatus.DENIED
        PermissionState.NotDetermined -> PermissionStatus.NOT_DETERMINED
        PermissionState.DeniedAlways -> PermissionStatus.DENIED_ALWAYS
    }

    override suspend fun request(
        permissions: List<AppPermission>,
        blockSuccess: suspend (Map<AppPermission, PermissionStatus>) -> Unit,
        blockDenied: () -> Unit,
        blockDeniedAlways: () -> Unit
    ) {
        runCatching {
            permissions.forEach {
                permissionsController.providePermission(it.toMoko())
            }
            permissions.associateWith {
                permissionsController.getPermissionState(it.toMoko()).toPermissionStatus()
            }
        }.onSuccess { result ->
            blockSuccess(result)
        }.onFailure { ex ->
            when (ex) {
                is DeniedException -> blockDenied()
                is DeniedAlwaysException -> blockDeniedAlways()
            }
        }
    }

    override fun openSettings() {
        permissionsController.openAppSettings()
    }
}