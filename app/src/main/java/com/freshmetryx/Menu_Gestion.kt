package com.freshmetryx

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.bumptech.glide.Glide
import com.freshmetryx.databinding.ActivityGestionNegocioBinding
import com.freshmetryx.databinding.ActivityMenuGestionBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class Menu_Gestion : AppCompatActivity() {

    //declaracion de variables
    private lateinit var binding: ActivityMenuGestionBinding
    private lateinit var correo: String
    private val db = Firebase.firestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_gestion)


        //obtener correo de la cuenta
        correo = intent.getStringExtra("correo").toString()

        //binding para trabajar con elementos visuales mas facilmente
        binding = ActivityMenuGestionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Mostrar datos del negocio
        db.collection("clientes").whereEqualTo("correo", correo).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    binding.txtvNombreNegocioCuenta.text = document.getString("nombre_negocio")
                    binding.txtvNombreUsuarioCuenta.text = document.getString("nombre_cliente")
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al cargar los datos del negocio", Toast.LENGTH_SHORT).show()
            }

        //iniciar el menu de gestion del negocio
        binding.ibtnGestionNegocioPanel.setOnClickListener {
            val intent = Intent(this, Gestion_Negocio::class.java)
            intent.putExtra("correo", correo)
            startActivity(intent)
        }


        //iniciar el menu de gestion de membresia
        binding.ibtnGestionMembresiaPanel.setOnClickListener {
            val intent = Intent(this, Gestion_Membresia::class.java)
            intent.putExtra("correo", correo)
            startActivity(intent)
        }


        cargarImagen()
    }

    /*
    Cuando se vuelva al activity se recargaran los datos del negocio si por si es que fueron editados.

     */
    override fun onResume() {
        super.onResume()
        cargarNegocio()
        cargarImagen()
    }

    //Mostrar datos del negocio
    private fun cargarNegocio(){
        val db = Firebase.firestore
        db.collection("clientes").whereEqualTo("correo", correo).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    binding.txtvNombreNegocioCuenta.text = document.getString("nombre_negocio")
                    binding.txtvNombreUsuarioCuenta.text = document.getString("nombre_cliente")
                    cargarImagen()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al cargar los datos del negocio", Toast.LENGTH_SHORT).show()
            }
    }

    private fun cargarImagen(){
        val storageReference = Firebase.storage.getReferenceFromUrl("gs://freshmetryx-aa049.appspot.com")
        val imageRef = storageReference.child("/${correo}/photos/logo.jpg")


        imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
            // Use Glide to load the image into the ImageView
            Glide.with(this@Menu_Gestion)
                .load(downloadUrl)
                .into(binding.imageView31)
        }.addOnFailureListener {
            // Handle any errors
            Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show()
        }
    }
}