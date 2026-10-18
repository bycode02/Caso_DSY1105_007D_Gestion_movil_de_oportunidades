package com.example.opportunitymanagement.data.repository
import com.example.opportunitymanagement.data.local.*

class OportunidadRepository(private val dao: OportunidadDao) {
 val oportunidades = dao.observarTodas()
 suspend fun buscar(id: Long) = dao.buscar(id)
 suspend fun guardar(item: OportunidadEntity): Long {
  if (item.id == 0L) return dao.insertar(item)
  check(dao.actualizar(item) == 1) { "La oportunidad ya no existe" }
  return item.id
 }
}
