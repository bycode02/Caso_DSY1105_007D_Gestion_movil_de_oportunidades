package com.example.opportunitymanagement.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface SeguimientoApi {

    // Todos los seguimientos, para sincronizar la copia local.
    @GET("api/seguimientos")
    suspend fun todos(): List<SeguimientoDto>

    @GET("api/oportunidades/{oportunidadId}/seguimientos")
    suspend fun historial(@Path("oportunidadId") oportunidadId: Long): List<SeguimientoDto>

    @POST("api/oportunidades/{oportunidadId}/seguimientos")
    suspend fun registrar(
        @Path("oportunidadId") oportunidadId: Long,
        @Body seguimiento: SeguimientoDto
    ): SeguimientoDto
}
