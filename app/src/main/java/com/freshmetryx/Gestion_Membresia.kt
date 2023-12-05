package com.freshmetryx

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.freshmetryx.databinding.ActivityGestionMembresiaBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class Gestion_Membresia : AppCompatActivity() {

    private lateinit var correo : String
    private lateinit var docId: String
    private lateinit var binding: ActivityGestionMembresiaBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gestion_membresia)

        //Configuracion del view Binding (es una funcion que te permite escribir codigo mas facilmente para interactuar con las vistas)
        binding = ActivityGestionMembresiaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //capturar el correo desde el intent anterior
        correo = ""
        correo = intent.getStringExtra("correo").toString()

        docId=""

        cargarNegocio()

    }

    //Mostrar datos del negocio
    private fun cargarNegocio(){
        val db = Firebase.firestore
        db.collection("clientes").whereEqualTo("correo", correo).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    docId = document.id
                    binding.txtvNombreNegocioGM.text = document.getString("nombre_negocio")
                    binding.txtvNombreClietneGM.text = document.getString("nombre_cliente")
                    binding.txtTipoMembresia.setText(document.getString("suscripcion"))
                    binding.txtTipoMembresia.isEnabled = false
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al cargar los datos del negocio", Toast.LENGTH_SHORT).show()
            }
    }
}