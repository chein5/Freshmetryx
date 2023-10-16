package com.freshmetryx

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton

class Menu_Ventas : AppCompatActivity() {
    lateinit var ib_realizarVenta : ImageButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_ventas)

        ib_realizarVenta= findViewById(R.id.ib_realizarVenta)
        ib_realizarVenta.setOnClickListener {
            val intent = Intent(this, Venta_Carrito ::class.java)
            startActivity(intent)
        }
    }
}