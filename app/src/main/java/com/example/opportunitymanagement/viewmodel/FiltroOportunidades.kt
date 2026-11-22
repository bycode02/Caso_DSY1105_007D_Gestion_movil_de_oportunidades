package com.example.opportunitymanagement.viewmodel

import com.example.opportunitymanagement.data.local.OportunidadConResponsable

/**
 * Regla de búsqueda y filtros de la lista de oportunidades. Está separada del ViewModel
 * para poder probarla sola. Los filtros se combinan: una oportunidad debe cumplirlos todos.
 */
object FiltroOportunidades {

    private const val DIA_MS = 24L * 60 * 60 * 1000

    /** @param hoyUtc medianoche UTC del día de hoy (mismo formato que la fecha del evento). */
    fun aplicar(
        lista: List<OportunidadConResponsable>,
        filtros: FiltrosOportunidad,
        hoyUtc: Long
    ): List<OportunidadConResponsable> = lista.filter { cumple(it, filtros, hoyUtc) }

    fun cumple(item: OportunidadConResponsable, f: FiltrosOportunidad, hoyUtc: Long): Boolean {
        val o = item.oportunidad
        val texto = f.texto.trim()
        // "Buscar por nombre": de la oportunidad o de su evento.
        if (texto.isNotEmpty() &&
            !o.nombre.contains(texto, ignoreCase = true) &&
            item.eventoNombre?.contains(texto, ignoreCase = true) != true
        ) return false
        if (f.estado != null && o.estado != f.estado) return false
        if (f.tipo != null && o.tipo != f.tipo) return false
        if (f.prioridad != null && o.prioridad != f.prioridad) return false
        if (f.organizacion != null && item.organizacionNombre != f.organizacion) return false
        if (f.comuna != null && item.eventoComuna != f.comuna) return false
        if (f.region != null && item.eventoRegion != f.region) return false
        if (f.responsable != null && item.responsableNombre != f.responsable) return false
        return cumpleFecha(item.eventoFecha, f.fecha, hoyUtc)
    }

    private fun cumpleFecha(fechaEvento: Long?, rango: RangoFecha, hoyUtc: Long): Boolean {
        if (rango == RangoFecha.TODAS) return true
        // Sin evento no hay fecha con qué comparar.
        val fecha = fechaEvento ?: return false
        return when (rango) {
            RangoFecha.TODAS -> true
            RangoFecha.PROXIMOS_7 -> fecha in hoyUtc..(hoyUtc + 7 * DIA_MS)
            RangoFecha.PROXIMOS_30 -> fecha in hoyUtc..(hoyUtc + 30 * DIA_MS)
            RangoFecha.PASADOS -> fecha < hoyUtc
        }
    }

    fun hayFiltros(f: FiltrosOportunidad): Boolean = f != FiltrosOportunidad()

    // Opciones de los desplegables que dependen de los datos guardados (sin repetir, ordenadas).
    fun organizaciones(lista: List<OportunidadConResponsable>) = valores(lista) { it.organizacionNombre }
    fun comunas(lista: List<OportunidadConResponsable>) = valores(lista) { it.eventoComuna }
    fun regiones(lista: List<OportunidadConResponsable>) = valores(lista) { it.eventoRegion }
    fun responsables(lista: List<OportunidadConResponsable>) = valores(lista) { it.responsableNombre }

    private fun valores(
        lista: List<OportunidadConResponsable>,
        dato: (OportunidadConResponsable) -> String?
    ): List<String> = lista.mapNotNull(dato).filter { it.isNotBlank() }.distinct().sorted()
}
