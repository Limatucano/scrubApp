package br.com.scrubs.presentation.backup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.scrubs.presentation.permission.AppPermission
import br.com.scrubs.presentation.permission.PermissionDeniedDialog
import br.com.scrubs.presentation.permission.requiresStoragePermission
import br.com.scrubs.utils.backup.FilePicker
import br.com.scrubs.utils.backup.shareBackupFile
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.jetbrains.compose.resources.painterResource
import scrubs.composeapp.generated.resources.Res
import scrubs.composeapp.generated.resources.arrow_back

class BackupScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = koinScreenModel<BackupScreenModel>()
        val state by screenModel.state.collectAsState()
        var showImportPasswordDialog by remember { mutableStateOf(false) }
        var pendingImportBytes by remember { mutableStateOf<ByteArray?>(null) }

        LaunchedEffect(Unit) {
            if (requiresStoragePermission()) {
                screenModel.requestPermission(
                    permissions = listOf(
                        AppPermission.WRITE_STORAGE,
                        AppPermission.STORAGE
                    )
                )
            }
        }

        if (showImportPasswordDialog && pendingImportBytes != null) {
            ImportPasswordDialog(
                onDismiss = {
                    showImportPasswordDialog = false
                    pendingImportBytes = null
                },
                onConfirm = { password ->
                    showImportPasswordDialog = false
                    screenModel.onEvent(
                        BackupEvent.ImportFileSelected(pendingImportBytes!!, password)
                    )
                    pendingImportBytes = null
                }
            )
        }

        if (state.showPermissionDialog) {
            PermissionDeniedDialog(
                onDismiss = screenModel::dismissPermissionDialog,
                onSettingsClick = screenModel::goToSetting
            )
        }

        if (state.error != null) {
            AlertDialog(
                onDismissRequest = { screenModel.onEvent(BackupEvent.DismissError) },
                title = { Text("Erro", fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E)) },
                text = { Text(state.error!!, fontSize = 14.sp, color = Color(0xFF5A5A8A)) },
                confirmButton = {
                    Button(
                        onClick = { screenModel.onEvent(BackupEvent.DismissError) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A4AE8))
                    ) { Text("OK", color = Color.White) }
                },
                containerColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            )
        }

        if (state.importSuccess) {
            AlertDialog(
                onDismissRequest = { screenModel.onEvent(BackupEvent.DismissImportSuccess) },
                title = { Text("Importação concluída!", fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        "${state.importCount} registro${if (state.importCount == 1) "" else "s"} importado${if (state.importCount == 1) "" else "s"} com sucesso.",
                        fontSize = 14.sp,
                        color = Color(0xFF5A5A8A)
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { screenModel.onEvent(BackupEvent.DismissImportSuccess) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A4AE8))
                    ) { Text("OK", color = Color.White) }
                },
                containerColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            )
        }

        FilePicker(
            open = state.filePickerOpen,
            onFileSelected = { bytes ->
                bytes?.let {
                    pendingImportBytes = it
                    showImportPasswordDialog = true
                    screenModel.onEvent(BackupEvent.DismissFilePicker)
                }
            },
            onDismiss = { screenModel.onEvent(BackupEvent.DismissFilePicker) }
        )

        BackupContent(
            state = state,
            onEvent = screenModel::onEvent,
            onBack = { navigator.pop() },
            onSelectFile = { screenModel.onEvent(BackupEvent.OpenFilePicker) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BackupContent(
    state: BackupState,
    onEvent: (BackupEvent) -> Unit,
    onBack: () -> Unit,
    onSelectFile: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Backup e Dados",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color(0xFF1A1A2E)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(Res.drawable.arrow_back),
                            contentDescription = null,
                            tint = Color(0xFF4A4AE8)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF4F4FB)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ── Card Exportar ─────────────────────────────────────
            ExportCard(state = state, onEvent = onEvent)

            // ── Banner de exportação concluída ────────────────────
            AnimatedVisibility(
                visible = state.exportedFilePath != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                state.exportedFilePath?.let { path ->
                    ExportSuccessBanner(
                        filePath = path,
                        onClick = { shareBackupFile(path) }
                    )
                }
            }

            // ── Card Importar ─────────────────────────────────────
            ImportCard(state = state, onSelectFile = onSelectFile)
        }
    }
}

