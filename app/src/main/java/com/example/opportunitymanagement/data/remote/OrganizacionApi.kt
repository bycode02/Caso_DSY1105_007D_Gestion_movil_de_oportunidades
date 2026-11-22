package com.example.opportunitymanagement.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface OrganizacionApi {

    @GET("api/organizaciones")
    suspend fun listar(): List<OrganizacionDto>

    @POST("api/organizaciones")
    suspend fun crear(@Body organizacion: OrganizacionDto): OrganizacionDto

    @PUT("api/organizaciones/{id}")
    suspend fun actualizar(@Path("id") id: Long, @Body organizacion: OrganizacionDto): OrganizacionDto

    // 204 si se eliminó; 409 si tiene contactos o eventos asociados.
    @DELETE("api/organizaciones/{id}")
    suspend fun eliminar(@Path("id") id: Long): Response<Unit>
}
