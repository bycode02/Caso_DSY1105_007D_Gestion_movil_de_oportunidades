package com.example.opportunitymanagement

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.example.opportunitymanagement.data.local.AppDatabase
import com.example.opportunitymanagement.data.local.Role
import com.example.opportunitymanagement.data.repository.AuthRepository
import com.example.opportunitymanagement.data.repository.EmailAlreadyRegisteredException
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AuthRepositoryTest {
    private lateinit var database: AppDatabase
    private lateinit var repository: AuthRepository

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            AppDatabase::class.java
        ).build()
        repository = AuthRepository(database)
    }

    @After
    fun cleanup() = database.close()

    @Test
    fun publicRegistrationCreatesSellerAndHashedCredential() = runBlocking {
        val user = repository.register("  Ana Pérez  ", "  ANA@EJEMPLO.CL ", "Clave1234!")
        assertEquals("Ana Pérez", user.nombre)
        assertEquals("ana@ejemplo.cl", user.email)
        assertEquals(Role.VENDEDOR, user.role)
        val credential = database.credenciales().findByUserId(user.id)!!
        assertNotEquals("Clave1234!", credential.passwordHash)
        assertTrue(credential.passwordSalt.isNotBlank())
        assertEquals(user.id, repository.login(" ANA@EJEMPLO.CL ", "Clave1234!")?.id)
        assertNull(repository.login(user.email, "incorrecta"))
    }

    @Test
    fun duplicateEmailIsRejectedIgnoringCaseAndSpaces() = runBlocking {
        val original = repository.register("Ana", "ana@ejemplo.cl", "Clave1234!")
        try {
            repository.register("Otra cuenta", " ANA@EJEMPLO.CL ", "OtraClave123!")
            fail("No debe permitir el correo duplicado")
        } catch (_: EmailAlreadyRegisteredException) {
            assertEquals(original.id, database.usuarios().findbyEmail("ana@ejemplo.cl")?.id)
            assertNotNull(repository.login("ana@ejemplo.cl", "Clave1234!"))
        }
    }

    @Test
    fun credentialFailureRollsBackUserInsert() = runBlocking {
        database.openHelper.writableDatabase.execSQL(
            """
            CREATE TRIGGER fail_credential BEFORE INSERT ON credentials
            BEGIN SELECT RAISE(ABORT, 'forced test failure'); END
            """.trimIndent()
        )
        try {
            repository.register("Ana", "rollback@ejemplo.cl", "Clave1234!")
            fail("La inserción debe fallar")
        } catch (_: android.database.sqlite.SQLiteException) {
            assertNull(database.usuarios().findbyEmail("rollback@ejemplo.cl"))
        }
    }

    @Test
    fun loginRejectsInactiveMissingOrCredentiallessUsers() = runBlocking {
        val user = repository.register("Ana", "ana@ejemplo.cl", "Clave1234!")
        database.usuarios().setUserActive(user.id, false, System.currentTimeMillis())
        assertNull(repository.login(user.email, "Clave1234!"))
        assertNull(repository.login("inexistente@ejemplo.cl", "Clave1234!"))
        database.usuarios().setUserActive(user.id, true, System.currentTimeMillis())
        database.credenciales().deleteByUserId(user.id)
        assertNull(repository.login(user.email, "Clave1234!"))
    }

    @Test
    fun loginReturnsStoredRoleInsteadOfAssigningOne() = runBlocking {
        val user = repository.register("Ana", "ana@ejemplo.cl", "Clave1234!")
        // Preparación de un administrador exclusivamente en esta BD de prueba.
        database.usuarios().updateUser(user.copy(role = Role.ADMINISTRADOR))
        assertEquals(Role.ADMINISTRADOR, repository.login(user.email, "Clave1234!")?.role)
    }
}
