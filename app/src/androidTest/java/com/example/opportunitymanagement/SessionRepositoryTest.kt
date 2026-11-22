package com.example.opportunitymanagement

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.example.opportunitymanagement.data.local.AppDatabase
import com.example.opportunitymanagement.data.repository.AuthRepository
import com.example.opportunitymanagement.data.repository.SessionRepository
import com.example.opportunitymanagement.domain.usecase.LoginUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SessionRepositoryTest {
    private lateinit var database: AppDatabase
    private lateinit var auth: AuthRepository
    private lateinit var session: SessionRepository
    private lateinit var login: LoginUseCase

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            AppDatabase::class.java
        ).build()
        auth = AuthRepository(database.usuarios(), FakeAuthApi())
        session = SessionRepository(database.sesion())
        login = LoginUseCase(auth, session)
    }

    @After
    fun cleanup() = database.close()

    @Test
    fun loginCorrectoGuardaLaSesionConElUsuario() = runBlocking {
        assertNull(session.usuarioActual.first())
        val user = auth.register("Ana", "ana@ejemplo.cl", "Clave1234!")
        assertEquals(user.id, login("ana@ejemplo.cl", "Clave1234!")?.id)
        assertEquals("Ana", session.usuarioActual.first()?.nombre)
    }

    @Test
    fun loginFallidoNoCreaSesion() = runBlocking {
        auth.register("Ana", "ana@ejemplo.cl", "Clave1234!")
        assertNull(login("ana@ejemplo.cl", "otra"))
        assertNull(session.usuarioActual.first())
    }

    @Test
    fun soloExisteUnaSesionYCerrarLaElimina() = runBlocking {
        val ana = auth.register("Ana", "ana@ejemplo.cl", "Clave1234!")
        val luis = auth.register("Luis", "luis@ejemplo.cl", "Clave1234!")
        session.iniciar(ana.id)
        session.iniciar(luis.id)
        assertEquals(luis.id, session.usuarioActual.first()?.id)
        session.cerrar()
        assertNull(session.usuarioActual.first())
    }

    @Test
    fun cuentaDesactivadaInvalidaLaSesion() = runBlocking {
        val ana = auth.register("Ana", "ana@ejemplo.cl", "Clave1234!")
        session.iniciar(ana.id)
        database.usuarios().setUserActive(ana.id, active = false, updatedAt = 1)
        assertNull(session.usuarioActual.first())
    }
}
