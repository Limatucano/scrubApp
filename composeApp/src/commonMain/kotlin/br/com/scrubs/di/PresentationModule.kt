package br.com.scrubs.di

import br.com.scrubs.presentation.camera.CameraScreenModule
import br.com.scrubs.presentation.confirmation.ConfirmationScreenModel
import br.com.scrubs.presentation.home.HomeScreenModel
import br.com.scrubs.presentation.permission.PermissionFactory
import br.com.scrubs.presentation.permission.PermissionManager
import br.com.scrubs.presentation.permission.PermissionManagerImpl
import org.koin.dsl.module
val presentationModule = module {
    single<PermissionManager> {
        PermissionManagerImpl(
            permissionsController = PermissionFactory()
                .getPermissionFactory()
                .createPermissionsController()
        )
    }
    factory {
        HomeScreenModel(
            repository = get()
        )
    }

    factory {
        CameraScreenModule(
            permissionManager = get()
        )
    }

    factory { (imageBytes: ByteArray) ->
        ConfirmationScreenModel(
            imageBytes = get(),
            repository = get()
        )
    }
}
