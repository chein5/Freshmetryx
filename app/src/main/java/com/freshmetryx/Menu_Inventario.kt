package com.freshmetryx

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import com.freshmetryx.databinding.ActivityMenuInventarioBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class Menu_Inventario : AppCompatActivity() {
    private lateinit var binding: ActivityMenuInventarioBinding
    private lateinit var docId: String
    private lateinit var correo : String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_inventario)

        //Configuracion del view Binding (es una funcion que te permite escribir codigo mas facilmente para interactuar directamente con las vistas)
        binding = ActivityMenuInventarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Declaracion de variables
        lateinit var btnAgregarProducto : Button
        lateinit var btnEditarInventario : Button
        lateinit var btn_listarProductosMenu : Button

        correo = ""

        correo = intent.getStringExtra("correo").toString()

        //precargar datos del negocio
        cargarNegocio()

        //Iniciar la ventana de agregar productos
        btnAgregarProducto = findViewById(R.id.btn_agregarProductoInventario)
        btnAgregarProducto.setOnClickListener {
            val intent = Intent(this, Producto_Agregar::class.java)
            intent.putExtra("correo", correo)
            startActivity(intent)
        }
        //Iniciar la ventana de editar productos
        btnEditarInventario= findViewById(R.id.btn_editarBorrarMenu)
        btnEditarInventario.setOnClickListener {
            val intent = Intent(this, Producto_Editar::class.java)
            intent.putExtra("correo", correo)
            startActivity(intent)
        }

        //Iniciar la ventana de listar productos
        btn_listarProductosMenu = findViewById(R.id.btn_listarProductosMenu)
        btn_listarProductosMenu.setOnClickListener {
            val intent = Intent(this, Productos_Listar::class.java)
            intent.putExtra("correo", correo)
            startActivity(intent)
        }


    }

    override fun onResume() {
        super.onResume()
        cargarNegocio()
    }

    private fun cargarNegocio(){
        //Mostrar datos del negocio
        val db = Firebase.firestore
        db.collection("clientes").whereEqualTo("correo", correo).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    Log.e("TAG", "${document.id} => ${document.data}")
                    docId = document.id
                    binding.txtvNombreNegocioMI.text = document.getString("nombre_negocio")
                    binding.txtvNombreClienteMI.text = document.getString("nombre_cliente")
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al cargar los datos del negocio", Toast.LENGTH_SHORT).show()
            }
    }
}