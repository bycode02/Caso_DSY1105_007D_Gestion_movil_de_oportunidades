package com.example.opportunitymanagement.data.remote

import com.example.opportunitymanagement.data.local.OrganizacionEntity

/** Representación JSON de una organización en la API REST. */
data class OrganizacionDto(
    // null al crear: el identificador lo asigna el servidor.
    val id: Long? = null,
    val nombre: String,
    val tipo: String,
    val rubro: String,
    val comuna: String,
    val region: String,
    val sitioWeb: String? = null,
    val observaciones: String? = null
)

fun OrganizacionEntity.toDto() = OrganizacionDto(
    id = id.takeIf { it != 0L },
    nombre = nombre,
    tipo = tipo,
    rubro = rubro,
    comuna = comuna,
    region = region,
    sitioWeb = sitioWeb,
    observaciones = observaciones
)

fun OrganizacionDto.toEntity() = OrganizacionEntity(
    id = id ?: 0,
    nombre = nombre,
    tipo = tipo,
    rubro = rubro,
    comuna = comuna,
    region = region,
    sitioWeb = sitioWeb.orEmpty(),
    observaciones = observaciones.orEmpty()
)
