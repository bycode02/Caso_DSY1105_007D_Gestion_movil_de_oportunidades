package com.example.opportunitymanagement.data.remote

import retrofit2.http.GET

interface UsuarioApi {

    // Responsables de las oportunidades y autores de los seguimientos.
    @GET("api/usuarios")
    suspend fun listar(): List<UsuarioDto>
}
