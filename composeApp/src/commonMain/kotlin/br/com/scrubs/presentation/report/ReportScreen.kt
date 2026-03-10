package br.com.scrubs.presentation.report

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.scrubs.presentation.confirmation.components.normalize
import br.com.scrubs.utils.formatCurrency
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.jetbrains.compose.resources.painterResource
import scrubs.composeapp.generated.resources.Res
import scrubs.composeapp.generated.resources.arrow_back
import scrubs.composeapp.generated.resources.trending_up

class ReportScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = koinScreenModel<ReportScreenModel>()
        val state by screenModel.state.collectAsState()

        ReportContent(
            state = state,
            onQueryChanged = screenModel::onQueryChanged,
            onBack = { navigator.pop() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReportContent(
    state: ReportState,
    onQueryChanged: (String) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Relatórios",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color(0xFF1A1A2E)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(Res.drawable.arrow_back),
                            contentDescription = null,
                            tint = Color(0xFF4A4AE8)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF4F4FB)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ProcedureSearchField(
                query = state.query,
                suggestions = state.procedureSuggestions,
                onQueryChange = onQueryChanged,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            LazyColumn(
                contentPadding = PaddingValues(
                    start = 16.dp, end = 16.dp, bottom = 24.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when {
                    !state.hasSearched -> item { SearchPromptState() }

                    state.isLoading -> item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Buscando...", color = Color(0xFF8A8AAD))
                        }
                    }

                    state.plans.isEmpty() -> item { EmptyResultState(query = state.query) }

                    else -> {
                        item {
                            ResultsHeader(totalSurgeries = state.totalSurgeries)
                        }

                        items(state.plans) { plan ->
                            HealthPlanCard(plan = plan)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProcedureSearchField(
    query: String,
    suggestions: List<String>,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }

    val filtered = remember(query, suggestions) {
        if (query.isBlank()) emptyList()
        else suggestions.filter {
            it.normalize().contains(query.normalize()) && !it.equals(query, ignoreCase = true)
        }
    }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = {
                Text(
                    text = "Digite o procedimento (ex: uretero)",
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
private fun ResultsHeader(totalSurgeries: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEEEEFF))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Resultados encontrados",
                    fontSize = 12.sp,
                    color = Color(0xFF4A4AE8),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "$totalSurgeries cirurgi${if (totalSurgeries == 1) "a" else "as"}",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A2E)
                )
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF4A4AE8).copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(Res.drawable.trending_up),
                    contentDescription = null,
                    tint = Color(0xFF4A4AE8)
                )
            }
        }
    }
}

@Composable
private fun HealthPlanCard(plan: HealthPlanReport) {
    val maxMonthTotal = plan.months.maxOfOrNull { it.total } ?: 1.0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = plan.healthPlan,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A2E)
                    )
                    Text(
                        text = "Média Geral: ${plan.generalAverage.formatCurrency()}",
                        fontSize = 13.sp,
                        color = Color(0xFF4A4AE8),
                        fontWeight = FontWeight.Medium
                    )
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4A4AE8).copy(alpha = 0.10f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.trending_up),
                        contentDescription = null,
                        tint = Color(0xFF4A4AE8)
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFF0F0F0))

            // Meses
            plan.months.forEach { month ->
                MonthRow(entry = month, maxTotal = maxMonthTotal)
            }
        }
    }
}

@Composable
private fun MonthRow(entry: MonthEntry, maxTotal: Double) {
    val progress = (entry.total / maxTotal).toFloat().coerceIn(0f, 1f)

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = entry.yearMonth,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF5A5A8A)
            )
            Text(
                text = "${entry.count} cirurgi${if (entry.count == 1) "a" else "as"}",
                fontSize = 12.sp,
                color = Color(0xFF8A8AAD)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF0F0FA)),
            contentAlignment = Alignment.CenterStart
        ) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .clip(RoundedCornerShape(8.dp)),
                color = Color(0xFF4A4AE8),
                trackColor = Color(0xFFF0F0FA),
                strokeCap = StrokeCap.Round
            )
            Text(
                text = entry.total.formatCurrency(),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
private fun SearchPromptState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 80.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Text(text = "🔍", fontSize = 56.sp)
            Text(
                text = "Pesquise um procedimento",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1A1A2E),
                textAlign = TextAlign.Center
            )
            Text(
                text = "Digite o nome do procedimento para ver as médias por plano e período",
                fontSize = 13.sp,
                color = Color(0xFF8A8AAD),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun EmptyResultState(query: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 80.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Text(text = "📋", fontSize = 56.sp)
            Text(
                text = "Nenhum resultado para \"$query\"",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1A1A2E),
                textAlign = TextAlign.Center
            )
            Text(
                text = "Tente buscar por outro procedimento",
                fontSize = 13.sp,
                color = Color(0xFF8A8AAD),
                textAlign = TextAlign.Center
            )
        }
    }
}