package com.example.opportunitymanagement.viewmodel

/** Filtro de fecha de la lista de oportunidades, según la fecha de su evento. */
enum class RangoFecha(val etiqueta: String) {
    TODAS("Cualquier fecha"),
    PROXIMOS_7("Próximos 7 días"),
    PROXIMOS_30("Próximos 30 días"),
    PASADOS("Eventos pasados")
}
