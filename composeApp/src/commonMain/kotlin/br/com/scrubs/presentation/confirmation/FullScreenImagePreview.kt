package br.com.scrubs.presentation.confirmation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.jetbrains.compose.resources.painterResource
import scrubs.composeapp.generated.resources.Res
import scrubs.composeapp.generated.resources.close
import scrubs.composeapp.generated.resources.crop
import scrubs.composeapp.generated.resources.download
import scrubs.composeapp.generated.resources.share_2

@Composable
fun FullScreenImagePreview(
    bitmap: ImageBitmap,
    onDismiss: () -> Unit,
    onShare: () -> Unit,
    onDownload: () -> Unit,
    onCropImage: () -> Unit
) {
    var dragOffsetY by remember { mutableStateOf(0f) }
    val dismissThreshold = 200f

    val backgroundAlpha = (1f - (dragOffsetY / 600f)).coerceIn(0f, 1f)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0D1117).copy(alpha = backgroundAlpha))
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragEnd = {
                            if (dragOffsetY > dismissThreshold) onDismiss()
                            else dragOffsetY = 0f
                        },
                        onDragCancel = { dragOffsetY = 0f },
                        onVerticalDrag = { _, delta ->
                            dragOffsetY = (dragOffsetY + delta).coerceAtLeast(0f)
                        }
                    )
                }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PreviewIconButton(icon = painterResource(Res.drawable.share_2), onClick = onShare)
                    PreviewIconButton(icon = painterResource(Res.drawable.download), onClick = onDownload)
                    PreviewIconButton(icon = painterResource(Res.drawable.crop), onClick = onCropImage)
                }
                PreviewIconButton(icon = painterResource(Res.drawable.close), onClick = onDismiss)
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { translationY = dragOffsetY }
            ) {
                Image(
                    bitmap = bitmap,
                    contentDescription = "Preview da etiqueta",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .align(Alignment.Center)
                    .graphicsLayer { translationY = dragOffsetY }
                    .padding(bottom = 160.dp)
            ) {}

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 24.dp)
                    .graphicsLayer { translationY = dragOffsetY }
            ) {
                Text(
                    text = "Toque em fechar ou arraste para baixo para voltar",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.White.copy(alpha = 0.3f))
                )
            }
        }
    }
}

@Composable
private fun PreviewIconButton(icon: Painter, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            painter = icon,
            contentDescription = null,
            tint = Color(0xFFFFFFFF)
        )
    }
}