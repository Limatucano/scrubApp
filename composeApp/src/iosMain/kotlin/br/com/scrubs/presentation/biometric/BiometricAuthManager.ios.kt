@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package br.com.scrubs.presentation.biometric

import platform.LocalAuthentication.LAContext
import platform.LocalAuthentication.LAPolicyDeviceOwnerAuthentication
import platform.LocalAuthentication.LAPolicyDeviceOwnerAuthenticationWithBiometrics
import platform.Foundation.NSError
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.Foundation.NSURL

actual class BiometricAuthManager {

    actual fun canAuthenticate(): BiometricStatus {
        val context = LAContext()
        var error: NSError? = null

        val hasBiometric = context.canEvaluatePolicy(
            LAPolicyDeviceOwnerAuthenticationWithBiometrics,
            error = null
        )

        return when {
            hasBiometric -> BiometricStatus.AVAILABLE
            context.canEvaluatePolicy(LAPolicyDeviceOwnerAuthentication, error = null) -> {
                // Tem PIN mas não biometria cadastrada
                BiometricStatus.PIN_ONLY
            }
            else -> BiometricStatus.NOT_ENROLLED
        }
    }

    actual fun authenticate(
        title: String,
        subtitle: String,
        onResult: (BiometricResult) -> Unit
    ) {
        val context = LAContext()

        val policy = when (canAuthenticate()) {
            BiometricStatus.AVAILABLE -> LAPolicyDeviceOwnerAuthenticationWithBiometrics
            BiometricStatus.PIN_ONLY  -> LAPolicyDeviceOwnerAuthentication
            else -> {
                onResult(BiometricResult.NoBiometricEnrolled)
                return
            }
        }

        context.evaluatePolicy(
            policy = policy,
            localizedReason = title
        ) { success, error ->
            when {
                success -> onResult(BiometricResult.Success)
                error != null -> {
                    val LAErrorUserCancel = -2L
                    val LAErrorUserFallback = -3L
                    val LAErrorSystemCancel = -4L
                    when (error.code) {
                        LAErrorUserCancel,
                        LAErrorSystemCancel -> onResult(BiometricResult.Cancelled)
                        LAErrorUserFallback -> onResult(BiometricResult.Cancelled)
                        else -> onResult(BiometricResult.Error(error.localizedDescription))
                    }
                }
                else -> onResult(BiometricResult.Error("Autenticação falhou"))
            }
        }
    }

    actual fun openSecuritySettings() {
        // iOS não permite ir direto para biometria — abre as configurações gerais do app
        NSURL.URLWithString(UIApplicationOpenSettingsURLString)?.let {
            UIApplication.sharedApplication.openURL(it)
        }
    }
}