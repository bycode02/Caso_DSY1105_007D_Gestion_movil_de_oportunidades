package com.example.opportunitymanagement.data.remote

import com.example.opportunitymanagement.data.local.OportunidadEntity

/** Representación JSON de una oportunidad en la API REST. */
data class OportunidadDto(
    // null al crear: el identificador lo asigna el servidor.
    val id: Long? = null,
    val nombre: String,
    val tipo: String,
    val descripcion: String,
    val fuente: String,
    val prioridad: String,
    val potencial: String,
    val estado: String,
    val observaciones: String? = null,
    val responsableId: Long? = null,
    val fechaDeteccion: Long? = null,
    val ultimaActualizacion: Long? = null,
    val referenciaGeografica: String? = null,
    val eventoId: Long? = null
)

fun OportunidadEntity.toDto() = OportunidadDto(
    id = id.takeIf { it != 0L },
    nombre = nombre,
    tipo = tipo,
    descripcion = descripcion,
    fuente = fuente,
    prioridad = prioridad,
    potencial = potencial,
    estado = estado,
    observaciones = observaciones,
    responsableId = responsableId,
    fechaDeteccion = fechaDeteccion,
    ultimaActualizacion = ultimaActualizacion,
    referenciaGeografica = referenciaGeografica,
    eventoId = eventoId
)

fun OportunidadDto.toEntity(): OportunidadEntity {
    val ahora = System.currentTimeMillis()
    return OportunidadEntity(
        id = id ?: 0,
        nombre = nombre,
        tipo = tipo,
        descripcion = descripcion,
        fuente = fuente,
        prioridad = prioridad,
        potencial = potencial,
        estado = estado,
        observaciones = observaciones.orEmpty(),
        responsableId = responsableId,
        fechaDeteccion = fechaDeteccion ?: ahora,
        ultimaActualizacion = ultimaActualizacion ?: ahora,
        referenciaGeografica = referenciaGeografica.orEmpty(),
        eventoId = eventoId
    )
}
