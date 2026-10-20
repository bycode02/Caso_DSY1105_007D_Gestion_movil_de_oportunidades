package com.example.opportunitymanagement.data.repository

import com.example.opportunitymanagement.data.local.CredentialDao
import com.example.opportunitymanagement.data.local.CredentialEntity

class CredentialRepository(
    private val dao: CredentialDao
) {

    suspend fun findByUserId(userId: Long): CredentialEntity? {

        return dao.findByUserId(userId)
    }

    suspend fun createCredential(credential: CredentialEntity): Long {
        require(credential.userId > 0) {
            "el id no puede ser 0"
        }
        require(credential.passwordHash.isNotBlank()) {
            "el hash de la contraseña es obligatorio"
        }
        require(credential.passwordSalt.isNotBlank()) {
            "el salt de la contraseña es obligatorio"
        }
        check(dao.findByUserId(credential.userId) == null) {
            "el usuario ya tiene credenciales"
        }
        return dao.insertCredential(credential)
    }

    suspend fun updateCredential(
        credential: CredentialEntity
    ): Boolean {
        require(credential.id > 0) {
            "La credencial no es válida"
        }

        require(credential.passwordHash.isNotBlank()) {
            "El hash de la contraseña es obligatorio"
        }

        require(credential.passwordSalt.isNotBlank()) {
            "La sal de la contraseña es obligatoria"
        }

        return dao.updateCredential(credential) == 1
    }

    suspend fun deleteByUserId(userId : Long): Boolean{
        return dao.deleteByUserId(userId) ==1
    }

}
