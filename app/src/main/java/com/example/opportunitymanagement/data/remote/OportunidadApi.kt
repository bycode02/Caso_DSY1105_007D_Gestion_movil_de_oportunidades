package com.example.opportunitymanagement.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/** Endpoints REST del microservicio de oportunidades. */
interface OportunidadApi {

    @GET("api/oportunidades")
    suspend fun listar(): List<OportunidadDto>

    @GET("api/oportunidades/{id}")
    suspend fun buscar(@Path("id") id: Long): OportunidadDto

    @POST("api/oportunidades")
    suspend fun crear(@Body oportunidad: OportunidadDto): OportunidadDto

    @PUT("api/oportunidades/{id}")
    suspend fun actualizar(
        @Path("id") id: Long,
        @Body oportunidad: OportunidadDto,
        @Header("X-Usuario-Id") usuarioId: Long?   // autor del cambio de estado
    ): OportunidadDto

    // Response<Unit> admite tanto 200 como 204 sin cuerpo.
    @DELETE("api/oportunidades/{id}")
    suspend fun eliminar(@Path("id") id: Long): Response<Unit>
}
