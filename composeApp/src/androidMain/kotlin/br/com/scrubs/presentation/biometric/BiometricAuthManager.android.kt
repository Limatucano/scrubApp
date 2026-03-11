package br.com.scrubs.presentation.biometric

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat

actual class BiometricAuthManager(private val context: Context) {

    private val biometricManager = BiometricManager.from(context)

    actual fun canAuthenticate(): BiometricStatus {
        return when (biometricManager.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS              -> BiometricStatus.AVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                // Verifica se ao menos PIN está disponível
                when (biometricManager.canAuthenticate(DEVICE_CREDENTIAL)) {
                    BiometricManager.BIOMETRIC_SUCCESS -> BiometricStatus.PIN_ONLY
                    else                               -> BiometricStatus.NOT_ENROLLED
                }
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE,
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricStatus.NOT_AVAILABLE
            else -> BiometricStatus.NOT_AVAILABLE
        }
    }

    actual fun authenticate(
        title: String,
        subtitle: String,
        onResult: (BiometricResult) -> Unit
    ) {
        // Pega a activity atual via ActivityProvider — nunca usa Application context
        val activity = ActivityProvider.get() ?: run {
            onResult(BiometricResult.Error("Tela não disponível. Tente novamente."))
            return
        }

        val executor = ContextCompat.getMainExecutor(context)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onResult(BiometricResult.Success)
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                when (errorCode) {
                    BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                    BiometricPrompt.ERROR_USER_CANCELED -> onResult(BiometricResult.Cancelled)
                    BiometricPrompt.ERROR_NO_BIOMETRICS -> onResult(BiometricResult.NoBiometricEnrolled)
                    BiometricPrompt.ERROR_HW_NOT_PRESENT,
                    BiometricPrompt.ERROR_HW_UNAVAILABLE -> onResult(BiometricResult.NotAvailable)
                    else -> onResult(BiometricResult.Error(errString.toString()))
                }
            }

            override fun onAuthenticationFailed() {
                // Tentativa falhou mas ainda pode tentar — não emitir resultado aqui
            }
        }

        val prompt = BiometricPrompt(activity, executor, callback)

        // Permite biometria forte + PIN/padrão como fallback
        val authenticators = when (canAuthenticate()) {
            BiometricStatus.AVAILABLE -> BIOMETRIC_STRONG or DEVICE_CREDENTIAL
            BiometricStatus.PIN_ONLY  -> DEVICE_CREDENTIAL
            else -> {
                onResult(BiometricResult.NoBiometricEnrolled)
                return
            }
        }

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(authenticators)
            .build()

        prompt.authenticate(promptInfo)
    }

    actual fun openSecuritySettings() {
        val intent = Intent(Settings.ACTION_SECURITY_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}