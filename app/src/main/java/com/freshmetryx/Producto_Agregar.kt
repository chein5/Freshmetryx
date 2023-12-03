package com.freshmetryx

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import com.freshmetryx.databinding.ActivityMainBinding
import com.freshmetryx.databinding.ActivityProductoAgregarBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.zxing.integration.android.IntentIntegrator

class Producto_Agregar : AppCompatActivity() {

    //declaracion de variables
    private lateinit var binding: ActivityProductoAgregarBinding
    val db = Firebase.firestore
    private lateinit var correo : String
    private lateinit var docId: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_producto_agregar)
        FirebaseApp.initializeApp(this)

        //capturar el correo desde el intent anterior
        correo = intent.getStringExtra("correo").toString()


        docId = ""

        //precargar datos del negocio
        cargarNegocio()

        //Configuracion del view Binding (es una funcion que te permite escribir codigo mas facilmente para interactuar con las vistas)
        binding = ActivityProductoAgregarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Se activa la funcion de escanear al presionar el boton
        binding.btnEscanearAgregar.setOnClickListener{( initScanner())}

        //Se activa la funcion de agregar datos al presionar el boton
        binding.btnAgregarProducto.setOnClickListener {
            agregarDatos()
        }
    }

    override fun onResume() {
        super.onResume()
        cargarNegocio()
    }
    private fun cargarNegocio(){
        //Mostrar datos del negocio
        val db = Firebase.firestore
        db.collection("clientes").whereEqualTo("correo", correo).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    Log.e("TAG", "${document.id} => ${document.data}")
                    docId = document.id
                    binding.txtvNombreNegocioAP.text = document.getString("nombre_negocio")
                    binding.txtvNombreClienteAP.text = document.getString("nombre_cliente")
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al cargar los datos del negocio", Toast.LENGTH_SHORT).show()
            }
    }

    /*
    Esta funcion inicia la biblioteca del scanner, ademas de configurar el tipo de codigo que se escaneara
     */
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
                binding.txvCodigoProductoAgregar.text = result.contents
                Toast.makeText(this,"el valor escaneado es: "+ result.contents, Toast.LENGTH_LONG ).show()
                if (result != null){
                    mostrarDatos(result.contents.toString())
                }
            }
        }else{
            super.onActivityResult(requestCode, resultCode, data)

        }
    }

    fun agregarDatos() {
        // Obtener los valores de los campos de texto
        val txtNombre_Scan = binding.txtNombreProductoAgregar.text.toString()
        val txtStock_Scan = binding.txtStockProductoAgregar.text.toString()
        val txtValor_Scan = binding.txtValorProductoAgregar.text.toString()

        // Verificar si algún campo está vacío
        if (txtNombre_Scan.isEmpty()) {
            binding.txtNombreProductoAgregar.error = "Campo obligatorio"
            return
        }

        if (txtStock_Scan.isEmpty()) {
            binding.txtStockProductoAgregar.error = "Campo obligatorio"
            return
        }

        if (txtValor_Scan.isEmpty()) {
            binding.txtValorProductoAgregar.error = "Campo obligatorio"
            return
        }

        // Verificar si el producto ya existe en la base de datos
        val codigoProducto = binding.txvCodigoProductoAgregar.text.toString()
        val docRef = db.collection("clientes").document(docId).collection("Productos").document(codigoProducto)
        docRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    // El producto ya existe en la base de datos, mostrar mensaje y no agregar
                    Toast.makeText(this, "El producto ya existe en la base de datos", Toast.LENGTH_SHORT).show()
                } else {
                    // El producto no existe, agregarlo a la colección "Productos"
                    val p = Producto(txtNombre_Scan, txtStock_Scan.toLong(), txtValor_Scan.toLong())
                    db.collection("clientes").whereEqualTo("correo", correo).get()
                        .addOnSuccessListener { documents ->
                            for (document in documents) {
                                Log.e("TAG", "${document.id} => ${document.data}")
                                docId = document.id
                                db.collection("clientes").document(docId).collection("Productos").document(codigoProducto)
                                    .set(p)
                                    .addOnSuccessListener { documentReference ->
                                        Log.d(ContentValues.TAG, "DocumentSnapshot added with ID: $codigoProducto")
                                        mostrarDatos(codigoProducto)
                                        // Limpiar los campos después de agregar el producto correctamente
                                        binding.txtNombreProductoAgregar.text.clear()
                                        binding.txtStockProductoAgregar.text.clear()
                                        binding.txtValorProductoAgregar.text.clear()
                                        binding.txvCodigoProductoAgregar.text = ""

                                        Toast.makeText(
                                            this,
                                            "Se agregó: $p con el código $codigoProducto",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                    .addOnFailureListener { e ->
                                        Log.w(ContentValues.TAG, "Error adding document", e)
                                    }
                            }
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(this, "Error al cargar los datos del negocio", Toast.LENGTH_SHORT).show()
                        }

                }
            }
            .addOnFailureListener { e ->
                Log.e(ContentValues.TAG, "Error al obtener documento: $e")
            }
    }

    fun mostrarDatos(consulta : String){
        val db = Firebase.firestore
        Log.e(ContentValues.TAG, "Numero de consulta: $consulta")
        val docRef= db.collection("clientes").document(docId).collection("Productos").document(consulta)
        docRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    Log.e(ContentValues.TAG, "Encontro el dato")
                    // Obtener los datos del documento
                    val data = documentSnapshot.data
                } else {

                }
            }
            .addOnFailureListener { e ->
                // Manejar errores en la lectura del documento
                Log.e(ContentValues.TAG, "Error al obtener documento: $e")
            }
    }
}