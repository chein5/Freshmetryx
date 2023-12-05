package com.freshmetryx

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.bumptech.glide.Glide
import com.freshmetryx.databinding.ActivityCheckInThreeBinding
import com.freshmetryx.databinding.ActivityCheckInTwoBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class Check_In_Three : AppCompatActivity() {
    private lateinit var correo :String
    private lateinit var idCheckin :String
    private lateinit var binding: ActivityCheckInThreeBinding
    private lateinit var docId: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_in_three)

        binding = ActivityCheckInThreeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //capturar el correo y el id desde el intent anterior
        correo = ""
        correo = intent.getStringExtra("correo").toString()
        idCheckin = ""
        idCheckin = intent.getStringExtra("idCheck").toString()

        //mostrar datos del negocio
        cargarNegocio()
        cargarImagen()
        binding.ibtnGuardarCheck3.setOnClickListener {
            guardarDatos()
        }

    }

    private fun cargarImagen(){
        val storageReference = Firebase.storage.getReferenceFromUrl("gs://freshmetryx-aa049.appspot.com")
        val imageRef = storageReference.child("/${correo}/photos/logo.jpg")


        imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
            // Use Glide to load the image into the ImageView
            Glide.with(this@Check_In_Three)
                .load(downloadUrl)
                .into(binding.imageView27)
        }.addOnFailureListener {
            // Handle any errors
            Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show()
        }
    }
    //Mostrar datos del negocio
    private fun cargarNegocio(){
        val db = Firebase.firestore
        db.collection("clientes").whereEqualTo("correo", correo).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    docId = document.id
                    binding.txtvNombreNegocioC3.text = document.getString("nombre_negocio")
                    binding.txtvNombreClienteC3.text = document.getString("nombre_cliente")
                    cargarImagen()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al cargar los datos del negocio", Toast.LENGTH_SHORT).show()
            }
    }

    private fun guardarDatos() {
        if (binding.txtNombreRepartidor.text.isNullOrEmpty() ||
            binding.txtFirma.text.isNullOrEmpty() ||
            binding.txtPedido.text.isNullOrEmpty()) {
            Toast.makeText(this, "Rellene todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val totalPedido: Long = binding.txtPedido.text.toString().toLong()

        val data: Map<String, Any> = hashMapOf(
            "nombre_repartidor" to binding.txtNombreRepartidor.text.toString(),
            "firma_electronica" to binding.txtFirma.text.toString(),
            "total_pedido" to totalPedido
        )

        val db = Firebase.firestore

        db.collection("clientes").document(docId).collection("checkIn").document(idCheckin)
            .update(data)
            .addOnSuccessListener {
                Toast.makeText(this, "Se completo el check in", Toast.LENGTH_SHORT).show()
                var intent = Intent(this, Home::class.java)
                intent.putExtra("correo", correo)
                startActivity(intent)


            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al subir los datos: $e", Toast.LENGTH_SHORT).show()
            }
    }
}