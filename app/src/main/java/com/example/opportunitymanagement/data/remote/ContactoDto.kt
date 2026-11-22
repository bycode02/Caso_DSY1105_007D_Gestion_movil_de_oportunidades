package com.example.opportunitymanagement.data.remote

import com.example.opportunitymanagement.data.local.ContactoEntity

/** Representación JSON de un contacto en la API REST. */
data class ContactoDto(
    // null al crear: el identificador lo asigna el servidor.
    val id: Long? = null,
    val nombre: String,
    val organizacionId: Long,
    // Solo viene en las respuestas (en Room se obtiene con un JOIN).
    val organizacionNombre: String? = null,
    val cargo: String,
    val area: String,
    val correo: String? = null,
    val telefono: String? = null,
    val medioPreferente: String,
    val estado: String,
    val fechaUltimoContacto: Long? = null,
    val observaciones: String? = null
)

fun ContactoEntity.toDto() = ContactoDto(
    id = id.takeIf { it != 0L },
    nombre = nombre,
    organizacionId = organizacionId,
    cargo = cargo,
    area = area,
    correo = correo,
    telefono = telefono,
    medioPreferente = medioPreferente,
    estado = estado,
    fechaUltimoContacto = fechaUltimoContacto,
    observaciones = observaciones
)

fun ContactoDto.toEntity() = ContactoEntity(
    id = id ?: 0,
    nombre = nombre,
    organizacionId = organizacionId,
    cargo = cargo,
    area = area,
    correo = correo.orEmpty(),
    telefono = telefono.orEmpty(),
    medioPreferente = medioPreferente,
    estado = estado,
    fechaUltimoContacto = fechaUltimoContacto,
    observaciones = observaciones.orEmpty()
)
