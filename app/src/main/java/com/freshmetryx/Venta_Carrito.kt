package com.freshmetryx

import android.content.ContentValues
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.freshmetryx.databinding.ActivityVentaCarritoBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.zxing.integration.android.IntentIntegrator
import java.time.LocalDate
import java.util.Calendar

class Venta_Carrito : AppCompatActivity() {
    val db = Firebase.firestore
    private lateinit var binding: ActivityVentaCarritoBinding
    lateinit var listView_carrito : ListView
    lateinit var lista_productos : ArrayList<Producto>
    lateinit var lista_carrito : ArrayList<Carrito>
    private lateinit var dic_carrito : MutableMap <String,Long>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_venta_carrito)
        FirebaseApp.initializeApp(this)

        binding = ActivityVentaCarritoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnScanqrCarrito.setOnClickListener{( initScanner())}
        lista_productos= ArrayList()
        lista_carrito= ArrayList()
        dic_carrito = mutableMapOf()
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
                        Log.d("CORRECTO","encontrado")
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

    fun obtenerProducto (id: String, datos : List<Producto>){
        val db = FirebaseFirestore.getInstance()
        val docRef= db.collection("Productos").document(id)
        docRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()){
                val nombre = documentSnapshot.getString("Nombre")
                val stock = documentSnapshot.get("Stock")
                val valor = documentSnapshot.get("Valor")
                val producto= Producto(nombre.toString(), stock as Long, valor as Long)
                Log.d("producto","$producto")
                /*
                * Aqui se busca el producto a traves del nombre en el diccionario, si se encuentra se suma 1 a la cantidad, si no lo encuentra lo agrega al diccionario
                 */
                if (dic_carrito.containsKey(nombre.toString())){
                    val valor_actual= dic_carrito[nombre.toString()] ?: 0
                    dic_carrito[nombre.toString()]= valor_actual+1
                }else{
                    dic_carrito.put(nombre.toString(),1)
                }
                /*
                * Aqui se recorre el diccionario para agregar los productos a la lista del carrito
                 */
                for ((nombre, cantidad) in dic_carrito){
                    Log.d("Se agrego","$datos")
                    val carrito = Carrito(nombre, cantidad, valor)
                    lista_carrito.add(carrito)
                }
                Log.d("diccionario","$dic_carrito")
                lista_productos.add(producto)
                llenarList(lista_carrito, nombre.toString())

            }else{
                Toast.makeText(this,"Producto no encontrado", Toast.LENGTH_SHORT ).show()
            }
        }
    }

    fun llenarList(lista_carrito: ArrayList<Carrito>, nombre_producto: String){
        Log.e("LISTA CARRITO","$lista_productos")
        listView_carrito= findViewById(R.id.list_carrito)
        val adapter : ArrayAdapter<Carrito> = ArrayAdapter<Carrito>(
            this@Venta_Carrito,
            android.R.layout.simple_list_item_1,
            lista_carrito as List<Carrito>
        )
        listView_carrito.adapter = adapter
        adapter.notifyDataSetChanged()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun agregarDatos(lista_carrito: ArrayList<Carrito>){

        db.collection("Detalle_carrito").add(lista_carrito).addOnSuccessListener { documentReference ->
            val nuevoId = documentReference.id
            println("Se ha insertado un nuevo documento con el ID: $nuevoId")
            //boleta = Boleta(LocalDate.now(),)
            db.collection("Boleta").document(nuevoId).set(lista_carrito).addOnSuccessListener {
                println("Se agrego la boleta")
            }
        }.addOnFailureListener { e ->
            println("Error al agregar el documento: $e")
        }
    }
}