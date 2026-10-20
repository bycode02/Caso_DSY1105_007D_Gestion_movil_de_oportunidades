package com.example.opportunitymanagement.data.repository

import com.example.opportunitymanagement.data.local.UsuarioDao
import com.example.opportunitymanagement.data.local.UsuarioEntity
import kotlinx.coroutines.flow.Flow

class UsuarioRepository(private val dao: UsuarioDao) {

    suspend fun observeUser(): Flow<List<UsuarioEntity>>{
        return dao.observeUsers()
    }

    suspend fun findById(id: Long): UsuarioEntity?{
        return dao.findById(id)
    }

    suspend fun findByEmail(email : String): UsuarioEntity?{
        return dao.findbyEmail(normalizeEmail(email))
    }

    suspend fun createUser(usuario: UsuarioEntity): Long {
        val normalizedEmail = normalizeEmail(usuario.email)

        check(dao.findbyEmail(normalizedEmail) == null) {
            "El correo ya está registrado"
        }
        return dao.inserUser(
            usuario.copy(
                nombre = usuario.nombre.trim(),
                email = normalizedEmail
            )
        )
    }

    suspend fun upadateUser(usuario : UsuarioEntity): Boolean{
        val normalizedUSer = usuario.copy(
            nombre = usuario.nombre.trim(),
            email = normalizeEmail(usuario.email),
            updateAt = System.currentTimeMillis()
        )

        return dao.updateUser(normalizedUSer) == 1
    }

    suspend fun setUserActive(
        userId : Long,
        active : Boolean
    ): Boolean{
        return dao.setUserActive(
            userId,
            active,
            updatedAt = System.currentTimeMillis()) == 1

    }

    private fun normalizeEmail(email: String): String {
        return email.trim().lowercase()
    }

}