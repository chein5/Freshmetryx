package com.freshmetryx

import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.bumptech.glide.Glide
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import com.freshmetryx.databinding.ActivityHistorialCheckInBinding
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage


class Historial_Check_In : AppCompatActivity() {
    private lateinit var correo : String
    private lateinit var docId: String
    private lateinit var binding: ActivityHistorialCheckInBinding
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historial_check_in)

        binding = ActivityHistorialCheckInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //capturar el correo desde el intent anterior
        correo=""
        docId = ""
        correo = intent.getStringExtra("correo").toString()

        cargarNegocio()
        llenarListInventario()
    }


    @GlideModule
    class MyAppGlideModule : AppGlideModule()

    //Mostrar datos del negocio
    private fun cargarNegocio(){
        val db = Firebase.firestore
        db.collection("clientes").whereEqualTo("correo", correo).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    docId = document.id
                    binding.txtvNombreNegocioHC.text = document.getString("nombre_negocio")
                    binding.txtvNombreClienteHC.text = document.getString("nombre_cliente")
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al cargar los datos del negocio", Toast.LENGTH_SHORT).show()
            }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun llenarListInventario() {
        val listacheckin: MutableList<CheckIn> = mutableListOf()
        val db = Firebase.firestore
        db.collection("clientes").whereEqualTo("correo", correo).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    docId = document.id
                    db.collection("clientes").whereEqualTo("correo", correo).get()
                        .addOnSuccessListener { documents ->
                            for (document in documents) {
                                Log.e("TAG", "${document.id} => ${document.data}")
                                db.collection("clientes").document(docId).collection("checkIn")
                                    .get()
                                    .addOnSuccessListener { result ->
                                        for (document in result) {
                                            val proveedor = document.get("proveedor") as String
                                            val fecha_hora = document.get("fecha") as Timestamp
                                            val total = document.get("total_pedido") as Long
                                            val urlFoto = document.get("url_imgBoleta") as String

                                            // Crea un objeto Clase_ayuda para cada producto y agrégalo a la lista
                                            val check = CheckIn(proveedor, fecha_hora, total, urlFoto)
                                            listacheckin.add(check)
                                            binding.listHcheckin.setOnItemClickListener { _, _, position, _ ->
                                                val item = listacheckin[position]

                                                // Get the reference to your image in Firebase Storage
                                                // storageReference = Firebase.storage.getReferenceFromUrl(item.urlFoto)
                                                val storageReference = Firebase.storage.getReferenceFromUrl("gs://freshmetryx-aa049.appspot.com/mati25quezada@gmail.com/images/boleta/JPEG_20231204_234633_8655165103241702494.jpg")
                                                storageReference.downloadUrl.addOnSuccessListener { uri ->
                                                    Glide.with(this).load(uri).into(binding.imageView24)
                                                }.addOnFailureListener {
                                                    // Handle any errors
                                                }
                                                // Get the download URL
                                                /*
                                                storageReference.downloadUrl.addOnSuccessListener { uri ->
                                                    // Use a library like Glide to load the image into an ImageView
                                                    Glide.with(this).load(uri).into(binding.imageView24) // Replace imageView with your ImageView

                                                    // Make the ImageView visible
                                                    binding.imageView24.visibility = View.VISIBLE

                                                    // Hide the ImageView after 3 seconds (3000 milliseconds)
                                                    Handler(Looper.getMainLooper()).postDelayed({
                                                        binding.imageView24.visibility = View.GONE
                                                    }, 3000)
                                                }.addOnFailureListener {
                                                    // Handle any errors
                                                }

                                                 */
                                            }
                                        }

                                        // Configura el adaptador para la ListView
                                        val adapter: ArrayAdapter<CheckIn> = ArrayAdapter(
                                            this@Historial_Check_In,
                                            android.R.layout.simple_list_item_1,
                                            listacheckin
                                        )

                                        // Configura la ListView con el adaptador
                                        binding.listHcheckin.adapter = adapter
                                        adapter.notifyDataSetChanged()
                                    }
                                    .addOnFailureListener { exception ->
                                        Log.e("Error", "Error al obtener documentos.", exception)
                                    }
                            }
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(this, "Error al cargar los datos del negocio", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al cargar los datos del negocio", Toast.LENGTH_SHORT).show()
            }

        // Realizar la consulta en la colección "Productos"

        // Initially hide the ImageView
        binding.imageView24.visibility = View.GONE
    }


}