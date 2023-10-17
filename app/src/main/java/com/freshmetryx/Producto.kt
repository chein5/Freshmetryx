package com.freshmetryx

data class Producto (
    var nombre: String="",
    var stock: Long=0,
    var valor: Long=0
) {
    /*
    override fun toString(): String {
        return "$nombre X $stock PRECIO: $$valor"
    }
    */
}