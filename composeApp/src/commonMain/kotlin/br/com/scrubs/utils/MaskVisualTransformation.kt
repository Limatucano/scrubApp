package br.com.scrubs.utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class MaskVisualTransformation(private val mask: String) : VisualTransformation {
    companion object {
        const val DATE_MASK = "##/##/####"
    }
    private val digitPositions: List<Int> = mask.indices.filter { mask[it] == '#' }

    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text
        val masked = buildString {
            var digitIndex = 0
            for (maskChar in mask) {
                if (digitIndex >= digits.length) break
                if (maskChar == '#') {
                    append(digits[digitIndex++])
                } else {
                    append(maskChar)
                }
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset == 0) return 0
                val pos = digitPositions.getOrNull(offset - 1) ?: return masked.length
                return pos + 1
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset == 0) return 0
                return mask.take(offset).count { it == '#' }
            }
        }

        return TransformedText(AnnotatedString(masked), offsetMapping)
    }
}