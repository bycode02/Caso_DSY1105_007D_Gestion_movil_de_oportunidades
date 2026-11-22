package com.example.opportunitymanagement.viewmodel

object Opciones {
 val tipos = listOf("Evento corporativo", "Feria empresarial", "Actividad municipal", "Jornada de beneficios", "Feria institucional", "Actividad de trabajadores", "Activación comercial", "Otra instancia relevante")
 val prioridades = listOf("Alta", "Media", "Baja")
 val potenciales = listOf("Alto", "Medio", "Bajo")
 val estados = listOf("Evento identificado", "Contacto pendiente", "Contacto realizado", "Propuesta enviada", "En espera de respuesta", "Participación confirmada", "Oportunidad descartada")
 val tiposOrganizacion = listOf("Empresa", "Municipalidad", "Institución pública", "Institución privada",
  "Organizador de eventos", "Centro comercial", "Asociación", "Otra entidad")
 val areasContacto = listOf("Eventos", "Activaciones", "Marketing", "Comunicaciones",
  "Recursos Humanos", "Bienestar", "Beneficios", "Administración")
 val mediosContacto = listOf("Correo", "Teléfono", "Reunión presencial", "Videollamada")
 val estadosContacto = listOf("Por contactar", "Contactado", "Sin respuesta", "No disponible")
 // Tipos de acción de seguimiento del caso (página 10).
 val tiposAccion = listOf("Investigación inicial", "Búsqueda de contacto", "Primer contacto",
  "Envío de presentación", "Envío de propuesta", "Solicitud de antecedentes", "Reunión",
  "Llamada", "Seguimiento", "Recepción de respuesta", "Confirmación", "Cierre")

 /**
  * Estado que normalmente sigue a cada acción; se le propone al usuario al registrarla.
  * null = la acción no implica un cambio de estado claro (por ejemplo, una respuesta
  * puede ser positiva o negativa).
  */
 fun estadoSugerido(tipoAccion: String): String? = when (tipoAccion) {
  "Búsqueda de contacto" -> "Contacto pendiente"
  "Primer contacto", "Llamada", "Reunión", "Envío de presentación" -> "Contacto realizado"
  "Envío de propuesta" -> "Propuesta enviada"
  "Solicitud de antecedentes", "Seguimiento" -> "En espera de respuesta"
  "Confirmación" -> "Participación confirmada"
  else -> null
 }
}
