package br.com.scrubs.di

import br.com.scrubs.domain.model.Receipt
import br.com.scrubs.presentation.camera.CameraScreenModule
import br.com.scrubs.presentation.confirmation.ConfirmationScreenModel
import br.com.scrubs.presentation.home.HomeScreenModel
import br.com.scrubs.presentation.permission.PermissionFactory
import br.com.scrubs.presentation.permission.PermissionManager
import br.com.scrubs.presentation.permission.PermissionManagerImpl
import br.com.scrubs.presentation.report.ReportScreenModel
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

    factory { (receipt: Receipt) ->
        ConfirmationScreenModel(
            initialReceipt = receipt,
            repository = get()
        )
    }

    factory { ReportScreenModel(repository = get()) }
}
