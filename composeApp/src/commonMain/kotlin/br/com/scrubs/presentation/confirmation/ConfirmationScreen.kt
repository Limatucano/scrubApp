package br.com.scrubs.presentation.confirmation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.scrubs.domain.model.Receipt
import br.com.scrubs.presentation.camera.CameraScreen
import br.com.scrubs.presentation.confirmation.components.AutoCompleteField
import br.com.scrubs.utils.CurrencyVisualTransformation
import br.com.scrubs.utils.MaskVisualTransformation
import br.com.scrubs.utils.formatCurrency
import br.com.scrubs.utils.saveImageToGallery
import br.com.scrubs.utils.shareImage
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.attafitamim.krop.core.crop.AspectRatio
import com.attafitamim.krop.core.crop.CircleCropShape
import com.attafitamim.krop.core.crop.CropError
import com.attafitamim.krop.core.crop.CropResult
import com.attafitamim.krop.core.crop.RectCropShape
import com.attafitamim.krop.core.crop.StarCropShape
import com.attafitamim.krop.core.crop.TriangleCropShape
import com.attafitamim.krop.core.crop.crop
import com.attafitamim.krop.core.crop.cropperStyle
import com.attafitamim.krop.core.crop.rememberImageCropper
import com.attafitamim.krop.ui.ImageCropperDialog
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.koin.core.parameter.parametersOf
import scrubs.composeapp.generated.resources.Res
import scrubs.composeapp.generated.resources.arrow_back
import scrubs.composeapp.generated.resources.delete

