package com.example.opportunitymanagement

import com.example.opportunitymanagement.data.local.DatosServidor
import com.example.opportunitymanagement.data.local.OportunidadEntity
import com.example.opportunitymanagement.data.local.Role
import com.example.opportunitymanagement.data.local.SincronizacionDao
import com.example.opportunitymanagement.data.local.UsuarioDao
import com.example.opportunitymanagement.data.local.UsuarioEntity
import com.example.opportunitymanagement.data.remote.AuthApi
import com.example.opportunitymanagement.data.remote.ContactoApi
import com.example.opportunitymanagement.data.remote.EventoApi
import com.example.opportunitymanagement.data.remote.OportunidadApi
import com.example.opportunitymanagement.data.remote.OrganizacionApi
import com.example.opportunitymanagement.data.remote.RetrofitClient
import com.example.opportunitymanagement.data.remote.SeguimientoApi
import com.example.opportunitymanagement.data.remote.UsuarioApi
import com.example.opportunitymanagement.data.remote.mensajeParaUsuario
import com.example.opportunitymanagement.data.repository.AuthRepository
import com.example.opportunitymanagement.data.repository.EmailAlreadyRegisteredException
import com.example.opportunitymanagement.data.repository.OportunidadRepository
import com.example.opportunitymanagement.data.repository.OrganizacionEnUsoException
import com.example.opportunitymanagement.data.repository.OrganizacionRepository
import com.example.opportunitymanagement.data.repository.SincronizacionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Retrofit

/**
 * Repositories con Retrofit real contra un servidor HTTP simulado: verifican rutas, métodos,
 * cuerpos JSON (los mismos que envía Mobile_Backend) y la traducción de los códigos de error.
 */
class RepositoriosRemotosTest {
    private val server = MockWebServer()
    private lateinit var retrofit: Retrofit

    @Before fun setup() {
        server.start()
        retrofit = RetrofitClient.crear(server.url("/").toString())
    }

    @After fun cleanup() { server.shutdown() }

    private inline fun <reified T> api(): T = retrofit.create(T::class.java)

    private fun responder(codigo: Int, cuerpo: String = "") =
        server.enqueue(MockResponse().setResponseCode(codigo).setBody(cuerpo))

    // JSON de una oportunidad tal como la responde el backend (incluye campos de detalle).
    private val oportunidadJson = """
        {"id":7,"nombre":"Stand dental","tipo":"Feria empresarial","descripcion":"Demo",
         "fuente":"Sitio web","prioridad":"Alta","potencial":"Medio","estado":"Propuesta enviada",
         "observaciones":"","responsableId":42,"responsableNombre":"Ana Pérez",
         "fechaDeteccion":1000,"ultimaActualizacion":2000,"referenciaGeografica":"",
         "eventoId":9,"eventoNombre":"Feria","organizacionNombre":"Municipalidad Ficticia"}
    """.trimIndent()

    private fun oportunidad(id: Long) = OportunidadEntity(
        id = id, nombre = "Stand dental", tipo = "Feria empresarial", descripcion = "Demo",
        fuente = "Sitio web", prioridad = "Alta", potencial = "Medio", estado = "Propuesta enviada",
        responsableId = 42, eventoId = 9
    )

    // ---------- Oportunidades ----------

    @Test fun crearEnviaPostSinIdYGuardaLaRespuestaEnRoom() = runTest {
        val dao = FakeOportunidadDao()
        responder(201, oportunidadJson)

        val id = OportunidadRepository(dao, api<OportunidadApi>()).guardar(oportunidad(0), usuarioId = 42)

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/oportunidades", request.path)
        assertFalse(request.body.readUtf8().contains("\"id\""))
        assertEquals(7L, id)
        assertEquals("Propuesta enviada", dao.items.value.single().estado)
        assertEquals(1000L, dao.items.value.single().fechaDeteccion)
    }

    @Test fun actualizarEnviaPutConElAutorDelCambio() = runTest {
        val dao = FakeOportunidadDao().apply { items.value = listOf(oportunidad(7)) }
        responder(200, oportunidadJson)

        OportunidadRepository(dao, api<OportunidadApi>()).guardar(oportunidad(7), usuarioId = 42)

        val request = server.takeRequest()
        assertEquals("PUT", request.method)
        assertEquals("/api/oportunidades/7", request.path)
        assertEquals("42", request.getHeader("X-Usuario-Id"))
    }

