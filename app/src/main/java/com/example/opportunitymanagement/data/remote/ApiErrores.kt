package com.example.opportunitymanagement.data.remote

import com.google.gson.Gson
import retrofit2.HttpException
import java.io.IOException

const val MENSAJE_SIN_CONEXION =
    "No se pudo conectar con el servidor. Revisa tu conexión e inténtalo nuevamente."

/** Lee el ErrorResponse que envía el backend, o null si la respuesta no lo trae. */
fun HttpException.errorDelServidor(): ErrorResponse? = try {
    response()?.errorBody()?.string()?.let { Gson().fromJson(it, ErrorResponse::class.java) }
} catch (exception: Exception) {
    null
}

/**
 * Mensaje para mostrar en pantalla según el error:
 * - sin red o servidor apagado → [MENSAJE_SIN_CONEXION];
 * - error del backend → su primer error de campo o su mensaje;
 * - cualquier otro → [porDefecto].
 */
fun Throwable.mensajeParaUsuario(porDefecto: String): String = when (this) {
    is IOException -> MENSAJE_SIN_CONEXION
    is HttpException -> errorDelServidor()?.let { it.errores?.values?.firstOrNull() ?: it.mensaje } ?: porDefecto
    else -> porDefecto
}
