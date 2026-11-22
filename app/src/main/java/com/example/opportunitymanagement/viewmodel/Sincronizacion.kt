package com.example.opportunitymanagement.viewmodel

import kotlinx.coroutines.CancellationException

const val AVISO_SIN_CONEXION =
    "Sin conexión con el servidor: se muestran los datos guardados en el teléfono."

/**
 * Sincroniza la copia local con el servidor. Devuelve null si salió bien, o el aviso para
 * mostrar en la lista si falló (la lista sigue mostrando lo que hay en Room).
 */
suspend fun sincronizarOAviso(sincronizar: suspend () -> Unit): String? = try {
    sincronizar()
    null
} catch (exception: CancellationException) {
    throw exception
} catch (exception: Exception) {
    AVISO_SIN_CONEXION
}
