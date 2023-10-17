package com.freshmetryx

import com.google.type.DateTime
import java.time.LocalDate

data class Boleta(
    var fecha_hora: LocalDate,
    var cantidad_productos: Long = 0,
    var subtotal: Long = 0,
    var iva: Long = 0, var total: Long = 0,
    var ref_detalle: String = ""
) {
    /*
    override fun toString(): String {
        return "Fecha y Hora: $fecha_hora\n" +
                "Cantidad de Productos: $cantidad_productos\n" +
                "Subtotal: $subtotal\n" +
                "IVA: $iva\n" +
                "Total: $total\n" +
                "Referencia Detalle: $ref_detalle"
    }
    */
}