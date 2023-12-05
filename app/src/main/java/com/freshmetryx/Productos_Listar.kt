package com.freshmetryx

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.bumptech.glide.Glide
import com.freshmetryx.databinding.ActivityProductosListarBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class Productos_Listar : AppCompatActivity() {

    //Declaracion de variables
    private lateinit var binding: ActivityProductosListarBinding
    private lateinit var correo : String
    private lateinit var docId: String

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_productos_listar)

        //Configuracion del view Binding (es una funcion que te permite escribir codigo mas facilmente para interactuar con las vistas)
        binding = ActivityProductosListarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //capturar el correo desde el intent anterior
        correo = intent.getStringExtra("correo").toString()

        //inicializacion de la variable docID
        docId = ""

        //precargar datos del negocio
        cargarNegocio()

        llenarListInventario()
        cargarImagen()
    }

    private fun cargarImagen(){
        val storageReference = Firebase.storage.getReferenceFromUrl("gs://freshmetryx-aa049.appspot.com")
        val imageRef = storageReference.child("/${correo}/photos/logo.jpg")


        imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
            // Use Glide to load the image into the ImageView
            Glide.with(this@Productos_Listar)
                .load(downloadUrl)
                .into(binding.imageView18)
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
                    binding.txtvNombreNegocioPL.text = document.getString("nombre_negocio")
                    binding.txtvNombreClientePL.text = document.getString("nombre_cliente")
                    cargarImagen()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al cargar los datos del negocio", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onResume() {
        super.onResume()
        cargarNegocio()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun llenarListInventario() {
        val listaProductos: MutableList<Producto> = mutableListOf()
        val db = Firebase.firestore
        db.collection("clientes").whereEqualTo("correo", correo).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    Log.e("TAG", "${document.id} => ${document.data}")
                    db.collection("clientes").document(docId).collection("Productos")
                        .get()
                        .addOnSuccessListener { result ->
                            for (document in result) {
                                val nombre = document.get("nombre") as String
                                val stock = document.get("stock") as Long
                                val valor = document.get("valor") as Long

                                // Crea un objeto Clase_ayuda para cada producto y agrégalo a la lista
                                val prod = Producto(nombre, stock, valor)
                                listaProductos.add(prod)
                            }

                            // Configura el adaptador para la ListView
                            val adapter: ArrayAdapter<Producto> = ArrayAdapter(
                                this@Productos_Listar,
                                android.R.layout.simple_list_item_1,
                                listaProductos
                            )

                            // Configura la ListView con el adaptador
                            binding.listInventario.adapter = adapter
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
        // Realizar la consulta en la colección "Productos"


    }
}
