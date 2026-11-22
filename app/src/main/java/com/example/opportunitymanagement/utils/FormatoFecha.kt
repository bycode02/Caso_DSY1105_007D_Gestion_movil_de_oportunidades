package com.example.opportunitymanagement.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** Formatos de fecha y hora para mostrar en pantalla. */
object FormatoFecha {

    /**
     * Las fechas de evento se guardan como medianoche UTC (lo que entrega el DatePicker),
     * por eso se formatean en UTC: así no se corren un día según la zona horaria del teléfono.
     */
    fun fecha(millisUtc: Long): String =
        SimpleDateFormat("dd/MM/yyyy", Locale("es", "CL"))
            .apply { timeZone = TimeZone.getTimeZone("UTC") }
            .format(Date(millisUtc))

    /**
     * Medianoche UTC del día de hoy en el teléfono: mismo formato que la fecha de los eventos,
     * para poder compararlas ("próximos 7 días"). Se usa Calendar porque minSdk es 24.
     */
    fun hoyUtc(): Long {
        val local = Calendar.getInstance()
        return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH))
        }.timeInMillis
    }

    /** 570 → "09:30". */
    fun hora(minutos: Int): String = "%02d:%02d".format(minutos / 60, minutos % 60)

    /** "09:00 – 18:00". */
    fun horario(inicio: Int, termino: Int): String = "${hora(inicio)} – ${hora(termino)}"
}
