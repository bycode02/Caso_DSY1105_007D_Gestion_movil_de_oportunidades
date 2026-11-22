package com.example.opportunitymanagement.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert

/**
 * Deja la copia local (Room) igual a lo que hay en el servidor.
 * Es una clase abstracta para poder tener el método @Transaction con cuerpo.
 */
@Dao
abstract class SincronizacionDao {

    @Upsert abstract suspend fun upsertUsuarios(items: List<UsuarioEntity>)
    @Upsert abstract suspend fun upsertOrganizaciones(items: List<OrganizacionEntity>)
    @Upsert abstract suspend fun upsertContactos(items: List<ContactoEntity>)
    @Upsert abstract suspend fun upsertEventos(items: List<EventoEntity>)
    @Upsert abstract suspend fun upsertOportunidades(items: List<OportunidadEntity>)
    @Upsert abstract suspend fun upsertSeguimientos(items: List<SeguimientoEntity>)

    @Query("DELETE FROM seguimientos WHERE id NOT IN (:ids)")
    abstract suspend fun eliminarSeguimientosExcepto(ids: List<Long>)

    @Query("DELETE FROM oportunidades WHERE id NOT IN (:ids)")
    abstract suspend fun eliminarOportunidadesExcepto(ids: List<Long>)

    @Query("DELETE FROM eventos WHERE id NOT IN (:ids)")
    abstract suspend fun eliminarEventosExcepto(ids: List<Long>)

    @Query("DELETE FROM contactos WHERE id NOT IN (:ids)")
    abstract suspend fun eliminarContactosExcepto(ids: List<Long>)

    @Query("DELETE FROM organizaciones WHERE id NOT IN (:ids)")
    abstract suspend fun eliminarOrganizacionesExcepto(ids: List<Long>)

    // Si el usuario de la sesión ya no existe en el servidor, la sesión se cierra.
    @Query("DELETE FROM sesion WHERE userId NOT IN (:ids)")
    abstract suspend fun cerrarSesionSiNoExiste(ids: List<Long>)

    /**
     * Todo en una transacción: o queda la copia nueva completa, o la anterior.
     * 1) Se inserta o actualiza lo del servidor, de padres a hijos (por las llaves foráneas).
     *    Es upsert y no "borrar todo e insertar", para no borrar en cascada el historial.
     * 2) Se borra lo que ya no existe en el servidor, de hijos a padres.
     * Los usuarios no se borran: la sesión guardada depende de ellos.
     */
    @Transaction
    open suspend fun reemplazarTodo(datos: DatosServidor) {
        upsertUsuarios(datos.usuarios)
        upsertOrganizaciones(datos.organizaciones)
        upsertContactos(datos.contactos)
        upsertEventos(datos.eventos)
        upsertOportunidades(datos.oportunidades)
        upsertSeguimientos(datos.seguimientos)

        eliminarSeguimientosExcepto(datos.seguimientos.map { it.id })
        eliminarOportunidadesExcepto(datos.oportunidades.map { it.id })
        eliminarEventosExcepto(datos.eventos.map { it.id })
        eliminarContactosExcepto(datos.contactos.map { it.id })
        eliminarOrganizacionesExcepto(datos.organizaciones.map { it.id })
        cerrarSesionSiNoExiste(datos.usuarios.map { it.id })
    }
}
