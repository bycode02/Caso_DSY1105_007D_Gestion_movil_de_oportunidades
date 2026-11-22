package com.example.opportunitymanagement.viewmodel

import com.example.opportunitymanagement.data.local.OportunidadConResponsable

data class ListaUiState(
    // Oportunidades que cumplen la búsqueda y los filtros.
    val items: List<OportunidadConResponsable> = emptyList(),
    // Total guardado, sin filtrar ("3 de 12 oportunidades").
    val total: Int = 0,
    val filtros: FiltrosOportunidad = FiltrosOportunidad(),
    // Hoy (medianoche UTC): para mostrar si el próximo seguimiento está atrasado.
    val hoyUtc: Long = 0L,
    // Opciones de los filtros que salen de los datos guardados.
    val opcionesOrganizacion: List<String> = emptyList(),
    val opcionesComuna: List<String> = emptyList(),
    val opcionesRegion: List<String> = emptyList(),
    val opcionesResponsable: List<String> = emptyList(),
    val cargando: Boolean = true,
    val error: String? = null,
    val eliminandoId: Long? = null,
    val mensaje: String? = null
)
