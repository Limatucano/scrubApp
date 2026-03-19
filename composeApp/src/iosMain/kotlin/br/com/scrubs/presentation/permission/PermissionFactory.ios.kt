package br.com.scrubs.presentation.permission

import dev.icerock.moko.permissions.compose.PermissionsControllerFactory
import dev.icerock.moko.permissions.ios.PermissionsController

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
internal actual class PermissionFactory actual constructor() {
    actual fun getPermissionFactory(): PermissionsControllerFactory {
        return PermissionsControllerFactory{
            PermissionsController()
        }
    }
}

actual fun requiresStoragePermission(): Boolean {
    return false
}