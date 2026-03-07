package br.com.scrubs.presentation.home.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun TitleAtom(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF1A1A2E)
) {
    Text(
        text = text,
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        color = color,
        modifier = modifier
    )
}