package com.example.opportunitymanagement.di

import android.content.Context
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.opportunitymanagement.OpportunityApplication
import com.example.opportunitymanagement.data.local.AppDatabase
import com.example.opportunitymanagement.data.repository.AuthRepository
import com.example.opportunitymanagement.data.repository.ContactoRepository
import com.example.opportunitymanagement.data.repository.EventoRepository
import com.example.opportunitymanagement.data.repository.OportunidadRepository
import com.example.opportunitymanagement.data.repository.OrganizacionRepository
import com.example.opportunitymanagement.data.repository.SeguimientoRepository
import com.example.opportunitymanagement.data.repository.SessionRepository
import com.example.opportunitymanagement.data.repository.UsuarioRepository
import com.example.opportunitymanagement.domain.usecase.CerrarSesionUseCase
import com.example.opportunitymanagement.domain.usecase.LoginUseCase
import com.example.opportunitymanagement.domain.usecase.ObservarSesionUseCase

/** Dependencias compartidas de la app (inyección manual). */
interface AppContainer {
    val oportunidadRepository: OportunidadRepository
    val authRepository: AuthRepository
    val sessionRepository: SessionRepository
    val loginUseCase: LoginUseCase
    val observarSesionUseCase: ObservarSesionUseCase
    val cerrarSesionUseCase: CerrarSesionUseCase
    val usuarioRepository: UsuarioRepository
    val organizacionRepository: OrganizacionRepository
    val contactoRepository: ContactoRepository
    val eventoRepository: EventoRepository
    val seguimientoRepository: SeguimientoRepository
}

/** Implementación de producción: todo se crea perezosamente y una sola vez. */
class DefaultAppContainer(context: Context) : AppContainer {
    private val appContext = context.applicationContext
    private val database: AppDatabase by lazy { AppDatabase.obtener(appContext) }


    override val oportunidadRepository by lazy { OportunidadRepository(database.oportunidades()) }
    override val authRepository by lazy { AuthRepository(database) }
    override val sessionRepository by lazy { SessionRepository(database.sesion()) }
    override val loginUseCase by lazy { LoginUseCase(authRepository, sessionRepository) }
    override val observarSesionUseCase by lazy { ObservarSesionUseCase(sessionRepository) }
    override val cerrarSesionUseCase by lazy { CerrarSesionUseCase(sessionRepository) }
    override val  usuarioRepository by  lazy { UsuarioRepository(database.usuarios()) }
    override val organizacionRepository by lazy { OrganizacionRepository(database.organizaciones()) }
    override val contactoRepository by lazy { ContactoRepository(database.contacto()) }
    override val eventoRepository by lazy { EventoRepository(database.eventos()) }
    override val seguimientoRepository by lazy { SeguimientoRepository(database.seguimientos()) }
}

/** Acceso al contenedor desde los `initializer { }` de las factories de ViewModel. */
fun CreationExtras.appContainer(): AppContainer =
    (checkNotNull(this[APPLICATION_KEY]) as OpportunityApplication).container