    @Test fun actualizarUnaQueYaNoExisteAvisa() = runTest {
        responder(404, """{"mensaje":"No existe la oportunidad 7"}""")
        try {
            OportunidadRepository(FakeOportunidadDao(), api<OportunidadApi>()).guardar(oportunidad(7), 42)
            fail("Debía lanzar IllegalStateException")
        } catch (exception: IllegalStateException) {
            assertEquals("La oportunidad ya no existe", exception.message)
        }
    }

    @Test fun eliminarBorraEnElServidorYEnRoom() = runTest {
        val dao = FakeOportunidadDao().apply { items.value = listOf(oportunidad(7)) }
        responder(204)

        OportunidadRepository(dao, api<OportunidadApi>()).eliminar(7)

        val request = server.takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/api/oportunidades/7", request.path)
        assertTrue(dao.items.value.isEmpty())
    }

    @Test fun elErrorDeCampoDelServidorLlegaComoMensaje() = runTest {
        responder(400, """{"mensaje":"Hay campos con errores","errores":{"eventoId":"El evento seleccionado no existe"}}""")
        try {
            OportunidadRepository(FakeOportunidadDao(), api<OportunidadApi>()).guardar(oportunidad(0), 42)
            fail("Debía lanzar HttpException")
        } catch (exception: HttpException) {
            assertEquals("El evento seleccionado no existe", exception.mensajeParaUsuario("otro"))
        }
    }

    // ---------- Organizaciones ----------

    @Test fun organizacionConContactosNoSeElimina() = runTest {
        val dao = FakeOrganizacionDao().apply { items.value = listOf(organizacion(3)) }
        responder(409, """{"mensaje":"No se puede eliminar"}""")
        try {
            OrganizacionRepository(dao, api<OrganizacionApi>()).eliminar(3)
            fail("Debía lanzar OrganizacionEnUsoException")
        } catch (exception: OrganizacionEnUsoException) {
            // La copia local no se toca.
            assertEquals(1, dao.items.value.size)
        }
    }

    // ---------- Autenticación ----------

    private val usuarioJson = """
        {"id":42,"nombre":"Ana Pérez","email":"ana@ejemplo.cl","role":"VENDEDOR",
         "isActive":true,"createAt":1,"updateAt":2}
    """.trimIndent()

    @Test fun loginGuardaElUsuarioConElIdDelServidor() = runTest {
        val usuarios = FakeUsuarioDao()
        responder(200, usuarioJson)

        val usuario = AuthRepository(usuarios, api<AuthApi>()).login("  ANA@ejemplo.cl ", "clave1234")

        val request = server.takeRequest()
        assertEquals("/api/auth/login", request.path)
        assertTrue(request.body.readUtf8().contains("\"email\":\"ana@ejemplo.cl\""))
        assertEquals(42L, usuario?.id)
        assertEquals(Role.VENDEDOR, usuario?.role)
        assertEquals(listOf(42L), usuarios.guardados.map { it.id })
    }

    @Test fun loginConClaveIncorrectaDevuelveNull() = runTest {
        responder(401, """{"mensaje":"Correo o contraseña incorrectos"}""")
        assertNull(AuthRepository(FakeUsuarioDao(), api<AuthApi>()).login("ana@ejemplo.cl", "mala"))
    }

    @Test fun registroConCorreoRepetidoLanzaSuExcepcion() = runTest {
        responder(409, """{"mensaje":"Ya existe una cuenta con ese correo"}""")
        try {
            AuthRepository(FakeUsuarioDao(), api<AuthApi>()).register("Ana", "ana@ejemplo.cl", "clave1234")
            fail("Debía lanzar EmailAlreadyRegisteredException")
        } catch (exception: EmailAlreadyRegisteredException) {
            // Correcto.
        }
    }

    // ---------- Sincronización ----------

