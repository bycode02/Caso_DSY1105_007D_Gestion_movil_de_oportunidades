package com.example.opportunitymanagement.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface EventoApi {

    @GET("api/eventos")
    suspend fun listar(): List<EventoDto>

    @POST("api/eventos")
    suspend fun crear(@Body evento: EventoDto): EventoDto

    @PUT("api/eventos/{id}")
    suspend fun actualizar(@Path("id") id: Long, @Body evento: EventoDto): EventoDto

    // 204 si se eliminó; 409 si tiene oportunidades asociadas.
    @DELETE("api/eventos/{id}")
    suspend fun eliminar(@Path("id") id: Long): Response<Unit>
}
