package com.example.opportunitymanagement

import com.example.opportunitymanagement.data.local.Role
import com.example.opportunitymanagement.data.remote.AuthApi
import com.example.opportunitymanagement.data.remote.LoginRequest
import com.example.opportunitymanagement.data.remote.RegistroRequest
import com.example.opportunitymanagement.data.remote.UsuarioDto
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response

/**
 * Backend de autenticación en memoria (para no depender de Mobile_Backend en estas pruebas).
 * Responde como el real: 409 si el correo existe, 401 si la clave no coincide o la cuenta está inactiva.
 */
class FakeAuthApi : AuthApi {
    private data class Cuenta(var usuario: UsuarioDto, val password: String)

    private val cuentas = mutableListOf<Cuenta>()
    private var siguienteId = 100L

    override suspend fun registrar(request: RegistroRequest): UsuarioDto {
        if (cuentas.any { it.usuario.email == request.email }) throw error(409)
        val usuario = UsuarioDto(siguienteId++, request.nombre, request.email, Role.VENDEDOR, true, 1, 1)
        cuentas += Cuenta(usuario, request.password)
        return usuario
    }

    override suspend fun login(request: LoginRequest): UsuarioDto =
        cuentas.find { it.usuario.email == request.email && it.password == request.password && it.usuario.isActive }
            ?.usuario ?: throw error(401)

    /** Cambia el rol o el estado de una cuenta "en el servidor". */
    fun modificar(email: String, cambio: (UsuarioDto) -> UsuarioDto) {
        cuentas.first { it.usuario.email == email }.let { it.usuario = cambio(it.usuario) }
    }

    private fun error(codigo: Int) = HttpException(Response.error<Any>(codigo, "".toResponseBody(null)))
}
