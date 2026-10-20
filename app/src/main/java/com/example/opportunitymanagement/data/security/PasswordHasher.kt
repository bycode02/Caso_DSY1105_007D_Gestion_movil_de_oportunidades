package com.example.opportunitymanagement.data.security

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

data class PasswordHashResult(
    val hash: String,
    val salt: String
)

class PasswordHasher {

    private val secureRandom = SecureRandom()

    fun createHash(password: String): PasswordHashResult {
        val saltBytes = ByteArray(SALT_SIZE)
        secureRandom.nextBytes(saltBytes)

        val hashBytes = generateHash(
            password = password,
            salt = saltBytes
        )

        return PasswordHashResult(
            hash = Base64.encodeToString(
                hashBytes,
                Base64.NO_WRAP
            ),
            salt = Base64.encodeToString(
                saltBytes,
                Base64.NO_WRAP
            )
        )
    }

    fun verifyPassword(
        password: String,
        savedHash: String,
        savedSalt: String
    ): Boolean {
        val saltBytes = Base64.decode(
            savedSalt,
            Base64.NO_WRAP
        )

        val expectedHash = Base64.decode(
            savedHash,
            Base64.NO_WRAP
        )

        val calculatedHash = generateHash(
            password = password,
            salt = saltBytes
        )

        return MessageDigest.isEqual(
            expectedHash,
            calculatedHash
        )
    }

    private fun generateHash(
        password: String,
        salt: ByteArray
    ): ByteArray {
        val specification = PBEKeySpec(
            password.toCharArray(),
            salt,
            ITERATIONS,
            KEY_LENGTH
        )

        return try {
            SecretKeyFactory
                .getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(specification)
                .encoded
        } finally {
            specification.clearPassword()
        }
    }

    companion object {
        private const val ITERATIONS = 120_000
        private const val KEY_LENGTH = 256
        private const val SALT_SIZE = 16
    }
}