package com.example.opportunitymanagement.data.remote

import com.example.opportunitymanagement.data.local.SeguimientoEntity

/** Representación JSON de una acción del historial en la API REST. */
data class SeguimientoDto(
    val id: Long? = null,
    // Viene en las respuestas; al registrar va en la ruta.
    val oportunidadId: Long? = null,
    val usuarioId: Long? = null,
    val tipoAccion: String,
    // "Hora de reloj": medianoche UTC + minutos.
    val fechaHora: Long,
    val resultado: String,
    val contactoId: Long? = null,
    val proximaAccion: String? = null,
    val fechaProximoSeguimiento: Long? = null,
    val observaciones: String? = null,
    // Solo al registrar: estado al que pasa la oportunidad. El servidor cambia el estado
    // y agrega el registro "Cambio de estado" al historial.
    val nuevoEstado: String? = null
)

fun SeguimientoEntity.toDto(nuevoEstado: String? = null) = SeguimientoDto(
    id = id.takeIf { it != 0L },
    oportunidadId = oportunidadId,
    usuarioId = usuarioId,
    tipoAccion = tipoAccion,
    fechaHora = fechaHora,
    resultado = resultado,
    contactoId = contactoId,
    proximaAccion = proximaAccion,
    fechaProximoSeguimiento = fechaProximoSeguimiento,
    observaciones = observaciones,
    nuevoEstado = nuevoEstado
)

fun SeguimientoDto.toEntity() = SeguimientoEntity(
    id = id ?: 0,
    oportunidadId = checkNotNull(oportunidadId) { "El seguimiento no indica su oportunidad" },
    usuarioId = usuarioId,
    tipoAccion = tipoAccion,
    fechaHora = fechaHora,
    resultado = resultado,
    contactoId = contactoId,
    proximaAccion = proximaAccion.orEmpty(),
    fechaProximoSeguimiento = fechaProximoSeguimiento,
    observaciones = observaciones.orEmpty()
)
