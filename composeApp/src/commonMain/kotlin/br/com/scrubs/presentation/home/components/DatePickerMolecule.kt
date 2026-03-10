package br.com.scrubs.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScrubsDateRangePicker(
    state: DateRangePickerState,
    onDismiss: () -> Unit,
    onConfirm: (Pair<Long, Long>) -> Unit
) {
    val canConfirm = state.selectedStartDateMillis != null && state.selectedEndDateMillis != null

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
        ) {
            DateRangePicker(
                state = state,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .heightIn(max = 520.dp),
                title = {
                    Text(
                        text = "Selecionar Período",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1A1A2E),
                        modifier = Modifier.padding(start = 24.dp, top = 20.dp, bottom = 4.dp)
                    )
                },
                headline = {
                    // Range selecionado exibido abaixo do título
                    val start = state.selectedStartDateMillis?.toFormattedDate() ?: "Data inicial"
                    val end   = state.selectedEndDateMillis?.toFormattedDate()   ?: "Data final"
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 12.dp)
                    ) {
                        DateChip(label = start, selected = state.selectedStartDateMillis != null)
                        Text("→", color = Color(0xFF8A8AAD), fontSize = 14.sp)
                        DateChip(label = end, selected = state.selectedEndDateMillis != null)
                    }
                },
                showModeToggle = false,
                colors = dateRangePickerColors()
            )

            HorizontalDivider(color = Color(0xFFEEEEEE))

            // ── Botões ──
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF5A5A8A))
                ) {
                    Text("Cancelar", fontWeight = FontWeight.Medium)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (canConfirm) {
                            onConfirm(
                                Pair(
                                    state.selectedStartDateMillis!!,
                                    state.selectedEndDateMillis!!
                                )
                            )
                        }
                    },
                    enabled = canConfirm,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4A4AE8),
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFFDDDDEE),
                        disabledContentColor = Color(0xFF9999AA)
                    )
                ) {
                    Text("Selecionar", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun DateChip(label: String, selected: Boolean) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) Color(0xFF4A4AE8).copy(alpha = 0.1f) else Color(0xFFF4F4FB))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) Color(0xFF4A4AE8) else Color(0xFF8A8AAD)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun dateRangePickerColors() = DatePickerDefaults.colors(
    containerColor                      = Color.White,
    titleContentColor                   = Color(0xFF1A1A2E),
    headlineContentColor                = Color(0xFF1A1A2E),
    weekdayContentColor                 = Color(0xFF8A8AAD),
    subheadContentColor                 = Color(0xFF5A5A8A),
    navigationContentColor              = Color(0xFF1A1A2E),
    // Dia selecionado (início e fim do range)
    selectedDayContainerColor           = Color(0xFF4A4AE8),
    selectedDayContentColor             = Color.White,
    // Dia de hoje
    todayContentColor                   = Color(0xFF4A4AE8),
    todayDateBorderColor                = Color(0xFF4A4AE8),
    // Dias dentro do range selecionado
    dayInSelectionRangeContainerColor   = Color(0xFF4A4AE8).copy(alpha = 0.12f),
    dayInSelectionRangeContentColor     = Color(0xFF4A4AE8),
    // Dias normais
    dayContentColor                     = Color(0xFF1A1A2E),
    disabledDayContentColor             = Color(0xFFCCCCCC),
    // Ano/mês no header
    selectedYearContainerColor          = Color(0xFF4A4AE8),
    selectedYearContentColor            = Color.White,
    currentYearContentColor             = Color(0xFF4A4AE8),
    yearContentColor                    = Color(0xFF1A1A2E),
    disabledYearContentColor            = Color(0xFFCCCCCC),
    // Divider do header
    dividerColor                        = Color(0xFFEEEEEE)
)

private fun Long.toFormattedDate(): String {
    val instant = Instant.fromEpochMilliseconds(this)
    val local = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val day   = local.date.day.toString().padStart(2, '0')
    val month = local.date.month.number.toString().padStart(2, '0')
    val year  = local.year
    return "$day/$month/$year"
}