package com.example.opportunitymanagement

import com.example.opportunitymanagement.viewmodel.ContactoValidator
import com.example.opportunitymanagement.viewmodel.Opciones
import com.example.opportunitymanagement.viewmodel.OrganizacionValidator
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class OrganizacionContactoValidatorTest {
    private val organizacion = OrganizacionValidator()
    private val contacto = ContactoValidator()

    @Test fun organizacionCamposObligatorios() {
        assertNotNull(organizacion.nombreError("   "))
        assertNotNull(organizacion.tipoError(""))
        assertNotNull(organizacion.tipoError("Inventado"))
        assertNull(organizacion.tipoError(Opciones.tiposOrganizacion.first()))
        assertNotNull(organizacion.rubroError(""))
        assertNotNull(organizacion.comunaError(""))
        assertNotNull(organizacion.regionError(""))
    }

    @Test fun sitioWebEsOpcionalPeroConFormato() {
        assertNull(organizacion.sitioWebError(""))
        assertNull(organizacion.sitioWebError("ejemplo.cl"))
        assertNull(organizacion.sitioWebError("www.ejemplo.cl"))
        assertNull(organizacion.sitioWebError("https://www.ejemplo.cl/eventos"))
        assertNotNull(organizacion.sitioWebError("sin punto"))
        assertNotNull(organizacion.sitioWebError("ejemplo"))
    }

    @Test fun contactoCamposObligatorios() {
        assertNotNull(contacto.nombreError(""))
        assertNotNull(contacto.organizacionError(null))
        assertNotNull(contacto.organizacionError(0))
        assertNull(contacto.organizacionError(3))
        assertNotNull(contacto.cargoError(""))
        assertNotNull(contacto.areaError("Ventas"))
        assertNull(contacto.areaError(Opciones.areasContacto.first()))
        assertNotNull(contacto.medioPreferenteError(""))
        assertNull(contacto.medioPreferenteError(Opciones.mediosContacto.first()))
        assertNotNull(contacto.estadoError("Otro"))
        assertNull(contacto.estadoError(Opciones.estadosContacto.first()))
    }

    @Test fun correoYTelefonoSonOpcionalesPeroConFormato() {
        assertNull(contacto.correoError(""))
        assertNull(contacto.correoError("laura.soto@ejemplo.cl"))
        assertNotNull(contacto.correoError("laura@"))
        assertNull(contacto.telefonoError(""))
        assertNull(contacto.telefonoError("+56 9 1234 5678"))
        assertNotNull(contacto.telefonoError("12345"))
        assertNotNull(contacto.telefonoError("9-1234-5678"))
    }
}