@Composable
private fun ExportCard(state: BackupState, onEvent: (BackupEvent) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                IconCircle(icon = "⬇", tint = Color(0xFF4A4AE8))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "Exportar Dados",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF1A1A2E)
                    )
                    Text(
                        "Salve um arquivo com todos os seus registros de cirurgias no seu dispositivo. Ele será criptografado com uma senha de sua escolha.",
                        fontSize = 13.sp,
                        color = Color(0xFF8A8AAD),
                        lineHeight = 18.sp
                    )
                }
            }

            // Campo senha
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "Senha de Criptografia",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF5A5A8A)
                )
                OutlinedTextField(
                    value = state.exportPassword,
                    onValueChange = { onEvent(BackupEvent.ExportPasswordChanged(it)) },
                    placeholder = {
                        Text("Mínimo 6 caracteres", color = Color(0xFFCCCCCC), fontSize = 14.sp)
                    },
                    leadingIcon = {
                        Text("🔒", fontSize = 16.sp, modifier = Modifier.padding(start = 4.dp))
                    },
                    trailingIcon = {
                        Text(
                            text = if (state.exportPasswordVisible) "🙈" else "👁",
                            fontSize = 16.sp,
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .clickable { onEvent(BackupEvent.TogglePasswordVisibility) }
                        )
                    },
                    visualTransformation = if (state.exportPasswordVisible)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF4A4AE8),
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedContainerColor = Color(0xFFF8F8FF),
                        unfocusedContainerColor = Color(0xFFFAFAFA)
                    )
                )
            }

            // Botão exportar
            Button(
                onClick = { onEvent(BackupEvent.Export) },
                enabled = state.exportEnabled,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4A4AE8),
                    disabledContainerColor = Color(0xFF4A4AE8).copy(alpha = 0.4f)
                )
            ) {
                if (state.isExporting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "Exportar Backup",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun ExportSuccessBanner(filePath: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text("✅", fontSize = 18.sp)
                Text(
                    text = "Exportação concluída! Que tal garantir a segurança desses dados agora mesmo? Compartilhe o arquivo para a sua nuvem ou e-mail para ter um backup sempre acessível.",
                    fontSize = 13.sp,
                    color = Color(0xFF2E7D32),
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Row(
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "Clique aqui para compartilhar.",
                    fontSize = 14.sp,
                    color = Color(0xFF2E7D32),
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
private fun ImportCard(state: BackupState, onSelectFile: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                IconCircle(icon = "⬆", tint = Color(0xFF4A4AE8))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "Importar Dados",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF1A1A2E)
                    )
                    Text(
                        "Restaure seus dados a partir de um arquivo de backup previamente salvo no seu dispositivo.",
                        fontSize = 13.sp,
                        color = Color(0xFF8A8AAD),
                        lineHeight = 18.sp
                    )
                }
            }

            OutlinedButton(
                onClick = onSelectFile,
                enabled = !state.isImporting,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1A1A2E))
            ) {
                if (state.isImporting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color(0xFF4A4AE8),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "Selecionar Arquivo",
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ImportPasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var password by remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Senha do Backup", fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Digite a senha usada para criptografar este backup.",
                    fontSize = 14.sp,
                    color = Color(0xFF5A5A8A)
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text("Senha", color = Color(0xFFCCCCCC)) },
                    visualTransformation = if (visible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    trailingIcon = {
                        Text(
                            text = if (visible) "🙈" else "👁",
                            fontSize = 16.sp,
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .clickable { visible = !visible }
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF4A4AE8),
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (password.isNotBlank()) onConfirm(password) },
                enabled = password.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A4AE8))
            ) { Text("Importar", color = Color.White) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Color(0xFF8A8AAD))
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun IconCircle(icon: String, tint: Color) {
    Column(
        modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(tint.copy(alpha = 0.10f)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(icon, fontSize = 20.sp, color = tint, fontWeight = FontWeight.Bold)
    }
}