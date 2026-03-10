package br.com.scrubs.presentation.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.scrubs.utils.formatCurrency

@Composable
fun AmountSummaryRow(
    totalPaid: Double,
    countPaid: Int,
    totalPending: Double,
    countPending: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AmountCard(
            modifier = Modifier.weight(1f),
            backgroundColor = Color(0xFF2ECC71),
            icon = "✓",
            iconBackground = Color(0xFF27AE60),
            label = "Recebido",
            count = countPaid,
            amount = totalPaid
        )
        AmountCard(
            modifier = Modifier.weight(1f),
            backgroundColor = Color(0xFFFF8C00),
            icon = "↗",
            iconBackground = Color(0xFFE07B00),
            label = "A Receber",
            count = countPending,
            amount = totalPending
        )
    }
}

@Composable
private fun AmountCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    icon: String,
    iconBackground: Color,
    label: String,
    count: Int,
    amount: Double
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Ícone com fundo levemente mais escuro
                Card(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape),
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(containerColor = iconBackground)
                ) {
                    Row(
                        modifier = Modifier
                            .size(32.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = icon,
                            fontSize = 16.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Column {
                    Text(
                        text = label,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Text(
                        text = "$count cirurgi${if (count == 1) "a" else "as"}",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = amount.formatCurrency(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}