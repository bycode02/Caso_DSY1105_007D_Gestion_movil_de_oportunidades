package com.example.opportunitymanagement.data.remote

/** Cuerpo de POST api/auth/registro. */
data class RegistroRequest(
    val nombre: String,
    val email: String,
    val password: String
)
