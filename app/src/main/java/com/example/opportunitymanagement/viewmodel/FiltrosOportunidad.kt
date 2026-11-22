package com.example.opportunitymanagement.viewmodel

/** Búsqueda y filtros elegidos en la lista de oportunidades. null = sin filtrar por ese dato. */
data class FiltrosOportunidad(
    val texto: String = "",
    val estado: String? = null,
    val tipo: String? = null,
    val prioridad: String? = null,
    val organizacion: String? = null,
    val comuna: String? = null,
    val region: String? = null,
    val responsable: String? = null,
    val fecha: RangoFecha = RangoFecha.TODAS
)
