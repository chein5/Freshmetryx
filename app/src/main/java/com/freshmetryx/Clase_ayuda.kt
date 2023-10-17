package com.freshmetryx

data class Clase_ayuda (
    var nombre_producto : String= "",
    var cantidad_producto : Long =0,
    var precio_producto : Long = 0
) {
    override fun toString(): String {
        return "$nombre_producto X $cantidad_producto PRECIO: $$precio_producto"
    }
}