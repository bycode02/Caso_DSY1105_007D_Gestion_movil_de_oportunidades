package com.example.opportunitymanagement.data.local

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [OportunidadEntity::class,
                      UsuarioEntity::class,
                      CredentialEntity::class,
                      SesionEntity::class,
                      OrganizacionEntity::class,
                      ContactoEntity::class,
                      EventoEntity::class,
                      SeguimientoEntity::class],
    version = 8,
    exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun oportunidades(): OportunidadDao
    abstract fun usuarios(): UsuarioDao
    abstract fun credenciales(): CredentialDao
    abstract fun sesion(): SesionDao
    abstract fun organizaciones(): OrganizacionDao
    abstract fun contacto(): ContactoDao
    abstract fun eventos(): EventoDao
    abstract fun seguimientos(): SeguimientoDao


    companion object {
        /**
         * v4: el responsable deja de ser un texto libre y pasa a ser el usuario (clave foránea).
         * SQLite no permite agregar una clave foránea con ALTER TABLE, así que se crea la tabla
         * nueva, se copian los datos y se reemplaza la anterior. Los registros existentes quedan
         * sin responsable (NULL); se asigna al usuario que los vuelva a guardar.
         */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `oportunidades_nueva` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`nombre` TEXT NOT NULL, `tipo` TEXT NOT NULL, `descripcion` TEXT NOT NULL, " +
                        "`fuente` TEXT NOT NULL, `prioridad` TEXT NOT NULL, `potencial` TEXT NOT NULL, " +
                        "`estado` TEXT NOT NULL, `observaciones` TEXT NOT NULL, `responsableId` INTEGER, " +
                        "`fechaDeteccion` INTEGER NOT NULL, `ultimaActualizacion` INTEGER NOT NULL, " +
                        "`referenciaGeografica` TEXT NOT NULL, " +
                        "FOREIGN KEY(`responsableId`) REFERENCES `usuario`(`id`) " +
                        "ON UPDATE NO ACTION ON DELETE RESTRICT )"
                )
                db.execSQL(
                    "INSERT INTO `oportunidades_nueva` (`id`, `nombre`, `tipo`, `descripcion`, " +
                        "`fuente`, `prioridad`, `potencial`, `estado`, `observaciones`, `responsableId`, " +
                        "`fechaDeteccion`, `ultimaActualizacion`, `referenciaGeografica`) " +
                        "SELECT `id`, `nombre`, `tipo`, `descripcion`, `fuente`, `prioridad`, " +
                        "`potencial`, `estado`, `observaciones`, NULL, `fechaDeteccion`, " +
                        "`ultimaActualizacion`, `referenciaGeografica` FROM `oportunidades`"
                )
                db.execSQL("DROP TABLE `oportunidades`")
                db.execSQL("ALTER TABLE `oportunidades_nueva` RENAME TO `oportunidades`")
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_oportunidades_responsableId` " +
                        "ON `oportunidades` (`responsableId`)"
                )
            }
        }

        /** v5: tabla de sesión de una sola fila para recordar al usuario autenticado. */
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `sesion` (" +
                        "`id` INTEGER NOT NULL, `userId` INTEGER NOT NULL, " +
                        "`iniciadaEn` INTEGER NOT NULL, PRIMARY KEY(`id`), " +
                        "FOREIGN KEY(`userId`) REFERENCES `usuario`(`id`) " +
                        "ON UPDATE NO ACTION ON DELETE CASCADE )"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_sesion_userId` ON `sesion` (`userId`)")
            }
        }

        /**
         * v6: organizaciones y sus contactos. Son tablas nuevas, así que no hay datos que copiar.
         * `organizaciones` se crea primero porque `contactos` la referencia.
         */
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `organizaciones` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`nombre` TEXT NOT NULL, `tipo` TEXT NOT NULL, `rubro` TEXT NOT NULL, " +
                        "`comuna` TEXT NOT NULL, `region` TEXT NOT NULL, " +
                        "`sitioWeb` TEXT NOT NULL, `observaciones` TEXT NOT NULL)"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `contactos` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`nombre` TEXT NOT NULL, `organizacionId` INTEGER NOT NULL, " +
                        "`cargo` TEXT NOT NULL, `area` TEXT NOT NULL, " +
                        "`correo` TEXT NOT NULL, `telefono` TEXT NOT NULL, " +
                        "`medioPreferente` TEXT NOT NULL, `estado` TEXT NOT NULL, " +
                        "`fechaUltimoContacto` INTEGER, `observaciones` TEXT NOT NULL, " +
                        "FOREIGN KEY(`organizacionId`) REFERENCES `organizaciones`(`id`) " +
                        "ON UPDATE NO ACTION ON DELETE RESTRICT )"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_contactos_organizacionId` " +
                        "ON `contactos` (`organizacionId`)"
                )
            }
        }

        /**
         * v7: eventos (de una organización) y asociación opcional de la oportunidad a un evento.
         * 1. Se crea `eventos`, que referencia a `organizaciones`.
         * 2. Se recrea `oportunidades` para agregar `eventoId` con su clave foránea (SQLite no
         *    permite agregarla con ALTER TABLE). Las oportunidades existentes quedan sin evento.
         */
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `eventos` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`nombre` TEXT NOT NULL, `tipo` TEXT NOT NULL, `descripcion` TEXT NOT NULL, " +
                        "`fechaInicio` INTEGER NOT NULL, `horaInicio` INTEGER NOT NULL, " +
                        "`horaTermino` INTEGER NOT NULL, `comuna` TEXT NOT NULL, `region` TEXT NOT NULL, " +
                        "`organizacionId` INTEGER NOT NULL, `publicoEsperado` TEXT NOT NULL, " +
                        "`condiciones` TEXT NOT NULL, " +
                        "FOREIGN KEY(`organizacionId`) REFERENCES `organizaciones`(`id`) " +
                        "ON UPDATE NO ACTION ON DELETE RESTRICT )"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_eventos_organizacionId` " +
                        "ON `eventos` (`organizacionId`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_eventos_fechaInicio` " +
                        "ON `eventos` (`fechaInicio`)"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `oportunidades_nueva` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`nombre` TEXT NOT NULL, `tipo` TEXT NOT NULL, `descripcion` TEXT NOT NULL, " +
                        "`fuente` TEXT NOT NULL, `prioridad` TEXT NOT NULL, `potencial` TEXT NOT NULL, " +
                        "`estado` TEXT NOT NULL, `observaciones` TEXT NOT NULL, `responsableId` INTEGER, " +
                        "`fechaDeteccion` INTEGER NOT NULL, `ultimaActualizacion` INTEGER NOT NULL, " +
                        "`referenciaGeografica` TEXT NOT NULL, `eventoId` INTEGER, " +
                        "FOREIGN KEY(`responsableId`) REFERENCES `usuario`(`id`) " +
                        "ON UPDATE NO ACTION ON DELETE RESTRICT , " +
                        "FOREIGN KEY(`eventoId`) REFERENCES `eventos`(`id`) " +
                        "ON UPDATE NO ACTION ON DELETE RESTRICT )"
                )
                db.execSQL(
                    "INSERT INTO `oportunidades_nueva` (`id`, `nombre`, `tipo`, `descripcion`, " +
                        "`fuente`, `prioridad`, `potencial`, `estado`, `observaciones`, `responsableId`, " +
                        "`fechaDeteccion`, `ultimaActualizacion`, `referenciaGeografica`, `eventoId`) " +
                        "SELECT `id`, `nombre`, `tipo`, `descripcion`, `fuente`, `prioridad`, " +
                        "`potencial`, `estado`, `observaciones`, `responsableId`, `fechaDeteccion`, " +
                        "`ultimaActualizacion`, `referenciaGeografica`, NULL FROM `oportunidades`"
                )
                db.execSQL("DROP TABLE `oportunidades`")
                db.execSQL("ALTER TABLE `oportunidades_nueva` RENAME TO `oportunidades`")
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_oportunidades_responsableId` " +
                        "ON `oportunidades` (`responsableId`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_oportunidades_eventoId` " +
                        "ON `oportunidades` (`eventoId`)"
                )
            }
        }

        /**
         * v8: historial de seguimiento de cada oportunidad. Es una tabla nueva, así que no hay
         * datos que copiar. Se borra junto con su oportunidad (CASCADE); si se borra el usuario
         * o el contacto, la acción queda en el historial sin ellos (SET NULL).
         */
        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `seguimientos` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`oportunidadId` INTEGER NOT NULL, `usuarioId` INTEGER, " +
                        "`tipoAccion` TEXT NOT NULL, `fechaHora` INTEGER NOT NULL, " +
                        "`resultado` TEXT NOT NULL, `contactoId` INTEGER, " +
                        "`proximaAccion` TEXT NOT NULL, `fechaProximoSeguimiento` INTEGER, " +
                        "`observaciones` TEXT NOT NULL, " +
                        "FOREIGN KEY(`oportunidadId`) REFERENCES `oportunidades`(`id`) " +
                        "ON UPDATE NO ACTION ON DELETE CASCADE , " +
                        "FOREIGN KEY(`usuarioId`) REFERENCES `usuario`(`id`) " +
                        "ON UPDATE NO ACTION ON DELETE SET NULL , " +
                        "FOREIGN KEY(`contactoId`) REFERENCES `contactos`(`id`) " +
                        "ON UPDATE NO ACTION ON DELETE SET NULL )"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_seguimientos_oportunidadId` " +
                        "ON `seguimientos` (`oportunidadId`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_seguimientos_usuarioId` " +
                        "ON `seguimientos` (`usuarioId`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_seguimientos_contactoId` " +
                        "ON `seguimientos` (`contactoId`)"
                )
            }
        }

        @Volatile
        private var instancia: AppDatabase? = null
        fun obtener(context: Context): AppDatabase = instancia ?: synchronized(this) {
            instancia ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "oportunidades.db"
            ).addMigrations(MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
                .build().also { instancia = it }
        }
    }
}
