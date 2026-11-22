package com.example.opportunitymanagement.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

/** Registro e inicio de sesión: el servidor valida la contraseña. */
interface AuthApi {

    // 201 con el usuario creado; 409 si el correo ya está registrado.
    @POST("api/auth/registro")
    suspend fun registrar(@Body request: RegistroRequest): UsuarioDto

    // 200 con el usuario; 401 si el correo o la contraseña no coinciden.
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): UsuarioDto
}
