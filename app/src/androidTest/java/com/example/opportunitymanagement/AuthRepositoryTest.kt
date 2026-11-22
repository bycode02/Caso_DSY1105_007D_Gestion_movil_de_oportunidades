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

/**
 * La contraseña la guarda y verifica el backend (sus pruebas cubren el hash).
 * Aquí se comprueba que la app normaliza el correo y guarda en Room el usuario del servidor.
 */
class AuthRepositoryTest {
    private lateinit var database: AppDatabase
    private lateinit var servidor: FakeAuthApi
    private lateinit var repository: AuthRepository

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            AppDatabase::class.java
        ).build()
        servidor = FakeAuthApi()
        repository = AuthRepository(database.usuarios(), servidor)
    }

    @After
    fun cleanup() = database.close()

    @Test
    fun registroCreaVendedorYLoGuardaConElIdDelServidor() = runBlocking {
        val user = repository.register("  Ana Pérez  ", "  ANA@EJEMPLO.CL ", "Clave1234!")
        assertEquals("Ana Pérez", user.nombre)
        assertEquals("ana@ejemplo.cl", user.email)
        assertEquals(Role.VENDEDOR, user.role)
        assertEquals(user.id, database.usuarios().findById(user.id)?.id)
        // La contraseña no queda en el teléfono.
        assertNull(database.credenciales().findByUserId(user.id))
        assertEquals(user.id, repository.login(" ANA@EJEMPLO.CL ", "Clave1234!")?.id)
        assertNull(repository.login(user.email, "incorrecta"))
    }

    @Test
    fun correoRepetidoSeRechazaSinImportarMayusculasNiEspacios() = runBlocking {
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
    fun loginRechazaCuentasInactivasOInexistentes() = runBlocking {
        val user = repository.register("Ana", "ana@ejemplo.cl", "Clave1234!")
        servidor.modificar(user.email) { it.copy(isActive = false) }
        assertNull(repository.login(user.email, "Clave1234!"))
        assertNull(repository.login("inexistente@ejemplo.cl", "Clave1234!"))
    }

    @Test
    fun loginTraeElRolActualDelServidor() = runBlocking {
        val user = repository.register("Ana", "ana@ejemplo.cl", "Clave1234!")
        servidor.modificar(user.email) { it.copy(role = Role.ADMINISTRADOR) }
        assertEquals(Role.ADMINISTRADOR, repository.login(user.email, "Clave1234!")?.role)
        assertEquals(Role.ADMINISTRADOR, database.usuarios().findById(user.id)?.role)
    }
}
