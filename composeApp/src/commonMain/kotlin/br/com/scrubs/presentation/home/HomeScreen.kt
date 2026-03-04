package br.com.scrubs.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.scrubs.domain.model.DateFilter
import br.com.scrubs.presentation.home.components.AmountMolecule
import br.com.scrubs.presentation.home.components.HeaderOrganism
import br.com.scrubs.presentation.home.components.ReceiptItemMolecule
import br.com.scrubs.utils.parseToDayMonth
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator

class HomeScreen : Screen {
    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<HomeScreenModel>()
        val state by screenModel.state.collectAsState()
        val navigator = LocalNavigator.current

        HomeContent(
            state = state,
            onEvent = screenModel::onEvent,
            onAddClick = {}
        )
    }
}

@Composable
private fun HomeContent(
    state: HomeState,
    onAddClick: () -> Unit,
    onEvent: (HomeEvent) -> Unit
) {
    val backgroundColor = Color(0xFFF4F4FB)

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* navegar para tela de adicionar */ },
                containerColor = Color(0xFF4A4AE8),
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Text(
                    text = "Adicionar"
                )
//                Icon(
//                    imageVector = Icons.Default.CameraAlt,
//                    contentDescription = "Adicionar recibo"
//                )
            }
        },
        containerColor = Color(0xFFF4F4FB)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                HeaderOrganism(title = "Olá, Daniella")
            }

            item {
                AmountMolecule(
                    label = "Total a Receber",
                    amount = state.totalPending
                )
            }

            item {
                DateFilterRow(
                    selectedFilter = state.selectedFilter,
                    onFilterSelected = { onEvent(HomeEvent.FilterChanged(it)) }
                )
            }

            if (state.isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF4A4AE8))
                    }
                }
            } else {
                items(state.receipts, key = { it.id }) { receipt ->
                    val (day, month) = receipt.surgicalDate.parseToDayMonth()

                    ReceiptItemMolecule(
                        day = day,
                        month = month,
                        patientName = receipt.patientName,
                        healthPlan = receipt.healthPlan,
                        surgicalProcedure = receipt.surgicalProcedure,
                        value = receipt.value,
                        status = receipt.status,
                        onEditClick = { onEvent(HomeEvent.EditReceipt(receipt)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DateFilterRow(
    selectedFilter: DateFilter,
    onFilterSelected: (DateFilter) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(DateFilter.entries) { filter ->
            val isSelected = filter == selectedFilter
            val containerColor = if (isSelected) Color(0xFF4A4AE8) else Color.White
            val textColor = if (isSelected) Color.White else Color(0xFF5A5A8A)

            Box(
                contentAlignment = androidx.compose.ui.Alignment.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(containerColor)
                    .let {
                        if (!isSelected) it
                            .then(
                                Modifier.background(Color.White, RoundedCornerShape(20.dp))
                            )
                        else it
                    }
            ) {
                TextButton(
                    onClick = { onFilterSelected(filter) },
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = containerColor,
                        contentColor = textColor
                    ),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = filter.label,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
    }
}
