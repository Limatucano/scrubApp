package br.com.scrubs.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.scrubs.utils.formatCurrency

val GradientStart = Color(0xFF4A4AE8)
val GradientEnd = Color(0xFF7B5EA7)
private val AmountTextColor = Color.White
private val LabelColor = Color(0xCCFFFFFF)

@Composable
fun AmountMolecule(
    label: String,
    amount: Double,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(GradientStart, GradientEnd)
                )
            )
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Text(
                text = label,
                color = LabelColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = amount.formatCurrency(),
                color = AmountTextColor,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        }

//        Box(
//            contentAlignment = Alignment.Center,
//            modifier = Modifier
//                .align(Alignment.CenterEnd)
//                .size(44.dp)
//                .clip(CircleShape)
//                .background(Color.White.copy(alpha = 0.2f))
//        ) {
//            Icon(
//                imageVector = Icons.Default.AttachMoney,
//                contentDescription = null,
//                tint = Color.White,
//                modifier = Modifier.size(24.dp)
//            )
//        }
    }
}