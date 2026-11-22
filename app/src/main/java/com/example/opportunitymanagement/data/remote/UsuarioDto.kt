package com.example.opportunitymanagement.data.remote

import com.example.opportunitymanagement.data.local.Role
import com.example.opportunitymanagement.data.local.UsuarioEntity

/** Usuario tal como lo devuelve el backend (nunca incluye la contraseña). */
data class UsuarioDto(
    val id: Long,
    val nombre: String,
    val email: String,
    val role: Role,
    val isActive: Boolean,
    val createAt: Long,
    val updateAt: Long
)

fun UsuarioDto.toEntity() = UsuarioEntity(
    id = id,
    nombre = nombre,
    email = email,
    role = role,
    isActive = isActive,
    createAt = createAt,
    updateAt = updateAt
)
