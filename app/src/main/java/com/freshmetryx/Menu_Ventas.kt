package com.freshmetryx

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton

class Menu_Ventas : AppCompatActivity() {
    lateinit var ib_realizarVenta : ImageButton
    lateinit var ibtn_historialVentasMenu : ImageButton
    lateinit var ibtn_volverHomeVentas : ImageButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_ventas)

        ib_realizarVenta= findViewById(R.id.ib_realizarVenta)
        ib_realizarVenta.setOnClickListener {
            val intent = Intent(this, Venta_Carrito ::class.java)
            startActivity(intent)
        }

        //Se acciona el boton para ir al historial de ventas
        ibtn_historialVentasMenu= findViewById(R.id.ibtn_historialVentasMenu)
        ibtn_historialVentasMenu.setOnClickListener {
            val intent = Intent(this, Venta_Listar ::class.java)
            startActivity(intent)
        }

        ibtn_volverHomeVentas= findViewById(R.id.ibtn_volverHomeVentas)
        ibtn_volverHomeVentas.setOnClickListener {
            val intent = Intent(this, Home ::class.java)
            startActivity(intent)
        }

    }
}