data class ConfirmationScreen(val receipt: Receipt) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = koinScreenModel<ConfirmationScreenModel> { parametersOf(receipt) }
        val state by screenModel.state.collectAsState()
        val scrollState = rememberScrollState()
        val scope = rememberCoroutineScope()
        var showImagePreview by remember { mutableStateOf(false) }
        val imageCropper = rememberImageCropper()
        val cropState = imageCropper.cropState

        val focusSurgicalDate = remember { FocusRequester() }
        val focusPatientName = remember { FocusRequester() }
        val focusProcedure = remember { FocusRequester() }
        val focusHealthPlan = remember { FocusRequester() }
        val focusValue = remember { FocusRequester() }
        val focusPaymentDate = remember { FocusRequester() }
        val focusCompany = remember { FocusRequester() }

        LaunchedEffect(Unit) {
            screenModel.navigation.collect { nav ->
                when (nav) {
                    ConfirmationNavigation.GoBack -> navigator.pop()
                    ConfirmationNavigation.RetakePhoto -> {
                        navigator.replace(CameraScreen(existingReceipt = screenModel.currentReceipt()))
                    }
                }
            }
        }

        LaunchedEffect(Unit) {
            screenModel.focusEvent.collect { field ->
                val requester = when (field) {
                    FormField.SURGICAL_DATE -> focusSurgicalDate
                    FormField.PATIENT_NAME -> focusPatientName
                    FormField.PROCEDURE -> focusProcedure
                    FormField.HEALTH_PLAN -> focusHealthPlan
                    FormField.VALUE -> focusValue
                    FormField.PAYMENT_DATE -> focusPaymentDate
                    FormField.COMPANY -> focusCompany
                }
                requester.requestFocus()
                scope.launch { scrollState.animateScrollTo(0) }
            }
        }

        state.imageBitmap.takeIf { showImagePreview }?.let { bitmap ->
            FullScreenImagePreview(
                bitmap = bitmap,
                onDismiss = { showImagePreview = false },
                onShare = { state.imageBytes?.let { shareImage(it, "etiqueta_cirurgica.jpg") } },
                onDownload = { state.imageBytes?.let { saveImageToGallery(it) } },
                onCropImage = {
                    scope.launch {
                        when(val result = imageCropper.crop(bitmap)) {
                            CropResult.Cancelled -> {}
                            is CropError -> {}
                            is CropResult.Success -> {
                                screenModel.onEvent(ConfirmationEvent.ImageChanged(result.bitmap))
                            }
                        }
                    }
                }
            )
        }

        if (cropState != null) {
            ImageCropperDialog(
                state = cropState,
                style = cropperStyle(
                    shapes = listOf(RectCropShape, CircleCropShape, TriangleCropShape, StarCropShape),
                    aspects = listOf(AspectRatio(16, 9), AspectRatio(1, 1)),
                )
            )
        }

        Scaffold(
            topBar = {
                ConfirmationTopBar(
                    isEditing = receipt.patientName.isNotBlank(),
                    onBack = { navigator.pop() },
                    onDelete = { screenModel.onEvent(ConfirmationEvent.Delete) }
                )
            },
            bottomBar = {
                ConfirmationBottomBar(
                    onCancel = { navigator.pop() },
                    onSave = { screenModel.onEvent(ConfirmationEvent.Save) },
                    isSaving = state.isSaving
                )
            },
            containerColor = Color(0xFFF4F4FB)
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ImagePreviewCard(
                    bitmap = state.imageBitmap,
                    onRetakePhoto = { screenModel.onEvent(ConfirmationEvent.RetakePhoto) },
                    onImageClick = { showImagePreview = true }
                )

                FormSection(title = "Informações da Cirurgia", accentColor = Color(0xFF4A4AE8)) {
                    FormDateField(
                        label = "Data da Cirurgia",
                        value = state.surgicalDate,
                        error = state.errors[FormField.SURGICAL_DATE],
                        focusRequester = focusSurgicalDate,
                        nextFocusRequester = focusPatientName,
                        onValueChange = {
                            screenModel.onEvent(
                                ConfirmationEvent.SurgicalDateChanged(
                                    it
                                )
                            )
                        }
                    )
                    FormTextField(
                        label = "Nome do Paciente",
                        value = state.patientName,
                        error = state.errors[FormField.PATIENT_NAME],
                        focusRequester = focusPatientName,
                        nextFocusRequester = focusProcedure,
                        capitalization = KeyboardCapitalization.Words,
                        onValueChange = {
                            screenModel.onEvent(
                                ConfirmationEvent.PatientNameChanged(
                                    it
                                )
                            )
                        }
                    )
                    AutoCompleteField(
                        label = "Procedimento",
                        value = state.procedure,
                        suggestions = state.procedureSuggestions,
                        error = state.errors[FormField.PROCEDURE],
                        focusRequester = focusProcedure,
                        nextFocusRequester = focusHealthPlan,
                        capitalization = KeyboardCapitalization.Sentences,
                        onValueChange = { screenModel.onEvent(ConfirmationEvent.ProcedureChanged(it)) }
                    )
                    AutoCompleteField(
                        label = "Plano de Saúde",
                        value = state.healthPlan,
                        suggestions = state.healthPlanSuggestions,
                        error = state.errors[FormField.HEALTH_PLAN],
                        focusRequester = focusHealthPlan,
                        nextFocusRequester = focusCompany,
                        capitalization = KeyboardCapitalization.Words,
                        onValueChange = { screenModel.onEvent(ConfirmationEvent.HealthPlanChanged(it)) }
                    )
                    AutoCompleteField(
                        label = "Empresa Responsável",
                        value = state.company,
                        suggestions = state.companySuggestions,
                        error = state.errors[FormField.COMPANY],
                        focusRequester = focusCompany,
                        nextFocusRequester = focusValue,
                        capitalization = KeyboardCapitalization.Words,
                        onValueChange = { screenModel.onEvent(ConfirmationEvent.CompanyChanged(it)) }
                    )
                }

                FormSection(title = "Informações de Pagamento", accentColor = Color(0xFFD32F2F)) {
                    FormCurrencyField(
                        label = "Valor da Cirurgia",
                        value = state.value,
                        error = state.errors[FormField.VALUE],
                        suggestedValue = state.suggestedValue,
                        onApplySuggestion = { screenModel.onEvent(ConfirmationEvent.ApplySuggestedValue) },
                        focusRequester = focusValue,
                        onValueChange = { screenModel.onEvent(ConfirmationEvent.ValueChanged(it)) }
                    )
                    PaidToggle(
                        isPaid = state.isPaid,
                        onToggle = { screenModel.onEvent(ConfirmationEvent.IsPaidChanged(it)) }
                    )
                    AnimatedVisibility(
                        visible = state.isPaid,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        FormDateField(
                            label = "Data do Pagamento *",
                            value = state.paymentDate,
                            error = state.errors[FormField.PAYMENT_DATE],
                            focusRequester = focusPaymentDate,
                            nextFocusRequester = null,
                            onValueChange = {
                                screenModel.onEvent(
                                    ConfirmationEvent.PaymentDateChanged(
                                        it
                                    )
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConfirmationTopBar(
    isEditing: Boolean = false,
    onBack: () -> Unit,
    onDelete: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = if (isEditing) "Editar Cirurgia" else "Salvar Cirurgia",
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
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
        actions = {
            if (isEditing) {
                IconButton(onClick = onDelete) {
                    Icon(
                        painter = painterResource(Res.drawable.delete),
                        contentDescription = null,
                        tint = Color(0xFF4A4AE8)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
    )
}

@Composable
private fun ConfirmationBottomBar(onCancel: () -> Unit, onSave: () -> Unit, isSaving: Boolean) {
    Surface(shadowElevation = 8.dp, color = Color.White) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)
                .navigationBarsPadding()
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Color(0xFFDDDDDD)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF5A5A8A))
            ) { Text("Cancelar", fontWeight = FontWeight.Medium) }
            Button(
                onClick = onSave, enabled = !isSaving, modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2E7D32),
                    contentColor = Color.White
                )
            ) {
                if (isSaving) CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                else Text("Salvar Cirurgia", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun ImagePreviewCard(
    bitmap: ImageBitmap?,
    onImageClick: () -> Unit,
    onRetakePhoto: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(12.dp)
            .clickable(enabled = bitmap != null) { onImageClick() }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(72.dp).clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF0F0FA))
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Imagem Selecionada",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1A1A2E)
            )
            Text(
                text = "Etiqueta capturada da cirurgia",
                fontSize = 12.sp,
                color = Color(0xFF8A8AAD)
            )
            Spacer(modifier = Modifier.height(2.dp))
            OutlinedButton(
                onClick = onRetakePhoto,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color(0xFF4A4AE8)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF4A4AE8))
            ) {
                Text(
                    text = "Tirar Novamente",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun FormSection(
    title: String,
    accentColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier.width(3.dp).height(18.dp).clip(RoundedCornerShape(2.dp))
                    .background(accentColor)
            )
            Text(
                title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1A1A2E)
            )
        }
        content()
    }
}

