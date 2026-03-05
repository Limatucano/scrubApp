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
import br.com.scrubs.presentation.confirmation.ConfirmationScreen
import br.com.scrubs.presentation.permission.PermissionDeniedDialog
import br.com.scrubs.utils.cropAndRotateImage
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import dev.icerock.moko.permissions.compose.BindEffect

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

        if (state.showPermissionDialog) {
            PermissionDeniedDialog(
                onDismiss = screenModel::dismissPermissionDialog,
                onSettingsClick = screenModel::goToSetting
            )
        }

        GalleryPicker(
            open = state.galleryOpen,
            onImageSelected = {
                navigator?.push(ConfirmationScreen(it))
            },
            onDismiss = { screenModel.onEvent(CameraEvent.DismissGallery) }
        )

        CameraContent(
            state = state,
            onEvent = screenModel::onEvent,
            onClose = { navigator?.pop() },
            onGoToConfirmation = {
                navigator?.push(ConfirmationScreen(it))
            }
        )
    }
}

@Composable
private fun CameraContent(
    state: CameraState,
    onEvent: (CameraEvent) -> Unit,
    onClose: () -> Unit,
    onGoToConfirmation: (image: ByteArray) -> Unit = {}
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0E1A))
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {

        val frameSizeDp = 260.dp
        val frameOffsetYDp = (-20).dp
        val screenW = maxWidth
        val screenH = maxHeight

        val frameWidthRatio  = (frameSizeDp / screenW).coerceIn(0f, 1f)
        val frameHeightRatio = (frameSizeDp / screenH).coerceIn(0f, 1f)
        val frameCenterX = 0.5f
        val frameCenterY = 0.5f + (frameOffsetYDp / screenH).toFloat()

        CameraPreview(
            modifier = Modifier.fillMaxSize(),
            isFlashOn = state.isFlashOn,
            isFrontCamera = state.isFrontCamera,
            captureRequestId = state.captureTrigger,
            onImageCaptured = {
                val image = cropAndRotateImage(
                    bytes = it,
                    frameCenterX = frameCenterX,
                    frameCenterY = frameCenterY,
                    frameWidthRatio = frameWidthRatio,
                    frameHeightRatio = frameHeightRatio
                )
                onGoToConfirmation(image)
            }
        )

        ScanOverlay()

        CameraTopBar(
            isFlashOn = state.isFlashOn,
            onClose = onClose,
            onToggleFlash = { onEvent(CameraEvent.ToggleFlash) },
            onFlipCamera = { onEvent(CameraEvent.FlipCamera) },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.align(Alignment.Center).offset(y = (-20).dp)
        ) {
            ScanFrame(size = 260.dp)
            Spacer(modifier = Modifier.height(20.dp))
            ScanInstruction()
        }

        CameraBottomBar(
            onCapture = { onEvent(CameraEvent.Capture) },
            onGallery = { onEvent(CameraEvent.OpenGallery) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 40.dp)
        )
    }
}

@Composable
private fun CameraTopBar(isFlashOn: Boolean, onClose: () -> Unit, onToggleFlash: () -> Unit, onFlipCamera: () -> Unit, modifier: Modifier = Modifier) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = modifier) {
        CameraIconButton(onClick = onClose) { Text(text = "✕", color = Color.White, fontSize = 16.sp) }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CameraIconButton(onClick = onToggleFlash) {
                Text(text = "⚡", color = if (isFlashOn) Color(0xFFFFD600) else Color.White.copy(alpha = 0.5f), fontSize = 16.sp)
            }
            CameraIconButton(onClick = onFlipCamera) { Text(text = "↺", color = Color.White, fontSize = 18.sp) }
        }
    }
}

@Composable
private fun CameraIconButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.15f)).clickable { onClick() }) { content() }
}

@Composable
private fun ScanFrame(size: Dp) {
    Box(modifier = Modifier.size(size)) {
        val c = Color(0xFF4A6CF7); val cs = 28.dp; val sw = 3.dp
        CornerBracket(Modifier.align(Alignment.TopStart),    c, cs, sw, false, false)
        CornerBracket(Modifier.align(Alignment.TopEnd),      c, cs, sw, true,  false)
        CornerBracket(Modifier.align(Alignment.BottomStart), c, cs, sw, false, true)
        CornerBracket(Modifier.align(Alignment.BottomEnd),   c, cs, sw, true,  true)
    }
}

@Composable
private fun CornerBracket(modifier: Modifier, color: Color, size: Dp, strokeWidth: Dp, flipH: Boolean, flipV: Boolean) {
    androidx.compose.foundation.Canvas(modifier = modifier.size(size)) {
        val sw = strokeWidth.toPx(); val s = size.toPx(); val half = s / 2
        val sx = if (flipH) s else 0f; val sy = if (flipV) s else 0f
        drawLine(color, androidx.compose.ui.geometry.Offset(sx, sy), androidx.compose.ui.geometry.Offset(if (flipH) s - half else half, sy), sw, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        drawLine(color, androidx.compose.ui.geometry.Offset(sx, sy), androidx.compose.ui.geometry.Offset(sx, if (flipV) s - half else half), sw, cap = androidx.compose.ui.graphics.StrokeCap.Round)
    }
}

@Composable
private fun ScanInstruction() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(Color(0xFF0D1117).copy(alpha = 0.85f)).padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        ScanIcon(color = Color(0xFF4A6CF7))
        Column {
            Text(text = "Posicione a etiqueta cirúrgica", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(text = "dentro da área de escaneamento", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
        }
    }
}

@Composable
private fun ScanIcon(color: Color) {
    androidx.compose.foundation.Canvas(modifier = Modifier.size(28.dp)) {
        val s = size.minDimension; val sw = 2.dp.toPx(); val c = s * 0.28f
        listOf(0f to 0f, s to 0f, 0f to s, s to s).forEachIndexed { i, (cx, cy) ->
            val fH = i == 1 || i == 3; val fV = i == 2 || i == 3
            drawLine(color, androidx.compose.ui.geometry.Offset(cx, cy), androidx.compose.ui.geometry.Offset(cx + if (fH) -c else c, cy), sw, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            drawLine(color, androidx.compose.ui.geometry.Offset(cx, cy), androidx.compose.ui.geometry.Offset(cx, cy + if (fV) -c else c), sw, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        }
    }
}

@Composable
private fun CameraBottomBar(onCapture: () -> Unit, onGallery: () -> Unit, modifier: Modifier = Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(48.dp).clip(RoundedCornerShape(10.dp)).background(Color.White.copy(alpha = 0.15f)).clickable { onGallery() }) {
                Text(text = "🖼", fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.width(48.dp))
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(72.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)).clickable { onCapture() }) {
                Box(modifier = Modifier.size(58.dp).clip(CircleShape).background(Color.White))
            }
            Spacer(modifier = Modifier.width(96.dp))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "Toque no botão para capturar", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun ScanOverlay() {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)))
}