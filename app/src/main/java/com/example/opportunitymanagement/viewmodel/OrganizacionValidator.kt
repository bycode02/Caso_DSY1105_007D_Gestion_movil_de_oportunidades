package com.example.opportunitymanagement.viewmodel

class OrganizacionValidator {

    fun nombreError(nombre: String): String? = when {
        nombre.isBlank() -> "Ingresa el nombre de la organización"
        else -> null
    }

    fun tipoError(tipo: String): String? = when {
        tipo.isBlank() -> "Selecciona un tipo de organización"
        tipo !in Opciones.tiposOrganizacion -> "Selecciona un tipo válido"
        else -> null
    }

    fun rubroError(rubro: String): String? = when {
        rubro.isBlank() -> "Indica el rubro"
        else -> null
    }

    fun comunaError(comuna: String): String? = when {
        comuna.isBlank() -> "Indica la comuna"
        else -> null
    }

    fun regionError(region: String): String? = when {
        region.isBlank() -> "Indica la región"
        else -> null
    }

    // Opcional: solo se valida el formato si se escribió algo.
    fun sitioWebError(sitioWeb: String): String? = when {
        sitioWeb.isBlank() -> null
        !sitioWebPattern.matches(sitioWeb.trim()) -> "Ingresa un sitio web válido, por ejemplo www.ejemplo.cl"
        else -> null
    }

    private companion object {
        // Acepta "ejemplo.cl", "www.ejemplo.cl" o "https://www.ejemplo.cl/ruta".
        val sitioWebPattern = Regex("^(https?://)?[^\\s/.]+(\\.[^\\s/.]+)+(/\\S*)?$")
    }
}
