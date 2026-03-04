package br.com.scrubs.presentation.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.scrubs.presentation.permission.PermissionDeniedDialog
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import dev.icerock.moko.permissions.compose.BindEffect
import kotlinx.coroutines.launch

class CameraScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val screenModel = koinScreenModel<CameraScreenModule>()
        val state by screenModel.state.collectAsState()
        BindEffect(screenModel.permissionManager.permissionsController)

        LaunchedEffect(Unit) {
            screenModel.requestPermission()
        }

        LaunchedEffect(Unit) {
            screenModel.actionTakeImage.collect { actionTakeImage ->
                when (actionTakeImage) {
                    ActionTakeImage.OpenCamera -> {}
                    ActionTakeImage.OpenGallery -> {}
                }
            }
        }

        if (state.showPermissionDialog) {
            PermissionDeniedDialog(
                onDismiss = screenModel::dismissPermissionDialog,
                onSettingsClick = screenModel::goToSetting
            )
        }

        CameraContent(
            state = state,
            onEvent = screenModel::onEvent,
            onClose = { navigator?.pop() }
        )
    }
}

@Composable
private fun CameraContent(
    state: CameraState,
    onEvent: (CameraEvent) -> Unit,
    onClose: () -> Unit
) {
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0E1A))
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {


        ScanOverlay()

        CameraTopBar(
            isFlashOn = state.isFlashOn,
            onClose = onClose,
            onToggleFlash = { onEvent(CameraEvent.ToggleFlash) },
            onFlipCamera = {  },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-20).dp)
        ) {
            ScanFrame(size = 260.dp)
            Spacer(modifier = Modifier.height(20.dp))
            ScanInstruction()
        }

        CameraBottomBar(
            onCapture = {  },
            onGallery = {  },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 40.dp)
        )
    }
}

@Composable
private fun CameraIconButton(
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.15f))
            .clickable { onClick() }
    ) {
        content()
    }
}

@Composable
private fun CameraTopBar(
    isFlashOn: Boolean,
    onClose: () -> Unit,
    onToggleFlash: () -> Unit,
    onFlipCamera: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
    ) {
        CameraIconButton(onClick = onClose) {
            Text(text = "X", color = Color.White)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            CameraIconButton(onClick = onToggleFlash) {
                Text(
                    text = if (isFlashOn) "Desligar" else "Ligar",
                    color = if (isFlashOn) Color(0xFFFFD600) else Color.White
                )
            }
            CameraIconButton(onClick = onFlipCamera) {
                Text(text = "Virar", color = Color.White)
            }
        }
    }
}

@Composable
private fun ScanFrame(size: Dp) {
    val cornerColor = Color(0xFF4A6CF7)
    val cornerSize = 28.dp
    val strokeWidth = 3.dp

    Box(
        modifier = Modifier
            .size(size)
    ) {
        CornerBracket(
            modifier = Modifier.align(Alignment.TopStart),
            color = cornerColor,
            size = cornerSize,
            strokeWidth = strokeWidth,
            flipH = false,
            flipV = false
        )
        CornerBracket(
            modifier = Modifier.align(Alignment.TopEnd),
            color = cornerColor,
            size = cornerSize,
            strokeWidth = strokeWidth,
            flipH = true,
            flipV = false
        )
        CornerBracket(
            modifier = Modifier.align(Alignment.BottomStart),
            color = cornerColor,
            size = cornerSize,
            strokeWidth = strokeWidth,
            flipH = false,
            flipV = true
        )
        CornerBracket(
            modifier = Modifier.align(Alignment.BottomEnd),
            color = cornerColor,
            size = cornerSize,
            strokeWidth = strokeWidth,
            flipH = true,
            flipV = true
        )
    }
}

@Composable
private fun CornerBracket(
    modifier: Modifier = Modifier,
    color: Color,
    size: Dp,
    strokeWidth: Dp,
    flipH: Boolean,
    flipV: Boolean
) {
    androidx.compose.foundation.Canvas(
        modifier = modifier.size(size)
    ) {
        val sw = strokeWidth.toPx()
        val s = size.toPx()
        val half = s / 2

        val startX = if (flipH) s else 0f
        val startY = if (flipV) s else 0f
        val endXH = if (flipH) s - half else half
        val endYV = if (flipV) s - half else half

        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(startX, startY),
            end = androidx.compose.ui.geometry.Offset(endXH, startY),
            strokeWidth = sw,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(startX, startY),
            end = androidx.compose.ui.geometry.Offset(startX, endYV),
            strokeWidth = sw,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    }
}

@Composable
private fun ScanInstruction() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0D1117).copy(alpha = 0.85f))
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        ScanIcon(color = Color(0xFF4A6CF7))

        Column {
            Text(
                text = "Posicione a etiqueta cirúrgica",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "dentro da área de escaneamento",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun ScanIcon(color: Color) {
    androidx.compose.foundation.Canvas(modifier = Modifier.size(28.dp)) {
        val s = size.minDimension
        val sw = 2.dp.toPx()
        val corner = s * 0.28f

        listOf(
            Triple(0f, 0f, false to false),
            Triple(s, 0f, true to false),
            Triple(0f, s, false to true),
            Triple(s, s, true to true)
        ).forEach { (cx, cy, flip) ->
            val (fH, fV) = flip
            val dx = if (fH) -corner else corner
            val dy = if (fV) -corner else corner

            drawLine(color, androidx.compose.ui.geometry.Offset(cx, cy), androidx.compose.ui.geometry.Offset(cx + dx, cy), sw, androidx.compose.ui.graphics.StrokeCap.Round)
            drawLine(color, androidx.compose.ui.geometry.Offset(cx, cy), androidx.compose.ui.geometry.Offset(cx, cy + dy), sw, androidx.compose.ui.graphics.StrokeCap.Round)
        }
    }
}

@Composable
private fun CameraBottomBar(
    onCapture: () -> Unit,
    onGallery: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White.copy(alpha = 0.15f))
                    .clickable { onGallery() }
            ) {
                Text(
                    text = "Galeria",
                    color = Color.White.copy(alpha = 0.7f),
                )
            }

            Spacer(modifier = Modifier.width(48.dp))

            // Botão de captura central
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
                    .clickable { onCapture() }
            ) {
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }

            Spacer(modifier = Modifier.width(48.dp + 48.dp)) // simetria
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Toque no botão para capturar",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ScanOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f))
    )
}