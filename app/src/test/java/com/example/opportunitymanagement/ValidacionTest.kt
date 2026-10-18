package com.example.opportunitymanagement
import com.example.opportunitymanagement.viewmodel.OportunidadUiState
import com.example.opportunitymanagement.viewmodel.Opciones
import com.example.opportunitymanagement.viewmodel.validar
import org.junit.Assert.*
import org.junit.Test
import com.example.opportunitymanagement.ui.opportunities.*
import com.example.opportunitymanagement.ui.navigation.idDesdeQr
class ValidacionTest {
 @Test fun obligatorios() { assertEquals(6, validar(OportunidadUiState()).size) }
 @Test fun espaciosNoSonNombre() { assertTrue(validar(OportunidadUiState(nombre = "   ")).containsKey("nombre")) }
 @Test fun registroValido() { assertTrue(validar(OportunidadUiState(nombre="Feria", tipo=Opciones.tipos.first(), descripcion="Prueba", fuente="Referencia", prioridad="Alta", potencial="Alto")).isEmpty()) }
 @Test fun opcionesInvalidas() { val e=validar(OportunidadUiState(potencial="Infinito", estado="Desconocido")); assertTrue(e.containsKey("potencial")); assertTrue(e.containsKey("estado")) }
 @Test fun qrEstricto() { assertEquals(12L, idDesdeQr("OPORTUNIDAD:12")); assertNull(idDesdeQr("https://example.com")); assertNull(idDesdeQr("OPORTUNIDAD:-1")); assertNull(idDesdeQr("OPORTUNIDAD:abc")) }
}
