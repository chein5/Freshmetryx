package com.freshmetryx

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.freshmetryx.databinding.ActivityVentaCarritoBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.zxing.integration.android.IntentIntegrator

class Venta_Carrito : AppCompatActivity() {
    val db = Firebase.firestore
    private lateinit var binding: ActivityVentaCarritoBinding
    data class ModeloDatos(val nombre: String?, val stock: Any?, val valor: Any?)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_venta_carrito)
        FirebaseApp.initializeApp(this)

        binding = ActivityVentaCarritoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnScanqrCarrito.setOnClickListener{( initScanner())}

    }


    //funcion que nos permitira abrir el scanner
    private fun initScanner(){
        val integrator= IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
        integrator.initiateScan()
    }

    /*Esta funcion nos permitira capturar el resultado del escaneo para mostrarlo con un toast
  primero verificamos si la variable recibio un resultado, para posteriormente mostrar el resultado entregado */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null){
            if(result.contents==null){
                Toast.makeText(this, "cancelado", Toast.LENGTH_LONG).show()
            }else{
                val db = Firebase.firestore
                val dato= hashMapOf("Codigo" to result.contents)
                if (result != null){
                    mostrarDatos(result.contents.toString())
                    obtenerProducto(result.contents.toString(), emptyList())
                }
            }
        }else{
            super.onActivityResult(requestCode, resultCode, data)

        }
    }

    fun mostrarDatos(consulta : String){
        val db = Firebase.firestore
        Log.e(ContentValues.TAG, "Numero de consulta: $consulta")

        val docRef= db.collection("Productos").document(consulta)

        docRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    Log.e(ContentValues.TAG, "Encontro el dato")
                    // Obtener los datos del documento
                    val data = documentSnapshot.data
                    // Mostrar los datos en los TextField
                    if (data != null) {
                        Toast.makeText(this,"Datos encontrados", Toast.LENGTH_LONG ).show()
                        // Añade más líneas para otros campos según sea necesario
                    }
                } else {
                    Toast.makeText(this,"No se encontro el dato", Toast.LENGTH_LONG ).show()
                }
            }
            .addOnFailureListener { e ->
                // Manejar errores en la lectura del documento
                Log.e(ContentValues.TAG, "Error al obtener documento: $e")
            }


    }

    fun obtenerProducto (id: String, datos : List<ModeloDatos>){
        val listaDatos = mutableListOf<ModeloDatos>()
        val db = FirebaseFirestore.getInstance()
        val docRef= db.collection("Productos").document(id)
        docRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()){
                val nombre = documentSnapshot.getString("Nombre")
                Toast.makeText(this,nombre, Toast.LENGTH_SHORT ).show()
                val stock = documentSnapshot.get("Stock")
                Toast.makeText(this,stock.toString(), Toast.LENGTH_SHORT ).show()
                val valor = documentSnapshot.get("Valor")
                Toast.makeText(this,valor.toString(), Toast.LENGTH_SHORT ).show()

                val modelo= ModeloDatos(nombre,stock,valor)
                listaDatos.add(modelo)


            }
        }
    }


}