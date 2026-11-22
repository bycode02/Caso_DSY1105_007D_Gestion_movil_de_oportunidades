package com.example.opportunitymanagement.data.repository

import com.example.opportunitymanagement.data.local.DatosServidor
import com.example.opportunitymanagement.data.local.SincronizacionDao
import com.example.opportunitymanagement.data.remote.ContactoApi
import com.example.opportunitymanagement.data.remote.EventoApi
import com.example.opportunitymanagement.data.remote.OportunidadApi
import com.example.opportunitymanagement.data.remote.OrganizacionApi
import com.example.opportunitymanagement.data.remote.SeguimientoApi
import com.example.opportunitymanagement.data.remote.UsuarioApi
import com.example.opportunitymanagement.data.remote.toEntity

/**
 * Trae del servidor todos los datos y deja Room igual. Las pantallas siguen leyendo Room,
 * así que se actualizan solas; si no hay conexión, muestran lo último que se descargó.
 */
class SincronizacionRepository(
    private val dao: SincronizacionDao,
    private val usuarioApi: UsuarioApi,
    private val organizacionApi: OrganizacionApi,
    private val contactoApi: ContactoApi,
    private val eventoApi: EventoApi,
    private val oportunidadApi: OportunidadApi,
    private val seguimientoApi: SeguimientoApi
) {

    /** Lanza IOException/HttpException si falla; en ese caso Room queda como estaba. */
    suspend fun sincronizar() {
        // Primero se descarga todo; solo si todo llegó bien se toca la copia local.
        val datos = DatosServidor(
            usuarios = usuarioApi.listar().map { it.toEntity() },
            organizaciones = organizacionApi.listar().map { it.toEntity() },
            contactos = contactoApi.listar().map { it.toEntity() },
            eventos = eventoApi.listar().map { it.toEntity() },
            oportunidades = oportunidadApi.listar().map { it.toEntity() },
            seguimientos = seguimientoApi.todos().map { it.toEntity() }
        )
        dao.reemplazarTodo(datos)
    }
}
