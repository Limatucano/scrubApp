package br.com.scrubs.utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class CurrencyVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text.filter { it.isDigit() }
        val value = digits.toLongOrNull() ?: 0L
        val intPart = value / 100
        val decPart = value % 100

        val intFormatted = if (intPart == 0L) "0"
        else intPart.toString().reversed().chunked(3).joinToString(".").reversed()

        val formatted = "R$ $intFormatted,${decPart.toString().padStart(2, '0')}"

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int = formatted.length
            override fun transformedToOriginal(offset: Int): Int = digits.length
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}