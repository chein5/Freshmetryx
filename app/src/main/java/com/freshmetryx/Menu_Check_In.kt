package com.freshmetryx

import android.content.Intent
import android.os.Binder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.bumptech.glide.Glide
import com.freshmetryx.databinding.ActivityMenuCheckInBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

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

        cargarImagen()
    }

    private fun cargarImagen(){
        val storageReference = Firebase.storage.getReferenceFromUrl("gs://freshmetryx-aa049.appspot.com")
        val imageRef = storageReference.child("/${correo}/photos/logo.jpg")


        imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
            // Use Glide to load the image into the ImageView
            Glide.with(this@Menu_Check_In)
                .load(downloadUrl)
                .into(binding.imageView8)
        }.addOnFailureListener {
            // Handle any errors
            Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show()
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
                    cargarImagen()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al cargar los datos del negocio", Toast.LENGTH_SHORT).show()
            }
    }
}