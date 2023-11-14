package com.freshmetryx

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import com.freshmetryx.databinding.ActivityProductosListarBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class Productos_Listar : AppCompatActivity() {
    private lateinit var binding: ActivityProductosListarBinding
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_productos_listar)
        binding = ActivityProductosListarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        llenarListInventario()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun llenarListInventario() {
        val listaProductos: MutableList<Producto> = mutableListOf()

        // Realizar la consulta en la colección "Productos"
        val db = Firebase.firestore
        db.collection("Productos")
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
