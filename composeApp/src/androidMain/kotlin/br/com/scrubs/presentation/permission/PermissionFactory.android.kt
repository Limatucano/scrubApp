package br.com.scrubs.presentation.permission

import android.content.Context
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.compose.PermissionsControllerFactory
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
internal actual class PermissionFactory : KoinComponent {
    private val context by inject<Context>()
    actual fun getPermissionFactory(): PermissionsControllerFactory {
        return PermissionsControllerFactory{
            PermissionsController(context)
        }
    }
}