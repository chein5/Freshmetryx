package com.freshmetryx

import android.content.Intent
import android.os.Binder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.freshmetryx.databinding.ActivityMenuCheckInBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class Menu_Check_In : AppCompatActivity() {

    //declaracion de variables
    private lateinit var binding: ActivityMenuCheckInBinding
    private lateinit var correo : String
    private lateinit var docId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_check_in)

        //inicializacion de binding para trabajar con las vistas directamente
        binding = ActivityMenuCheckInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        correo = ""
        correo = intent.getStringExtra("correo").toString()
        docId = ""

        cargarNegocio()

        //Abrir el historial de pedidos
        binding.IbtnHistoriapedidoMenu.setOnClickListener {
            val intent = Intent(this, Historial_Check_In ::class.java)
            intent.putExtra("correo", correo)
            startActivity(intent)
        }

        //Iniciar el modulo de chech in
        binding.ibtnRealizarCheckinMenu.setOnClickListener {
            val intent = Intent(this, Check_In_One ::class.java)
            intent.putExtra("correo", correo)
            startActivity(intent)
        }

        binding.ibtnVolverMenuCheckMenu.setOnClickListener {
            val intent = Intent(this, Menu_Check_In ::class.java)
            intent.putExtra("correo", correo)
            startActivity(intent)
        }
    }

    private fun cargarNegocio(){
        //Mostrar datos del negocio
        val db = Firebase.firestore
        db.collection("clientes").whereEqualTo("correo", correo).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    Log.e("TAG", "${document.id} => ${document.data}")
                    docId = document.id
                    binding.txtvNombreNegocioMC.text = document.getString("nombre_negocio")
                    binding.txtvNombreClienteMC.text = document.getString("nombre_cliente")
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al cargar los datos del negocio", Toast.LENGTH_SHORT).show()
            }
    }
}