@Composable
private fun FormTextField(
    label: String, value: String, error: String?,
    focusRequester: FocusRequester, nextFocusRequester: FocusRequester?,
    onValueChange: (String) -> Unit, modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.None
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, fontSize = 12.sp, color = Color(0xFF5A5A8A), fontWeight = FontWeight.Medium)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            isError = error != null,
            singleLine = true,
            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                capitalization = capitalization,
                imeAction = if (nextFocusRequester != null) ImeAction.Next else ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onNext = { nextFocusRequester?.requestFocus() }),
            shape = RoundedCornerShape(8.dp),
            colors = fieldColors()
        )
        FieldError(error)
    }
}

@Composable
private fun FormDateField(
    label: String, value: String, error: String?,
    focusRequester: FocusRequester, nextFocusRequester: FocusRequester?,
    onValueChange: (String) -> Unit, modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, fontSize = 12.sp, color = Color(0xFF5A5A8A), fontWeight = FontWeight.Medium)
        OutlinedTextField(
            value = value,
            onValueChange = { raw -> onValueChange(raw.filter { it.isDigit() }.take(8)) },
            visualTransformation = MaskVisualTransformation(MaskVisualTransformation.DATE_MASK),
            isError = error != null, singleLine = true,
            placeholder = { Text("dd/MM/aaaa", color = Color(0xFFAAAAAA), fontSize = 14.sp) },
            leadingIcon = {
                Text(
                    "📅",
                    fontSize = 16.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
            },
            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = if (nextFocusRequester != null) ImeAction.Next else ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onNext = { nextFocusRequester?.requestFocus() }),
            shape = RoundedCornerShape(8.dp), colors = fieldColors()
        )
        FieldError(error)
    }
}

@Composable
private fun FormCurrencyField(
    label: String,
    value: String,
    error: String?,
    focusRequester: FocusRequester,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    suggestedValue: Double? = null,
    onApplySuggestion: () -> Unit = {}
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, fontSize = 12.sp, color = Color(0xFF5A5A8A), fontWeight = FontWeight.Medium)
        OutlinedTextField(
            value = value,
            onValueChange = { raw -> onValueChange(raw.filter { it.isDigit() }) },
            visualTransformation = CurrencyVisualTransformation(),
            isError = error != null, singleLine = true,
            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            shape = RoundedCornerShape(8.dp), colors = fieldColors(textColor = Color(0xFF4A4AE8))
        )

        AnimatedVisibility(
            visible = suggestedValue != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            suggestedValue?.let { avg ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF4A4AE8).copy(alpha = 0.08f))
                        .clickable { onApplySuggestion() }
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "💡",
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Valor médio sugerido: ${avg.formatCurrency()}",
                        fontSize = 12.sp,
                        color = Color(0xFF4A4AE8),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "• Aplicar",
                        fontSize = 12.sp,
                        color = Color(0xFF4A4AE8),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        FieldError(error)
    }
}

@Composable
private fun PaidToggle(isPaid: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Text(
                "Foi Pago?",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1A1A2E)
            )
            Text(
                "Marque se o pagamento já foi recebido",
                fontSize = 12.sp,
                color = Color(0xFF8A8AAD)
            )
        }
        Switch(
            checked = isPaid, onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF4A4AE8),
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFDDDDDD)
            )
        )
    }
}

@Composable
private fun FieldError(error: String?) {
    if (error != null) Text(error, fontSize = 11.sp, color = Color(0xFFD32F2F))
}

@Composable
private fun fieldColors(textColor: Color = Color(0xFF1A1A2E)) = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Color(0xFF4A4AE8),
    unfocusedBorderColor = Color(0xFFE0E0E0),
    errorBorderColor = Color(0xFFD32F2F),
    focusedContainerColor = Color(0xFFF8F8FF),
    unfocusedContainerColor = Color(0xFFFAFAFA),
    focusedTextColor = textColor,
    unfocusedTextColor = textColor
)