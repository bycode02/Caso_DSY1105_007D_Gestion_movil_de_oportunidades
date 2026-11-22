package com.example.opportunitymanagement.data.repository

import com.example.opportunitymanagement.data.local.SeguimientoConDetalle
import com.example.opportunitymanagement.data.local.SeguimientoDao
import com.example.opportunitymanagement.data.local.SeguimientoEntity
import kotlinx.coroutines.flow.Flow

class SeguimientoRepository(private val dao: SeguimientoDao) {

    /** Historial de la oportunidad, lo más reciente primero. Se actualiza solo. */
    fun historial(oportunidadId: Long): Flow<List<SeguimientoConDetalle>> =
        dao.observarPorOportunidad(oportunidadId)

    suspend fun registrar(seguimiento: SeguimientoEntity): Long = dao.insertar(seguimiento)

    /** Deja constancia automática de un cambio de estado: "Contacto pendiente → Contacto realizado". */
    suspend fun registrarCambioEstado(
        oportunidadId: Long,
        usuarioId: Long?,
        estadoAnterior: String,
        estadoNuevo: String,
        fechaHora: Long
    ): Long = dao.insertar(
        SeguimientoEntity(
            oportunidadId = oportunidadId,
            usuarioId = usuarioId,
            tipoAccion = SeguimientoEntity.TIPO_CAMBIO_ESTADO,
            fechaHora = fechaHora,
            resultado = "$estadoAnterior → $estadoNuevo"
        )
    )
}
