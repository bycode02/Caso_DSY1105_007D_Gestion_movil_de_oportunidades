package com.example.opportunitymanagement.data.repository

import com.example.opportunitymanagement.data.local.UsuarioDao
import com.example.opportunitymanagement.data.local.UsuarioEntity
import com.example.opportunitymanagement.data.remote.AuthApi
import com.example.opportunitymanagement.data.remote.LoginRequest
import com.example.opportunitymanagement.data.remote.RegistroRequest
import com.example.opportunitymanagement.data.remote.toEntity
import retrofit2.HttpException
import java.util.Locale

class EmailAlreadyRegisteredException : Exception()

/**
 * Autenticación contra el backend: el servidor guarda y verifica las contraseñas.
 * El usuario que responde se guarda en Room (con el id del servidor) para que la
 * sesión siga abierta al cerrar la app. El registro público siempre crea vendedores.
 */
class AuthRepository(
    private val usuarios: UsuarioDao,
    private val api: AuthApi
) {

    suspend fun register(name: String, email: String, password: String): UsuarioEntity {
        val usuario = try {
            api.registrar(RegistroRequest(name.trim(), normalizar(email), password)).toEntity()
        } catch (exception: HttpException) {
            if (exception.code() == 409) throw EmailAlreadyRegisteredException()
            throw exception
        }
        usuarios.upsert(usuario)
        return usuario
    }

    /** null si el correo o la contraseña no coinciden, o la cuenta está inactiva (401). */
    suspend fun login(email: String, password: String): UsuarioEntity? {
        val usuario = try {
            api.login(LoginRequest(normalizar(email), password)).toEntity()
        } catch (exception: HttpException) {
            if (exception.code() == 401) return null
            throw exception
        }
        usuarios.upsert(usuario)
        return usuario.takeIf { it.isActive }
    }

    private fun normalizar(email: String) = email.trim().lowercase(Locale.ROOT)
}
