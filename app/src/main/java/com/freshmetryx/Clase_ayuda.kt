package com.freshmetryx

/*
Esta clase nos permite crear objetos para mostrar en algunas de las listview de la aplicacion.
 */
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule

@GlideModule
class MyAppGlideModule : AppGlideModule()
data class Clase_ayuda (
    var nombre_producto : String= "",
    var cantidad_producto : Long =0,
    var precio_producto : Long = 0
) {
    override fun toString(): String {
        return "$nombre_producto X $cantidad_producto PRECIO: $$precio_producto"
    }
}