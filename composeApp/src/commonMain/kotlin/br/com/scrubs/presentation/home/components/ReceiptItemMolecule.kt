package br.com.scrubs.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.scrubs.domain.model.Status
import br.com.scrubs.utils.formatCurrency

private val CardBackground = Color.White
private val DateBoxBackground = Color(0xFFF0F0FA)
private val DateDayColor = Color(0xFF2D2D6B)
private val DateMonthColor = Color(0xFF8A8AAD)
private val PatientNameColor = Color(0xFF1A1A2E)
private val ProcedureColor = Color(0xFF8A8AAD)
private val AmountColor = Color(0xFF4A4AE8)
private val EditColor = Color(0xFF4A4AE8)

@Composable
fun ReceiptItemMolecule(
    day: String,
    month: String,
    patientName: String,
    healthPlan: String,
    surgicalProcedure: String,
    value: Double,
    status: Status,
    onEditClick: (() -> Unit) = {},
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardBackground)
            .padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .size(width = 52.dp, height = 60.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(DateBoxBackground)
                .padding(6.dp)
        ) {
            Text(
                text = day,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = DateDayColor,
                lineHeight = 22.sp
            )
            Text(
                text = month.uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = DateMonthColor
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = patientName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PatientNameColor,
                    modifier = Modifier.weight(1f)
                )
                StatusAtom(status = status)
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = surgicalProcedure,
                fontSize = 13.sp,
                color = ProcedureColor,
                fontWeight = FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = healthPlan,
                fontSize = 13.sp,
                color = ProcedureColor,
                fontWeight = FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = value.formatCurrency(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AmountColor
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.clickable { onEditClick() }
                ) {
//                        Icon(
//                            imageVector = Icon,
//                            contentDescription = "Editar",
//                            tint = EditColor,
//                            modifier = Modifier.size(14.dp)
//                        )
                    Text(
                        text = "Editar",
                        fontSize = 13.sp,
                        color = EditColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
