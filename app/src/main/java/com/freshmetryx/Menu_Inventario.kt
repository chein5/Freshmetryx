package com.freshmetryx

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton

class Menu_Inventario : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_inventario)

        //Declaracion de variables
        lateinit var btnAgregarProducto : Button
        lateinit var btnEditarInventario : Button
        lateinit var btn_listarProductosMenu : Button

        //Iniciar la ventana de agregar productos
        btnAgregarProducto = findViewById(R.id.btn_agregarProductoInventario)
        btnAgregarProducto.setOnClickListener {
            val intent = Intent(this, Producto_Agregar::class.java)
            startActivity(intent)
        }
        //Iniciar la ventana de editar productos
        btnEditarInventario= findViewById(R.id.btn_editarBorrarMenu)
        btnEditarInventario.setOnClickListener {
            val intent = Intent(this, Producto_Editar::class.java)
            startActivity(intent)
        }

        //Iniciar la ventana de listar productos
        btn_listarProductosMenu = findViewById(R.id.btn_listarProductosMenu)
        btn_listarProductosMenu.setOnClickListener {
            val intent = Intent(this, Productos_Listar::class.java)
            startActivity(intent)
        }


    }
}