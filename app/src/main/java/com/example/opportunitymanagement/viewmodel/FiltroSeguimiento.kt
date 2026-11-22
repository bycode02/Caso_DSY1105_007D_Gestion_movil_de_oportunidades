package com.example.opportunitymanagement.viewmodel

/** Filtro por la próxima acción de seguimiento de la oportunidad. */
enum class FiltroSeguimiento(val etiqueta: String) {
    // La fecha del próximo seguimiento ya pasó.
    ATRASADOS("Atrasados"),
    // Tiene próximo seguimiento hoy o más adelante.
    PENDIENTES("Pendientes")
}
