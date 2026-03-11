package br.com.scrubs.presentation.biometric

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import scrubs.composeapp.generated.resources.Res
import scrubs.composeapp.generated.resources.arrow_back
import scrubs.composeapp.generated.resources.fingerprint

class BiometricScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val biometricManager = koinInject<BiometricAuthManager>()

        var showNoEnrollDialog by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        val status = remember { biometricManager.canAuthenticate() }

        // Dispara biometria automaticamente ao entrar na tela
        LaunchedEffect(Unit) {
            if (status == BiometricStatus.NOT_ENROLLED || status == BiometricStatus.NOT_AVAILABLE) {
                showNoEnrollDialog = true
            } else {
                triggerAuth(biometricManager) { result ->
                    when (result) {
                        is BiometricResult.Success     -> navigator.replace(HomeNavigationTarget())
                        is BiometricResult.Cancelled   -> { /* mantém na tela */ }
                        is BiometricResult.NoBiometricEnrolled -> showNoEnrollDialog = true
                        is BiometricResult.Error       -> errorMessage = result.message
                        else                           -> {}
                    }
                }
            }
        }

        if (showNoEnrollDialog) {
            NoEnrollDialog(
                onDismiss = { showNoEnrollDialog = false },
                onGoToSettings = {
                    showNoEnrollDialog = false
                    biometricManager.openSecuritySettings()
                }
            )
        }

        BiometricContent(
            status = status,
            errorMessage = errorMessage,
            onBiometricClick = {
                errorMessage = null
                triggerAuth(biometricManager) { result ->
                    when (result) {
                        is BiometricResult.Success           -> navigator.replace(HomeNavigationTarget())
                        is BiometricResult.NoBiometricEnrolled -> showNoEnrollDialog = true
                        is BiometricResult.Error             -> errorMessage = result.message
                        else                                 -> {}
                    }
                }
            },
            onUsePinClick = {
                errorMessage = null
                triggerAuth(biometricManager, forcePin = true) { result ->
                    when (result) {
                        is BiometricResult.Success -> navigator.replace(HomeNavigationTarget())
                        is BiometricResult.Error   -> errorMessage = result.message
                        else                       -> {}
                    }
                }
            }
        )
    }
}

@Composable
private fun BiometricContent(
    status: BiometricStatus,
    errorMessage: String?,
    onBiometricClick: () -> Unit,
    onUsePinClick: () -> Unit
) {
    val pulse = rememberInfiniteTransition(label = "pulse")
    val scale by pulse.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "scale"
    )
    Scaffold(
        containerColor = Color(0xFFF4F4FB)
    ) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFF8F8FF), Color(0xFFEEEEFF))
                    )
                )
        ) {
            Text(
                text = "Controle Cirúrgico",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A2E)
            )
            Text(
                text = "Gerenciamento profissional de cirurgias",
                fontSize = 14.sp,
                color = Color(0xFF8A8AAD),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(60.dp))

            Box(
                modifier = Modifier
                    .size(110.dp)
                    .scale(scale)
                    .shadow(12.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable(enabled = status != BiometricStatus.NOT_AVAILABLE) {
                        onBiometricClick()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    modifier = Modifier.size(64.dp),
                    painter = painterResource(Res.drawable.fingerprint),
                    contentDescription = null,
                    tint = Color(0xFF4A4AE8)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = when (status) {
                    BiometricStatus.AVAILABLE -> "Login com Biometria"
                    BiometricStatus.PIN_ONLY  -> "Login com PIN"
                    else                      -> "Autenticação indisponível"
                },
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A2E)
            )
            Text(
                text = "Toque no ícone para autenticar",
                fontSize = 13.sp,
                color = Color(0xFF8A8AAD)
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = errorMessage,
                    fontSize = 13.sp,
                    color = Color(0xFFD32F2F),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFFEEEE))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (status == BiometricStatus.AVAILABLE) {
                TextButton(onClick = onUsePinClick) {
                    Text(
                        text = "Usar senha em vez disso",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF4A4AE8)
                    )
                }
            }
        }
    }
}

@Composable
private fun NoEnrollDialog(
    onDismiss: () -> Unit,
    onGoToSettings: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Biometria necessária",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A2E)
            )
        },
        text = {
            Text(
                text = "Para acessar o app é necessário ter biometria (digital ou facial) ou PIN cadastrado no dispositivo.\n\nDeseja ir às configurações de segurança agora?",
                fontSize = 14.sp,
                color = Color(0xFF5A5A8A)
            )
        },
        confirmButton = {
            Button(
                onClick = onGoToSettings,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A4AE8))
            ) {
                Text("Ir para configurações", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Agora não", color = Color(0xFF8A8AAD))
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}

// ── Helpers ──────────────────────────────────────────────────────────────────

private fun triggerAuth(
    manager: BiometricAuthManager,
    forcePin: Boolean = false,
    onResult: (BiometricResult) -> Unit
) {
    manager.authenticate(
        title = if (forcePin) "Confirme sua identidade" else "Login com Biometria",
        subtitle = "Acesse o Control Cirúrgico",
        onResult = onResult
    )
}

/**
 * Troque por sua tela inicial real.
 * Exemplo: br.com.scrubs.presentation.home.HomeScreen()
 */
private fun HomeNavigationTarget() = br.com.scrubs.presentation.home.HomeScreen()