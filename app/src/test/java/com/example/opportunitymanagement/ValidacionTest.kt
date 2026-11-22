package com.example.opportunitymanagement
import com.example.opportunitymanagement.viewmodel.OportunidadUiState
import com.example.opportunitymanagement.viewmodel.Opciones
import com.example.opportunitymanagement.viewmodel.OportunidadValidator
import org.junit.Assert.*
import org.junit.Test
import com.example.opportunitymanagement.ui.opportunities.*
import com.example.opportunitymanagement.ui.navigation.idDesdeQr
class ValidacionTest {
 private val validator = OportunidadValidator()
 @Test fun obligatorios() { val e = validator.validar(OportunidadUiState()); assertEquals(7, listOfNotNull(e.nombreError, e.tipoError, e.descripcionError, e.fuenteError, e.prioridadError, e.potencialError, e.estadoError, e.eventoError).size) }
 @Test fun espaciosNoSonNombre() { assertTrue(validator.nombreError("   ") != null) }
 @Test fun registroValido() { assertFalse(validator.tieneErrores(validator.validar(OportunidadUiState(nombre="Feria", tipo=Opciones.tipos.first(), descripcion="Prueba", fuente="Referencia", prioridad="Alta", potencial="Alto", eventoId=1)))) }
 @Test fun opcionesInvalidas() { val e=validator.validar(OportunidadUiState(potencial="Infinito", estado="Desconocido")); assertNotNull(e.potencialError); assertNotNull(e.estadoError) }
 @Test fun qrEstricto() { assertEquals(12L, idDesdeQr("OPORTUNIDAD:12")); assertNull(idDesdeQr("https://example.com")); assertNull(idDesdeQr("OPORTUNIDAD:-1")); assertNull(idDesdeQr("OPORTUNIDAD:abc")) }
}
