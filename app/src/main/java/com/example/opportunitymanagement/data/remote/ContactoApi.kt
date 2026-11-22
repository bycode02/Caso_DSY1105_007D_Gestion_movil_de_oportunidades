package com.example.opportunitymanagement.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ContactoApi {

    @GET("api/contactos")
    suspend fun listar(): List<ContactoDto>

    @POST("api/contactos")
    suspend fun crear(@Body contacto: ContactoDto): ContactoDto

    @PUT("api/contactos/{id}")
    suspend fun actualizar(@Path("id") id: Long, @Body contacto: ContactoDto): ContactoDto

    @DELETE("api/contactos/{id}")
    suspend fun eliminar(@Path("id") id: Long): Response<Unit>
}
