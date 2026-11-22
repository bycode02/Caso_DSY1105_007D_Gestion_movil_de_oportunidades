package com.example.opportunitymanagement.ui.followups

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.opportunitymanagement.data.local.SeguimientoConDetalle
import com.example.opportunitymanagement.data.local.SeguimientoEntity
import com.example.opportunitymanagement.ui.theme.Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme
import com.example.opportunitymanagement.utils.FormatoFecha

/**
 * Historial de la oportunidad: acciones registradas y cambios de estado, lo más reciente
 * primero. El botón abre el formulario para registrar una acción nueva.
 */
@Composable
fun SeccionHistorial(
    historial: List<SeguimientoConDetalle>,
    enabled: Boolean,
    onRegistrarAccion: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                "HISTORIAL (${historial.size})",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "Guarda los cambios de la oportunidad antes de registrar una acción.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = onRegistrarAccion, enabled = enabled, modifier = Modifier.fillMaxWidth()) {
                Text("+ Registrar acción")
            }
            if (historial.isEmpty()) {
                Text(
                    "Aún no hay acciones registradas para esta oportunidad.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            historial.forEach { ItemHistorial(it) }
        }
    }
}

/** Una entrada del historial. Los cambios de estado automáticos se ven más simples. */
@Composable
fun ItemHistorial(item: SeguimientoConDetalle) {
    val s = item.seguimiento
    val encabezado = buildString {
        append(FormatoFecha.fechaHora(s.fechaHora))
        append(" · ").append(s.tipoAccion)
        item.usuarioNombre?.let { append(" · ").append(it) }
    }
    if (s.tipoAccion == SeguimientoEntity.TIPO_CAMBIO_ESTADO) {
        Text(
            "$encabezado\n${s.resultado}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        return
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(encabezado, style = MaterialTheme.typography.labelLarge)
            item.contactoNombre?.let { Text("Con: $it", style = MaterialTheme.typography.bodyMedium) }
            Text(s.resultado, style = MaterialTheme.typography.bodyMedium)
            if (s.observaciones.isNotBlank()) {
                Text(s.observaciones, style = MaterialTheme.typography.bodySmall)
            }
            s.fechaProximoSeguimiento?.let { fecha ->
                Text(
                    "Próxima: ${s.proximaAccion} · ${FormatoFecha.fecha(fecha)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

private val historialEjemplo = listOf(
    SeguimientoConDetalle(
        SeguimientoEntity(
            id = 3, oportunidadId = 1, usuarioId = 1, tipoAccion = SeguimientoEntity.TIPO_CAMBIO_ESTADO,
            fechaHora = 1_805_068_800_000 + 631 * 60_000L, resultado = "Contacto realizado → Propuesta enviada"
        ),
        usuarioNombre = "Ana Pérez", contactoNombre = null
    ),
    SeguimientoConDetalle(
        SeguimientoEntity(
            id = 2, oportunidadId = 1, usuarioId = 1, tipoAccion = "Envío de propuesta",
            fechaHora = 1_805_068_800_000 + 630 * 60_000L, resultado = "Se envió la propuesta por correo.",
            contactoId = 5, proximaAccion = "Seguimiento", fechaProximoSeguimiento = 1_805_673_600_000
        ),
        usuarioNombre = "Ana Pérez", contactoNombre = "Laura Soto"
    )
)

@Preview(showBackground = true, widthDp = 390)
@Composable
private fun SeccionHistorialPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme(dynamicColor = false) {
        SeccionHistorial(historialEjemplo, enabled = true) {}
    }
}

@Preview(showBackground = true, widthDp = 390)
@Composable
private fun SeccionHistorialVaciaPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme(dynamicColor = false) {
        SeccionHistorial(emptyList(), enabled = true) {}
    }
}

@Preview(showBackground = true, widthDp = 390)
@Composable
private fun ItemHistorialPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme(dynamicColor = false) {
        ItemHistorial(historialEjemplo.last())
    }
}
