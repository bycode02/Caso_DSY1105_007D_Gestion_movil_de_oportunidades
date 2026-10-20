package com.example.opportunitymanagement.data.repository

import androidx.room.withTransaction
import com.example.opportunitymanagement.data.local.AppDatabase
import com.example.opportunitymanagement.data.local.CredentialEntity
import com.example.opportunitymanagement.data.local.Role
import com.example.opportunitymanagement.data.local.UsuarioEntity
import com.example.opportunitymanagement.data.security.PasswordHasher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

class EmailAlreadyRegisteredException : Exception()

/**
 * Autenticación local. El registro público siempre crea vendedores.
 * La administración de permisos se implementa por separado.
 */
class AuthRepository(
    private val database: AppDatabase,
    private val passwordHasher: PasswordHasher = PasswordHasher()
) {
    private val users = database.usuarios()
    private val credentials = database.credenciales()

    suspend fun register(name: String, email: String, password: String): UsuarioEntity {
        val normalizedEmail = email.trim().lowercase(Locale.ROOT)
        val hash = withContext(Dispatchers.Default) {
            passwordHasher.createHash(password)
        }
        // Si falla la credencial, también se revierte la creación del usuario.
        return database.withTransaction {
            if (users.findbyEmail(normalizedEmail) != null) {
                throw EmailAlreadyRegisteredException()
            }
            val now = System.currentTimeMillis()
            val user = UsuarioEntity(
                id = 0,
                nombre = name.trim(),
                email = normalizedEmail,
                role = Role.VENDEDOR,
                isActive = true,
                createAt = now,
                updateAt = now
            )
            val id = users.inserUser(user)
            credentials.insertCredential(
                CredentialEntity(
                    userId = id,
                    passwordHash = hash.hash,
                    passwordSalt = hash.salt
                )
            )
            user.copy(id = id)
        }
    }

    suspend fun login(email: String, password: String): UsuarioEntity? {
        val user = users.findbyEmail(email.trim().lowercase(Locale.ROOT)) ?: return null
        if (!user.isActive) return null
        val credential = credentials.findByUserId(user.id) ?: return null
        val matches = withContext(Dispatchers.Default) {
            passwordHasher.verifyPassword(
                password,
                credential.passwordHash,
                credential.passwordSalt
            )
        }
        if (!matches) return null
        // Recupera el estado y rol actuales, incluso si cambiaron durante el hash.
        return users.findById(user.id)?.takeIf { it.isActive }
    }
}
