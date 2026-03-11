package com.angelhr28.yapechallenge.feature.detail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.angelhr28.yapechallenge.core.extensions.toFormattedDate
import com.angelhr28.yapechallenge.domain.model.AccessAction
import com.angelhr28.yapechallenge.domain.model.AccessLog

/**
 * Seccion que muestra el historial de accesos a un documento.
 *
 * Presenta una lista de registros con iconos diferenciados segun la accion (ver o eliminar).
 *
 * @param accessLogs Lista de registros de acceso a mostrar.
 * @param modifier Modificador aplicado al contenedor.
 */
@Composable
fun AccessLogSection(
    accessLogs: List<AccessLog>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Historial de accesos",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (accessLogs.isEmpty()) {
            Text(
                text = "Sin accesos registrados",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        } else {
            accessLogs.forEachIndexed { index, log ->
                AccessLogItem(log = log)
                if (index < accessLogs.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
        }
    }
}

/**
 * Elemento individual del historial de accesos.
 *
 * @param log Registro de acceso a mostrar.
 * @param modifier Modificador aplicado a la fila.
 */
@Composable
private fun AccessLogItem(
    log: AccessLog,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = when (log.action) {
                AccessAction.VIEW -> Icons.Default.Visibility
                AccessAction.DELETE -> Icons.Default.Delete
            },
            contentDescription = log.action.displayName,
            modifier = Modifier.size(16.dp),
            tint = when (log.action) {
                AccessAction.VIEW -> MaterialTheme.colorScheme.primary
                AccessAction.DELETE -> MaterialTheme.colorScheme.error
            }
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = log.action.displayName,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = log.accessedAt.toFormattedDate(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            val location = log.location
            if (location != null) {
                Text(
                    text = location,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
