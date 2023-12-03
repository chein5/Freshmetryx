package com.freshmetryx

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import com.freshmetryx.databinding.ActivityMenuVentasBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class Menu_Ventas : AppCompatActivity() {
    private lateinit var binding: ActivityMenuVentasBinding
    private lateinit var ib_realizarVenta : ImageButton
    private lateinit var ibtn_historialVentasMenu : ImageButton
    private lateinit var ibtn_volverHomeVentas : ImageButton
    private lateinit var correo : String
    private var docId: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_ventas)

        //binding para trabajar con elementos visuales mas facilmente
        binding = ActivityMenuVentasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //capturar el correo desde el intent anterior
        correo = intent.getStringExtra("correo").toString()

        //precargar datos del negocio
        cargarNegocio()

        ib_realizarVenta= findViewById(R.id.ib_realizarVenta)
        ib_realizarVenta.setOnClickListener {
            val intent = Intent(this, Venta_Carrito ::class.java)
            intent.putExtra("correo", correo)
            startActivity(intent)
        }

        //Se acciona el boton para ir al historial de ventas
        ibtn_historialVentasMenu= findViewById(R.id.ibtn_historialVentasMenu)
        ibtn_historialVentasMenu.setOnClickListener {
            val intent = Intent(this, Venta_Listar ::class.java)
            intent.putExtra("correo", correo)
            startActivity(intent)
        }

        ibtn_volverHomeVentas= findViewById(R.id.ibtn_volverHomeVentas)
        ibtn_volverHomeVentas.setOnClickListener {
            val intent = Intent(this, Home ::class.java)
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
                    binding.txtvNombreNegocioMV.text = document.getString("nombre_negocio")
                    binding.txtvNombreClienteMV.text = document.getString("nombre_cliente")
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al cargar los datos del negocio", Toast.LENGTH_SHORT).show()
            }
    }
}