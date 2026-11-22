package com.example.opportunitymanagement.viewmodel

import com.example.opportunitymanagement.data.local.EventoConOrganizacion

data class OportunidadUiState(
    val id: Long = 0,
    val nombre: String = "",
    val tipo: String = "",
    val descripcion: String = "",
    val fuente: String = "",
    val prioridad: String = "",
    val potencial: String = "",
    val estado: String = "Evento identificado",
    val observaciones: String = "",
    val referenciaGeografica: String = "",
    val fechaDeteccion: Long? = null,
    // Responsable guardado en la oportunidad; null mientras sea nueva.
    val responsableId: Long? = null,
    // Evento asociado (opcional) y opciones del desplegable de eventos.
    val eventoId: Long? = null,
    val eventos: List<EventoConOrganizacion> = emptyList(),
    val eliminando: Boolean = false,

    val nombreError: String? = null,
    val tipoError: String? = null,
    val descripcionError: String? = null,
    val fuenteError: String? = null,
    val prioridadError: String? = null,
    val potencialError: String? = null,
    val estadoError: String? = null,
    val eventoError: String? = null,


    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val guardadoExitosamente: Boolean = false,
    val mensaje: String? = null,
    val eliminadoExitosamente: Boolean = false,
    val cargaFallida: Boolean = false
)
