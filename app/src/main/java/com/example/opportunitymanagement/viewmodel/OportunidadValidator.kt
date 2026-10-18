package com.example.opportunitymanagement.viewmodel

fun validar(s: OportunidadUiState): Map<String, String> = buildMap {
 if (s.nombre.isBlank()) put("nombre", "Ingresa el nombre de la oportunidad")
 if (s.descripcion.isBlank()) put("descripcion", "Ingresa una descripción")
 if (s.fuente.isBlank()) put("fuente", "Indica la fuente de información")
 if (s.tipo !in Opciones.tipos) put("tipo", "Selecciona un tipo válido")
 if (s.prioridad !in Opciones.prioridades) put("prioridad", "Selecciona una prioridad")
 if (s.potencial !in Opciones.potenciales) put("potencial", "Selecciona un potencial válido")
 if (s.estado !in Opciones.estados) put("estado", "Selecciona un estado válido")
}
