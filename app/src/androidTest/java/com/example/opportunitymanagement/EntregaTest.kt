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
import java.io.File
class EntregaTest {
 @get:Rule val compose = createAndroidComposeRule<MainActivity>()
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
  foto("01_acceso")
  click("Iniciar sesión de prueba")
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
