package br.com.scrubs.presentation.biometric

sealed class BiometricResult {
    object Success : BiometricResult()
    object Cancelled : BiometricResult()
    object NoBiometricEnrolled : BiometricResult()  // nenhuma biometria cadastrada
    object NotAvailable : BiometricResult()          // hardware não suporta
    data class Error(val message: String) : BiometricResult()
}

expect class BiometricAuthManager {
    /**
     * Verifica se o dispositivo tem biometria/PIN configurado.
     */
    fun canAuthenticate(): BiometricStatus

    /**
     * Dispara o prompt de autenticação biométrica.
     * O resultado é entregue via [onResult].
     */
    fun authenticate(
        title: String,
        subtitle: String,
        onResult: (BiometricResult) -> Unit
    )

    /**
     * Abre as configurações de segurança do dispositivo para o usuário
     * cadastrar biometria ou PIN.
     */
    fun openSecuritySettings()
}

enum class BiometricStatus {
    AVAILABLE,           // biometria forte disponível e cadastrada
    PIN_ONLY,            // apenas PIN/padrão disponível (sem biometria cadastrada)
    NOT_ENROLLED,        // hardware ok mas nada cadastrado
    NOT_AVAILABLE        // sem hardware ou sem suporte
}