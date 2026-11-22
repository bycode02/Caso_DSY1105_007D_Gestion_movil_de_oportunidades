package com.example.opportunitymanagement.data.remote

/** Cuerpo de POST api/auth/login. */
data class LoginRequest(
    val email: String,
    val password: String
)
