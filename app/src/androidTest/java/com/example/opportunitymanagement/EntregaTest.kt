package com.example.opportunitymanagement
import android.graphics.Bitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.room.Room
import com.example.opportunitymanagement.data.local.*
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import java.io.File
class EntregaTest {
 private val compose = createAndroidComposeRule<MainActivity>()
 // La sesión persiste en Room: se cierra antes de abrir la Activity para partir siempre del login.
 private val sinSesion = object : ExternalResource() {
  override fun before() = runBlocking {
   AppDatabase.obtener(InstrumentationRegistry.getInstrumentation().targetContext).sesion().cerrar()
  }
 }
 @get:Rule val reglas: RuleChain = RuleChain.outerRule(sinSesion).around(compose)
 private fun foto(nombre: String) {
  compose.waitForIdle()
  val instrumentation = InstrumentationRegistry.getInstrumentation()
  val file = File(instrumentation.targetContext.getExternalFilesDir(null), "Evidencia/$nombre.png")
  file.parentFile!!.mkdirs()
  instrumentation.uiAutomation.takeScreenshot().let { bitmap -> file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }; bitmap.recycle() }
 }
 private fun click(text: String) { compose.onNodeWithText(text).performClick() }
 private fun texto(label: String, value: String) { compose.onNodeWithText(label).performScrollTo().performTextReplacement(value) }
 private fun seleccionar(value: String) { compose.onAllNodesWithText("Seleccionar ▾")[0].performScrollTo().performClick(); click(value) }
 @Test fun flujoCompleto() {
  val correo = "vendedor${System.currentTimeMillis()}@ejemplo.cl"
  // Toda oportunidad necesita un evento: se crea uno ficticio con su organización.
  val nombreEvento = "Feria de prueba ${System.currentTimeMillis()}"
  runBlocking {
   val db = AppDatabase.obtener(InstrumentationRegistry.getInstrumentation().targetContext)
   val organizacionId = db.organizaciones().insert(OrganizacionEntity(nombre = "Municipalidad Ficticia", tipo = "Municipalidad", rubro = "Servicios públicos", comuna = "Puente Alto", region = "Metropolitana"))
   db.eventos().insertar(EventoEntity(nombre = nombreEvento, tipo = "Feria institucional", fechaInicio = 1_805_068_800_000, horaInicio = 540, horaTermino = 1080, comuna = "Puente Alto", region = "Metropolitana", organizacionId = organizacionId, publicoEsperado = "Trabajadores y familias"))
  }
  foto("01_acceso")
  click("Crear cuenta")
  texto("Nombre completo", "Vendedor de prueba")
  texto("Correo electrónico", correo)
  texto("Contraseña", "Clave1234!")
  texto("Confirmar contraseña", "Clave1234!")
  compose.onNodeWithText("Confirmar contraseña").performImeAction()
  compose.waitUntil(30000) { compose.onAllNodesWithText("Ir a iniciar sesión").fetchSemanticsNodes().isNotEmpty() }
  click("Ir a iniciar sesión")
  texto("Correo electrónico", correo)
  texto("Contraseña", "Clave1234!")
  compose.onNodeWithText("Contraseña").performImeAction()
  compose.waitUntil(30000) { compose.onAllNodesWithText("Panel comercial").fetchSemanticsNodes().isNotEmpty() }
  foto("02_menu")
  click("Registrar, consultar y actualizar →")
  foto("03_lista_inicial")
  click("+ Nueva oportunidad")
  foto("04_formulario")
  compose.onNodeWithText("Guardar oportunidad").performScrollTo().performClick()
  compose.onNodeWithText("Nombre *").performScrollTo()
  foto("05_validaciones")
  compose.onNodeWithText("Ingresa el nombre de la oportunidad").assertExists()
  texto("Nombre *", "Feria corporativa de prueba")
  seleccionar("Feria empresarial")
  texto("Descripción *", "Encuentro ficticio para presentar beneficios corporativos.")
  texto("Fuente de información *", "Referencia de demostración")
  seleccionar("Alta")
  seleccionar("Alto")
  // El único desplegable que queda sin elegir es el del evento.
  compose.onAllNodesWithText("Seleccionar ▾")[0].performScrollTo().performClick()
  compose.onNodeWithText(nombreEvento, substring = true).performClick()
  texto("Observaciones", "Coordinar presentación de prueba.")
  compose.onNodeWithText("Usar ubicación de prueba").performScrollTo().performClick()
  foto("06_formulario_completo")
  compose.onNodeWithText("Guardar oportunidad").performScrollTo().performClick()
  compose.waitUntil(10000) { compose.onAllNodesWithText("Feria corporativa de prueba").fetchSemanticsNodes().isNotEmpty() }
  foto("07_registro_guardado")
  click("Feria corporativa de prueba")
  compose.waitUntil(10000) { compose.onAllNodesWithText("Feria corporativa de prueba").fetchSemanticsNodes().isNotEmpty() }
  texto("Nombre *", "Feria corporativa actualizada")
  compose.onNodeWithText("Evento identificado ▾").performScrollTo().performClick()
  click("Propuesta enviada")
  foto("08_edicion_estado")
  compose.onNodeWithText("Guardar oportunidad").performScrollTo().performClick()
  compose.waitUntil(10000) { compose.onAllNodesWithText("Feria corporativa actualizada").fetchSemanticsNodes().isNotEmpty() }
  foto("09_lista_actualizada")
  click("Menú")
  click("Consultar oportunidad por QR")
  foto("10_permiso_camara")
 }
 @Test fun roomConservaDatosAlReabrir() = runBlocking {
  val context = InstrumentationRegistry.getInstrumentation().targetContext
  val name = "prueba-persistencia.db"
  context.deleteDatabase(name)
  var db = Room.databaseBuilder(context, AppDatabase::class.java, name).build()
  val id = db.oportunidades().insertar(OportunidadEntity(nombre="Persistente",tipo="Feria empresarial",descripcion="Prueba",fuente="Ficticia",prioridad="Alta",potencial="Alto"))
  db.close()
  db = Room.databaseBuilder(context, AppDatabase::class.java, name).build()
  assertEquals("Persistente", db.oportunidades().buscar(id)?.nombre)
  val item = db.oportunidades().buscar(id)!!
  db.oportunidades().actualizar(item.copy(estado="Propuesta enviada"))
  assertEquals("Propuesta enviada", db.oportunidades().buscar(id)?.estado)
  db.close()
  context.deleteDatabase(name)
  Unit
 }
}
