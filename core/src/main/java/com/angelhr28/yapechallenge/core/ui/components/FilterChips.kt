package com.angelhr28.yapechallenge.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Modelo de datos que representa una opcion de filtro.
 *
 * @property label texto visible del filtro.
 * @property key identificador unico del filtro.
 * @property isSelected indica si el filtro esta seleccionado.
 */
data class FilterOption(
    val label: String,
    val key: String,
    val isSelected: Boolean
)

/**
 * Fila horizontal desplazable de chips de filtro.
 *
 * @param filters lista de opciones de filtro a mostrar.
 * @param onFilterSelected callback invocado con la clave del filtro seleccionado.
 * @param modifier modificador de Compose.
 */
@Composable
fun FilterChipRow(
    filters: List<FilterOption>,
    onFilterSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(filters) { filter ->
            FilterChip(
                selected = filter.isSelected,
                onClick = { onFilterSelected(filter.key) },
                label = {
                    Text(
                        text = filter.label,
                        style = MaterialTheme.typography.labelLarge
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}
