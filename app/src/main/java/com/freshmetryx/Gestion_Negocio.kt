package com.freshmetryx

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.freshmetryx.databinding.ActivityGestionNegocioBinding
import com.freshmetryx.databinding.ActivityMenuGestionBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class Gestion_Negocio : AppCompatActivity() {

    //declaracion de variables
    private lateinit var correo: String
    private val db = Firebase.firestore
    private lateinit var binding: ActivityGestionNegocioBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gestion_negocio)

        //binding para trabajar con elementos visuales mas facilmente
        binding = ActivityGestionNegocioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Mostrar datos del negocio
        db.collection("clientes").whereEqualTo("correo", correo).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    binding.txtvNombreNegocioEdicion.text = document.getString("nombre_negocio")
                    binding.txtvNombreUsuarioEdicion.text = document.getString("nombre_cliente")
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al cargar los datos del negocio", Toast.LENGTH_SHORT).show()
            }
    }
}