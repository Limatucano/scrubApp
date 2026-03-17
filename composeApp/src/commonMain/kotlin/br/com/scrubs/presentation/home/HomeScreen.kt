package br.com.scrubs.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.scrubs.domain.model.DateFilter
import br.com.scrubs.domain.model.Receipt
import br.com.scrubs.presentation.camera.CameraScreen
import br.com.scrubs.presentation.confirmation.ConfirmationScreen
import br.com.scrubs.presentation.confirmation.components.normalize
import br.com.scrubs.presentation.home.components.AmountSummaryRow
import br.com.scrubs.presentation.home.components.ReceiptItemMolecule
import br.com.scrubs.presentation.home.components.ScrubsDateRangePicker
import br.com.scrubs.presentation.home.components.TitleAtom
import br.com.scrubs.presentation.report.ReportScreen
import br.com.scrubs.utils.parseToDayMonth
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import org.jetbrains.compose.resources.painterResource
import scrubs.composeapp.generated.resources.Res
import scrubs.composeapp.generated.resources.chart_column

class HomeScreen : Screen {
    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<HomeScreenModel>()
        val state by screenModel.state.collectAsState()
        val navigator = LocalNavigator.current
        val datePickerState = rememberDateRangePickerState(
            initialDisplayMode = DisplayMode.Picker
        )

        if (state.showDatePicker) {
            ScrubsDateRangePicker(
                state = datePickerState,
                onDismiss = screenModel::dismissDatePicker,
                onConfirm = { (startMillis, endMillis) ->
                    screenModel.onEvent(HomeEvent.CustomDateSelected(startMillis, endMillis))
                }
            )
        }

        HomeContent(
            state = state,
            onEvent = screenModel::onEvent,
            onAddClick = { navigator?.push(CameraScreen()) },
            onEditClick = { navigator?.push(ConfirmationScreen(it)) },
            onReportClick = { navigator?.push(ReportScreen()) }
        )
    }
}

@Composable
private fun HomeContent(
    state: HomeState,
    onAddClick: () -> Unit,
    onEvent: (HomeEvent) -> Unit,
    onEditClick: (Receipt) -> Unit,
    onReportClick: () -> Unit
) {
    val backgroundColor = Color(0xFFF4F4FB)

    Scaffold(
        topBar = {
            HomeTopBar(
                onReportClick = onReportClick
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAddClick() },
                containerColor = Color(0xFF4A4AE8),
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Text(text = "+")
            }
        },
        containerColor = Color(0xFFF4F4FB)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(innerPadding),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 24.dp,
                bottom = 80.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            item {
                AmountSummaryRow(
                    totalPaid = state.totalPaid.total,
                    countPaid = state.totalPaid.count,
                    totalPending = state.totalPending.total,
                    countPending = state.totalPending.count
                )
            }

            item {
                DateFilterRow(
                    selectedFilter = state.selectedFilter,
                    customFilterLabel = state.customFilterLabel,
                    onFilterSelected = { onEvent(HomeEvent.FilterChanged(it)) }
                )
            }

            item {
                SearchField(
                    query = state.query,
                    suggestions = state.companySuggestions,
                    onQueryChange = { onEvent(HomeEvent.CompanyQueryChanged(it)) }
                )
            }

            if (state.isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF4A4AE8))
                    }
                }
            } else if (state.filteredReceipts.isEmpty()) {
                item { EmptyState() }
            } else {
                items(state.filteredReceipts, key = { it.id }) { receipt ->
                    val (day, month, year) = receipt.surgicalDate.parseToDayMonth()

                    ReceiptItemMolecule(
                        day = day,
                        month = month,
                        year = year,
                        patientName = receipt.patientName,
                        healthPlan = receipt.healthPlan,
                        surgicalProcedure = receipt.surgicalProcedure,
                        company = receipt.company,
                        value = receipt.value,
                        status = receipt.status,
                        onEditClick = { onEditClick(receipt) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopBar(onReportClick: () -> Unit = {}) {
    TopAppBar(
        title = {
            TitleAtom(text = "Olá, Daniella Corrêa")
        },
        actions = {
            IconButton(onClick = onReportClick) {
                Icon(
                    painter = painterResource(Res.drawable.chart_column),
                    contentDescription = null,
                    tint = Color(0xFF4A4AE8)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White
        )
    )
}

@Composable
private fun SearchField(
    query: String,
    suggestions: List<String>,
    onQueryChange: (String) -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    val filtered = remember(query, suggestions) {
        if (query.isBlank()) emptyList()
        else suggestions.filter {
            it.normalize().contains(query.normalize()) && !it.equals(query, ignoreCase = true)
        }
    }

    Column {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = {
                Text(
                    text = "Pesquise por empresas associadas ou nome do paciente",
                    color = Color(0xFFAAAAAA),
                    fontSize = 14.sp
                )
            },
            leadingIcon = {
                Text("🔍", fontSize = 16.sp, modifier = Modifier.padding(start = 4.dp))
            },
            trailingIcon = {
                if (query.isNotBlank()) {
                    Text(
                        text = "✕",
                        fontSize = 14.sp,
                        color = Color(0xFF8A8AAD),
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .clickable { onQueryChange("") }
                    )
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { isFocused = it.isFocused },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF4A4AE8),
                unfocusedBorderColor = Color(0xFFE0E0E0),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )

        AnimatedVisibility(
            visible = isFocused && filtered.isNotEmpty(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Spacer(modifier = Modifier.height(2.dp))
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 160.dp)
                    .shadow(4.dp, RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(filtered) { suggestion ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onQueryChange(suggestion) }
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text(suggestion, fontSize = 14.sp, color = Color(0xFF1A1A2E))
                    }
                    HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 0.5.dp)
                }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp, bottom = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Text(text = "🗂️", fontSize = 48.sp)
            Text(
                text = "Não possui nenhuma cirurgia salva",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1A1A2E),
                textAlign = TextAlign.Center
            )
            Text(
                text = "Para adicionar uma nova clique no botão '+'",
                fontSize = 13.sp,
                color = Color(0xFF8A8AAD),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun DateFilterRow(
    selectedFilter: DateFilter,
    customFilterLabel: String,
    onFilterSelected: (DateFilter) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(DateFilter.entries) { filter ->
            val isSelected = filter == selectedFilter
            val containerColor = if (isSelected) Color(0xFF4A4AE8) else Color.White
            val textColor = if (isSelected) Color.White else Color(0xFF5A5A8A)

            val label = if (filter == DateFilter.CUSTOM) customFilterLabel else filter.label

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(containerColor)
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
                        text = label,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
    }
}