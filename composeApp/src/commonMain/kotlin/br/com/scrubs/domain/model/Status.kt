package br.com.scrubs.domain.model

import androidx.compose.ui.graphics.Color

enum class Status(
    val backgroundColor: Color,
    val textColor: Color,
    val borderColor: Color,
    val text: String,
    val iconColor: Color
) {
    PAID(
        backgroundColor = Color(0xFFA5D6A7),
        textColor = Color(0xFF2E7D32),
        borderColor = Color(0xFF6FD273),
        text = "✓ Recebido",
        iconColor = Color(0xFF2E7D32)
    ),
    PENDING(
        backgroundColor = Color(0xFFFFCDD2),
        textColor = Color(0xFFD32F2F),
        borderColor = Color(0xFFFC99A3),
        text = "Pendente",
        iconColor = Color(0xFFD32F2F)
    );

    companion object {
        fun Status.isPaid() = PAID == this
    }
}