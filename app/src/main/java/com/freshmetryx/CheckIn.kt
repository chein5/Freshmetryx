package com.freshmetryx

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

class CheckIn(
    var proveedor: String,
    var fecha_hora: Timestamp,
    var total: Long,
    var urlFoto: String,
) {

    override fun toString(): String {
        val formatoFecha = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale("es", "ES"))
        val fechaFormateada = formatoFecha.format(fecha_hora.toDate())
        return "Proveedor: ${proveedor}, Fecha: ${fechaFormateada}, Total: $$total"
    }
}