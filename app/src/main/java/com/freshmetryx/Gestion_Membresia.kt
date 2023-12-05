package com.freshmetryx

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.bumptech.glide.Glide
import com.freshmetryx.databinding.ActivityGestionMembresiaBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

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
        cargarImagen()

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
            Glide.with(this@Gestion_Membresia)
                .load(downloadUrl)
                .into(binding.imageView37)
        }.addOnFailureListener {
            // Handle any errors
            Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show()
        }
    }
}