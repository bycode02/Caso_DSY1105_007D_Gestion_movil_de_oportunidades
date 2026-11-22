package com.example.opportunitymanagement.data.local

/** Todo lo que descargó la sincronización, ya convertido a entidades de Room. */
data class DatosServidor(
    val usuarios: List<UsuarioEntity>,
    val organizaciones: List<OrganizacionEntity>,
    val contactos: List<ContactoEntity>,
    val eventos: List<EventoEntity>,
    val oportunidades: List<OportunidadEntity>,
    val seguimientos: List<SeguimientoEntity>
)
