package com.example.opportunitymanagement.viewmodel

import com.example.opportunitymanagement.data.local.EventoConOrganizacion

data class EventoListaUiState(
    val items: List<EventoConOrganizacion> = emptyList(),
    val cargando: Boolean = true,
    val error: String? = null,
    val mensaje: String? = null
)
