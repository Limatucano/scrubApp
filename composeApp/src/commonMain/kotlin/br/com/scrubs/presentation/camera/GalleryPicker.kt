package br.com.scrubs.presentation.camera

import androidx.compose.runtime.Composable

/**
 * Abre o seletor nativo de imagens da galeria.
 * Renderiza um composable invisível que gerencia o launcher nativo.
 *
 * @param open            true para abrir o picker
 * @param onImageSelected Retorna ByteArray da imagem selecionada
 * @param onDismiss       Chamado ao fechar sem selecionar
 */
@Composable
expect fun GalleryPicker(
    open: Boolean,
    onImageSelected: (ByteArray) -> Unit,
    onDismiss: () -> Unit
)