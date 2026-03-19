package br.com.scrubs.presentation.permission

import dev.icerock.moko.permissions.compose.PermissionsControllerFactory

internal expect class PermissionFactory() {
    fun getPermissionFactory(): PermissionsControllerFactory
}

expect fun requiresStoragePermission(): Boolean