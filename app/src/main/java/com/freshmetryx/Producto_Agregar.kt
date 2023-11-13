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
    private lateinit var binding: ActivityProductoAgregarBinding
    val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_producto_agregar)
        FirebaseApp.initializeApp(this)

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

    fun agregarDatos (){
        val txtNombre_Scan = binding.txtNombreProductoAgregar.text
        val txtStock_Scan = binding.txtStockProductoAgregar.text
        val txtValor_Scan = binding.txtValorProductoAgregar.text
        val p = Producto(txtNombre_Scan.toString(),txtStock_Scan.toString().toLong(),txtValor_Scan.toString().toLong())
        db.collection("Productos").document(binding.txvCodigoProductoAgregar.text.toString()).set(p).addOnSuccessListener { documentReference ->
        Log.d(ContentValues.TAG, "DocumentSnapshot added with ID: ${binding.txvCodigoProductoAgregar.text}")
        mostrarDatos(binding.txvCodigoProductoAgregar.text.toString()) }.addOnFailureListener { e ->
             Log.w(ContentValues.TAG, "Error adding document", e)
         }
        Toast.makeText(this,"Se agrego: "+ p+ " con el codigo "+binding.txvCodigoProductoAgregar.text.toString(), Toast.LENGTH_LONG ).show()
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