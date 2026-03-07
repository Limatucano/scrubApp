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

    private fun AppPermission.toMoko(): Permission? = when(this) {
        AppPermission.CAMERA -> Permission.CAMERA
        AppPermission.GALLERY -> null
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
        val results = mutableMapOf<AppPermission, PermissionStatus>()
        var hasDenied = false
        var hasDeniedAlways = false

        permissions.forEach { permission ->
            val mokoPermission = permission.toMoko()

            if (mokoPermission == null) {
                results[permission] = PermissionStatus.GRANTED
                return@forEach
            }

            runCatching {
                val currentState = permissionsController.getPermissionState(mokoPermission)
                if (currentState != PermissionState.Granted) {
                    permissionsController.providePermission(mokoPermission)
                }
                permissionsController.getPermissionState(mokoPermission).toPermissionStatus()
            }.onSuccess { status ->
                results[permission] = status
            }.onFailure { ex ->
                when (ex) {
                    is DeniedAlwaysException -> {
                        results[permission] = PermissionStatus.DENIED_ALWAYS
                        hasDeniedAlways = true
                    }
                    is DeniedException -> {
                        results[permission] = PermissionStatus.DENIED
                        hasDenied = true
                    }
                }
            }
        }

        val hasAnyGranted = results.values.any { it == PermissionStatus.GRANTED }
        when {
            hasAnyGranted -> blockSuccess(results)
            hasDeniedAlways -> blockDeniedAlways()
            hasDenied -> blockDenied()
        }
    }

    override fun openSettings() {
        permissionsController.openAppSettings()
    }
}