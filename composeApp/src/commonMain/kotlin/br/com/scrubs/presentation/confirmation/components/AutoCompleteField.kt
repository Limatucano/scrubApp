package br.com.scrubs.presentation.confirmation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Campo de texto com dropdown de sugestões baseado nos valores já salvos no banco.
 * A comparação é feita ignorando acentos e maiúsculas/minúsculas.
 *
 * @param suggestions  Lista completa de valores distintos vindos do banco
 * @param value        Valor atual (dígitos brutos — só texto normal aqui)
 */
@Composable
fun AutoCompleteField(
    label: String,
    value: String,
    suggestions: List<String>,
    error: String?,
    focusRequester: FocusRequester,
    nextFocusRequester: FocusRequester?,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.Words
) {
    var isFocused by remember { mutableStateOf(false) }

    val filtered = remember(value, suggestions) {
        if (value.isBlank()) emptyList()
        else suggestions.filter { it.normalize().contains(value.normalize()) && !it.equals(value, ignoreCase = true) }
    }

    val showDropdown = isFocused && filtered.isNotEmpty()

    Column(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, fontSize = 12.sp, color = Color(0xFF5A5A8A), fontWeight = FontWeight.Medium)
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                isError = error != null,
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .onFocusChanged { isFocused = it.isFocused },
                keyboardOptions = KeyboardOptions(
                    capitalization = capitalization,
                    imeAction = if (nextFocusRequester != null) ImeAction.Next else ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onNext = { nextFocusRequester?.requestFocus() }
                ),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4A4AE8),
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    errorBorderColor = Color(0xFFD32F2F),
                    focusedContainerColor = Color(0xFFF8F8FF),
                    unfocusedContainerColor = Color(0xFFFAFAFA)
                )
            )
            if (error != null) {
                Text(error, fontSize = 11.sp, color = Color(0xFFD32F2F))
            }
        }

        if (showDropdown) {
            Spacer(modifier = Modifier.height(2.dp))
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 180.dp)
                    .shadow(4.dp, RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(filtered) { suggestion ->
                    SuggestionItem(
                        text = suggestion,
                        query = value,
                        onClick = {
                            onValueChange(suggestion)
                            nextFocusRequester?.requestFocus()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SuggestionItem(text: String, query: String, onClick: () -> Unit) {
    val annotated = buildAnnotatedString {
        val normalizedText = text.normalize()
        val normalizedQuery = query.normalize()
        val matchStart = normalizedText.indexOf(normalizedQuery)

        if (matchStart < 0) {
            append(text)
        } else {
            val matchEnd = matchStart + query.length
            append(text.substring(0, matchStart))
            withStyle(SpanStyle(color = Color(0xFF4A4AE8), fontWeight = FontWeight.SemiBold)) {
                append(text.substring(matchStart, matchEnd.coerceAtMost(text.length)))
            }
            if (matchEnd < text.length) append(text.substring(matchEnd))
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(annotated, fontSize = 14.sp, color = Color(0xFF1A1A2E))
    }

    HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 0.5.dp)
}

/**
 * Remove acentos e converte para minúsculas para comparação.
 * Puro Kotlin — sem java.text.Normalizer.
 */
private fun String.normalize(): String = this
    .lowercase()
    .replace(Regex("[àáâãäå]"), "a")
    .replace(Regex("[èéêë]"), "e")
    .replace(Regex("[ìíîï]"), "i")
    .replace(Regex("[òóôõö]"), "o")
    .replace(Regex("[ùúûü]"), "u")
    .replace(Regex("[ç]"), "c")
    .replace(Regex("[ñ]"), "n")