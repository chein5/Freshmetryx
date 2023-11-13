package com.freshmetryx

import com.google.firebase.Timestamp
import com.google.type.DateTime
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Locale

data class Boleta(
    var fecha_hora: Timestamp,
    var cantidad_productos: Long = 0,
    var total: Long,
) {
    override fun toString(): String {
        val formatoFecha = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale("es", "ES"))
        val fechaFormateada = formatoFecha.format(fecha_hora.toDate())
        return "Fecha: ${fechaFormateada}, cant. Prod.: $cantidad_productos, Total: $$total"
    }
}