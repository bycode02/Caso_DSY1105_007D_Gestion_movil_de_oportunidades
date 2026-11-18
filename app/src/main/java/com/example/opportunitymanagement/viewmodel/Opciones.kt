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
}
