package com.example.opportunitymanagement.data.remote

import com.example.opportunitymanagement.data.local.EventoEntity

/** Representación JSON de un evento en la API REST. */
data class EventoDto(
    // null al crear: el identificador lo asigna el servidor.
    val id: Long? = null,
    val nombre: String,
    val tipo: String,
    val descripcion: String? = null,
    // Medianoche UTC en milisegundos.
    val fechaInicio: Long,
    // Minutos desde la medianoche.
    val horaInicio: Int,
    val horaTermino: Int,
    val comuna: String,
    val region: String,
    val organizacionId: Long,
    // Solo viene en las respuestas.
    val organizacionNombre: String? = null,
    val publicoEsperado: String,
    val condiciones: String? = null
)

fun EventoEntity.toDto() = EventoDto(
    id = id.takeIf { it != 0L },
    nombre = nombre,
    tipo = tipo,
    descripcion = descripcion,
    fechaInicio = fechaInicio,
    horaInicio = horaInicio,
    horaTermino = horaTermino,
    comuna = comuna,
    region = region,
    organizacionId = organizacionId,
    publicoEsperado = publicoEsperado,
    condiciones = condiciones
)

fun EventoDto.toEntity() = EventoEntity(
    id = id ?: 0,
    nombre = nombre,
    tipo = tipo,
    descripcion = descripcion.orEmpty(),
    fechaInicio = fechaInicio,
    horaInicio = horaInicio,
    horaTermino = horaTermino,
    comuna = comuna,
    region = region,
    organizacionId = organizacionId,
    publicoEsperado = publicoEsperado,
    condiciones = condiciones.orEmpty()
)