    @Test fun sincronizarDescargaTodoYLoEntregaJuntoARoom() = runTest {
        responder(200, "[$usuarioJson]")
        responder(200, """[{"id":3,"nombre":"Municipalidad Ficticia","tipo":"Municipalidad","rubro":"Servicios",
            "comuna":"Puente Alto","region":"Metropolitana","sitioWeb":"","observaciones":""}]""")
        responder(200, """[{"id":5,"nombre":"Laura Soto","organizacionId":3,"organizacionNombre":"Municipalidad Ficticia",
            "cargo":"Encargada","area":"Bienestar","correo":"","telefono":"","medioPreferente":"Correo","estado":"Contactado"}]""")
        responder(200, """[{"id":9,"nombre":"Feria","tipo":"Feria empresarial","fechaInicio":$FECHA_EVENTO,
            "horaInicio":540,"horaTermino":1080,"comuna":"Puente Alto","region":"Metropolitana",
            "organizacionId":3,"publicoEsperado":"Trabajadores"}]""")
        responder(200, "[$oportunidadJson]")
        responder(200, """[{"id":11,"oportunidadId":7,"usuarioId":42,"tipoAccion":"Cambio de estado",
            "fechaHora":$FECHA_EVENTO,"resultado":"Contacto realizado → Propuesta enviada"}]""")
        val dao = FakeSincronizacionDao()

        SincronizacionRepository(
            dao, api<UsuarioApi>(), api<OrganizacionApi>(), api<ContactoApi>(),
            api<EventoApi>(), api<OportunidadApi>(), api<SeguimientoApi>()
        ).sincronizar()

        val rutas = List(6) { server.takeRequest().path }
        assertEquals(
            listOf("/api/usuarios", "/api/organizaciones", "/api/contactos", "/api/eventos",
                "/api/oportunidades", "/api/seguimientos"),
            rutas
        )
        val datos = dao.recibido!!
        assertEquals(listOf(42L), datos.usuarios.map { it.id })
        assertEquals(3L, datos.contactos.single().organizacionId)
        assertEquals(540, datos.eventos.single().horaInicio)
        assertEquals(9L, datos.oportunidades.single().eventoId)
        assertEquals("Contacto realizado → Propuesta enviada", datos.seguimientos.single().resultado)
        assertEquals("", datos.seguimientos.single().proximaAccion)
    }

    @Test fun siFallaUnaDescargaNoSeTocaRoom() = runTest {
        responder(200, "[$usuarioJson]")
        responder(500)
        val dao = FakeSincronizacionDao()
        try {
            SincronizacionRepository(
                dao, api<UsuarioApi>(), api<OrganizacionApi>(), api<ContactoApi>(),
                api<EventoApi>(), api<OportunidadApi>(), api<SeguimientoApi>()
            ).sincronizar()
            fail("Debía lanzar HttpException")
        } catch (exception: HttpException) {
            assertNull(dao.recibido)
        }
    }

    // ---------- Fakes ----------

    /** Solo registra lo que recibió reemplazarTodo (la lógica SQL se prueba en Room). */
    private class FakeSincronizacionDao : SincronizacionDao() {
        var recibido: DatosServidor? = null
        override suspend fun reemplazarTodo(datos: DatosServidor) { recibido = datos }
        override suspend fun upsertUsuarios(items: List<UsuarioEntity>) = Unit
        override suspend fun upsertOrganizaciones(items: List<com.example.opportunitymanagement.data.local.OrganizacionEntity>) = Unit
        override suspend fun upsertContactos(items: List<com.example.opportunitymanagement.data.local.ContactoEntity>) = Unit
        override suspend fun upsertEventos(items: List<com.example.opportunitymanagement.data.local.EventoEntity>) = Unit
        override suspend fun upsertOportunidades(items: List<OportunidadEntity>) = Unit
        override suspend fun upsertSeguimientos(items: List<com.example.opportunitymanagement.data.local.SeguimientoEntity>) = Unit
        override suspend fun eliminarSeguimientosExcepto(ids: List<Long>) = Unit
        override suspend fun eliminarOportunidadesExcepto(ids: List<Long>) = Unit
        override suspend fun eliminarEventosExcepto(ids: List<Long>) = Unit
        override suspend fun eliminarContactosExcepto(ids: List<Long>) = Unit
        override suspend fun eliminarOrganizacionesExcepto(ids: List<Long>) = Unit
        override suspend fun cerrarSesionSiNoExiste(ids: List<Long>) = Unit
    }

    private class FakeUsuarioDao : UsuarioDao {
        val guardados = mutableListOf<UsuarioEntity>()
        override suspend fun upsert(user: UsuarioEntity) { guardados += user }
        override suspend fun inserUser(user: UsuarioEntity): Long = error("no se usa")
        override suspend fun findById(id: Long): UsuarioEntity? = guardados.find { it.id == id }
        override suspend fun findbyEmail(email: String): UsuarioEntity? = guardados.find { it.email == email }
        override fun observeUsers(): Flow<List<UsuarioEntity>> = MutableStateFlow(guardados.toList())
        override suspend fun updateUser(user: UsuarioEntity): Int = error("no se usa")
        override suspend fun setUserActive(id: Long, active: Boolean, updatedAt: Long): Int = error("no se usa")
    }
}
