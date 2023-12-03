package com.freshmetryx

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.isEmpty
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
    lateinit var lista_ayuda: ArrayList<Clase_ayuda>
    lateinit var lista_productos : ArrayList<Producto>
    lateinit var lista_carrito : ArrayList<Carrito>
    lateinit var dic_carrito : MutableMap <String,Long>
    lateinit var dic_precio : MutableMap <String,Long>
    lateinit var carrito: Carrito
    lateinit var btn_crearVenta : ImageButton
    lateinit var txt_totalVenta : TextView
    private lateinit var correo : String
    private lateinit var docId: String
    @SuppressLint("MissingInflatedId")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_venta_carrito)
        FirebaseApp.initializeApp(this)

        binding = ActivityVentaCarritoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnScanqrCarrito.setOnClickListener{( initScanner())}
        lista_productos= ArrayList()
        lista_carrito= ArrayList()
        lista_ayuda= ArrayList()
        dic_carrito = mutableMapOf()
        dic_precio = mutableMapOf()
        carrito = Carrito()
        btn_crearVenta = findViewById(R.id.btn_confirmarVenta)
        txt_totalVenta = findViewById(R.id.txt_totalVenta)
        correo = ""

        //precargar datos del negocio
        cargarNegocio()

        //capturar el correo desde el intent anterior
        correo = intent.getStringExtra("correo").toString()

        btn_crearVenta.setOnClickListener {
            agregarDatos(lista_ayuda, 0)
        }

        binding.ibtnAnular.setOnClickListener {
            intent = Intent(this, Menu_Ventas::class.java)
            startActivity(intent)
        }
        if (binding.listCarrito.isEmpty()){
            binding.btnConfirmarVenta.isEnabled = false
        }else{
            binding.btnConfirmarVenta.isEnabled = true
        }

    }

    override fun onResume() {
        super.onResume()
        cargarNegocio()
    }
    //funcion que nos permitira abrir el scanner
    private fun initScanner(){
        val integrator= IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
        integrator.initiateScan()
    }

    /*Esta funcion nos permitira capturar el resultado del escaneo para mostrarlo con un toast
  primero verificamos si la variable recibio un resultado, para posteriormente mostrar el resultado entregado */
    @RequiresApi(Build.VERSION_CODES.O)
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

    private fun cargarNegocio(){
        val db = Firebase.firestore
        db.collection("clientes").whereEqualTo("correo", correo).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    Log.e("TAG", "${document.id} => ${document.data}")
                    docId = document.id
                    binding.txtvNombreNegocioVC.text = document.getString("nombre_negocio")
                    binding.txtvNombreClienteVC.text = document.getString("nombre_cliente")
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al cargar los datos del negocio", Toast.LENGTH_SHORT).show()
            }
    }
    fun mostrarDatos(consulta : String){
        val db = Firebase.firestore
        Log.e(ContentValues.TAG, "Numero de consulta: $consulta")
        db.collection("clientes").whereEqualTo("correo", correo).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    Log.e("TAG", "${document.id} => ${document.data}")
                    docId = document.id
                    db.collection("clientes").document(docId).collection("Productos").document(consulta).get()
                        .addOnSuccessListener { documentSnapshot ->
                            if (documentSnapshot.exists()) {
                                Log.e(ContentValues.TAG, "Encontro el dato")
                                // Obtener los datos del documento
                                val data = documentSnapshot.data
                                // Mostrar los datos en los TextField
                                if (data != null) {
                                    Log.d("CORRECTO","encontrado")
                                    Log.d("CORRECTO","$data")
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
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al cargar los datos del negocio", Toast.LENGTH_SHORT).show()
            }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun obtenerProducto(id: String, datos: List<Producto>) {
        val db = FirebaseFirestore.getInstance()
        db.collection("clientes").whereEqualTo("correo", correo).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    Log.e("TAG", "${document.id} => ${document.data}")
                    docId = document.id
                    db.collection("clientes").document(docId).collection("Productos").document(id).get().addOnSuccessListener { documentSnapshot ->
                        if (documentSnapshot.exists()) {
                            val nombre = documentSnapshot.getString("nombre")
                            val stock = documentSnapshot.get("stock")
                            val valor = documentSnapshot.get("valor")
                            val producto = Producto(nombre.toString(), stock as Long, valor as Long)
                            Log.d("producto", "$producto")
                            dic_precio= mutableMapOf(Pair (nombre.toString(),valor))
                            Log.e("PRECIOOOO", "$dic_precio ")
                            // Verificar si el producto ya está en la lista de ayuda
                            val carritoEnLista = lista_ayuda.find { it.nombre_producto == nombre.toString() }

                            // Si ya está en la lista, actualizar solo la cantidad
                            if (carritoEnLista != null) {
                                carritoEnLista.cantidad_producto++
                            } else {
                                // Si no está en la lista, agregarlo
                                val ayuda = Clase_ayuda(nombre.toString(), 1, valor)
                                lista_ayuda.add(ayuda)
                            }

                            llenarList(dic_carrito,dic_precio)

                        } else {
                            Toast.makeText(this, "Producto no encontrado", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al cargar los datos del negocio", Toast.LENGTH_SHORT).show()
            }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun llenarList(dic_carrito: MutableMap<String, Long>, dic_precios: MutableMap<String, Long>){
        /*
        * Aqui se recorre el diccionario para agregar los productos a la lista del carrito
         */
        for ((nombre, cantidad) in dic_carrito){
            Log.d("Se agrego","$nombre")
            val ayuda = Clase_ayuda(nombre, cantidad)
            lista_ayuda.add(ayuda)
        }
        Log.d("diccionario","$dic_carrito")
        Log.e("LISTA CARRITO","$lista_ayuda")
        listView_carrito= findViewById(R.id.list_carrito)
        val adapter : ArrayAdapter<Clase_ayuda> = ArrayAdapter<Clase_ayuda>(
            this@Venta_Carrito,
            android.R.layout.simple_list_item_1,
            lista_ayuda as List<Clase_ayuda>
        )
        listView_carrito.adapter = adapter
        adapter.notifyDataSetChanged()
        var total = 0L
        for (ayuda in lista_ayuda) {
            total += ayuda.cantidad_producto * ayuda.precio_producto
        }
        txt_totalVenta.setText("SubTotal: $total")
        btn_crearVenta.setOnClickListener {
            agregarDatos(lista_ayuda, total)
        }
        binding.btnConfirmarVenta.isEnabled = true
    }

    //Funcion para agregar los datos a Firestore despues de presionar el boton de confirmar venta
    @RequiresApi(Build.VERSION_CODES.O)
    fun agregarDatos(lista_ayuda: ArrayList<Clase_ayuda>, total:Long ) {

        // Crear un mapa que contenga la lista de productos
        val productosMap = lista_ayuda.map {
            mapOf(
                "nombre_producto" to it.nombre_producto,
                "cantidad_producto" to it.cantidad_producto,
                "precio_producto" to it.precio_producto
            )
        }

        //Suma la cantidad de productos dentro del mapa de la lista_ayuda
        val total_cantProd = lista_ayuda.sumOf { it.cantidad_producto }

        // Crear un mapa con los datos para el documento principal
        val datosBoleta = hashMapOf(
            "productos" to productosMap
        )

        /*Agregar los datos al documento en la colección "Boleta"
        Se agregan los productos a un mapa dentro de un documento especifico en Firestore
         */
        db.collection("clientes").whereEqualTo("correo", correo).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    Log.e("TAG", "${document.id} => ${document.data}")
                    docId = document.id
                    db.collection("clientes").document(docId).collection("Boleta").add(datosBoleta)
                        .addOnSuccessListener { documentReference ->
                            val nuevoId = documentReference.id
                            var totalConIVA: Long= (total* 0.19+ total).toLong()
                            var detalleMap = hashMapOf<String, Any>("fecha" to Calendar.getInstance().time,
                                "total" to totalConIVA,
                                "total_cantProd" to total_cantProd,
                            )
                            /*
                             Aqui se agrega la fecha y el total de la venta, asi no se agregan en los mapas individuales
                             de productos.
                             */
                            db.collection("clientes").document(docId).collection("Boleta").document(nuevoId).update(detalleMap)
                                .addOnSuccessListener { documentReference ->
                                    Log.e("Se agrego la fecha", "Se agrego la fecha")
                                }
                            //Aqui se envia el id de la boleta a la siguiente actividad para mostrar los datos
                            val intent2 = Intent(this, Detalle_Carrito::class.java).apply {
                                putExtra("id", nuevoId)
                            }
                            //Se inicia el proximo activity
                            startActivity(intent2)
                            println("Se ha insertado un nuevo documento de Boleta con el ID: ${documentReference.id}")
                        }
                        .addOnFailureListener { e ->
                            println("Error al agregar el documento de Boleta: $e")
                        }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al cargar los datos del negocio", Toast.LENGTH_SHORT).show()
            }

    }

}