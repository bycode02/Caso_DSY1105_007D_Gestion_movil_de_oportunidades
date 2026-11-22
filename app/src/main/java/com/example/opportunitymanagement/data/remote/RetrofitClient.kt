package com.example.opportunitymanagement.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    /**
     * Backend Mobile_Backend (Spring Boot) ejecutándose en el computador de desarrollo.
     * 10.0.2.2 equivale a localhost desde el emulador de Android.
     * En un teléfono físico, reemplazar por la IP del computador en la red local
     * y agregarla a res/xml/network_security_config.xml.
     */
    const val BASE_URL = "http://10.0.2.2:8080/"

    /** [baseUrl] debe terminar en "/". Las pruebas lo apuntan a un servidor simulado. */
    fun crear(baseUrl: String = BASE_URL): Retrofit {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Una sola instancia de Retrofit para todas las APIs.
    private val retrofit: Retrofit by lazy { crear() }

    val authApi: AuthApi by lazy { retrofit.create(AuthApi::class.java) }
    val usuarioApi: UsuarioApi by lazy { retrofit.create(UsuarioApi::class.java) }
    val organizacionApi: OrganizacionApi by lazy { retrofit.create(OrganizacionApi::class.java) }
    val contactoApi: ContactoApi by lazy { retrofit.create(ContactoApi::class.java) }
    val eventoApi: EventoApi by lazy { retrofit.create(EventoApi::class.java) }
    val oportunidadApi: OportunidadApi by lazy { retrofit.create(OportunidadApi::class.java) }
    val seguimientoApi: SeguimientoApi by lazy { retrofit.create(SeguimientoApi::class.java) }
}
