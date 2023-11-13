package com.freshmetryx

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.text.set
import com.freshmetryx.databinding.ActivityProductoAgregarBinding
import com.freshmetryx.databinding.ActivityProductoEditarBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.zxing.integration.android.IntentIntegrator

class Producto_Editar : AppCompatActivity() {

    //Declaracion de variables
    private lateinit var binding: ActivityProductoEditarBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_producto_editar)

        FirebaseApp.initializeApp(this)

        //Configuracion del view Binding (es una funcion que te permite escribir codigo mas facilmente para interactuar directamente con las vistas)
        binding = ActivityProductoEditarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Se activa la funcion de escanear al presionar el boton
        binding.btnEscanearProductoEditar.setOnClickListener{( initScanner())}

        //Se activa la funcion de mostrar datos al presionar el boton
        binding.btnCargarDatosEditar.setOnClickListener {
            mostrarDatos()
        }

        //Se activa la funcion de eliminar datos al presionar el boton
        binding.btnBorrarProductoEditar.setOnClickListener {
            eliminarDato()
        }

        //Se activa la funcion de editar datos al presionar el boton
        binding.btnEditarProductoEditar.setOnClickListener {
            editarDatos()
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
                //Se cambia el codigo del edittext manual por el codigo escaneado
                binding.txtCodigoEditarManual.setText(result.contents)
                Toast.makeText(this,"el valor escaneado es: "+ result.contents, Toast.LENGTH_LONG ).show()
                if (result != null){
                    mostrarDatos()
                }
            }
        }else{
            super.onActivityResult(requestCode, resultCode, data)

        }
    }

    fun mostrarDatos(){
        val db = Firebase.firestore
        val dato= hashMapOf("Codigo" to binding.txtCodigoEditarManual.text.toString())
        db.collection("Productos").document(binding.txtCodigoEditarManual.text.toString()).get().addOnSuccessListener {
            binding.txtNombreEditar.setText(it.get("nombre").toString())
            binding.txtValorEditar.setText(it.get("valor").toString())
            binding.txtStockEditar.setText(it.get("stock").toString())
        }.addOnFailureListener(){
            Toast.makeText(this,"No se encontro el producto: "+ binding.txtCodigoEditarManual.text.toString(), Toast.LENGTH_LONG ).show()
        }
    }

    fun editarDatos(){
        val db = Firebase.firestore
        val nuevosDatos = hashMapOf(
            "nombre" to binding.txtNombreEditar.text.toString(),
            "stock" to binding.txtStockEditar.text.toString(),
            "valor" to binding.txtValorEditar.text.toString()
        )
        db.collection("Productos").document(binding.txtCodigoEditarManual.text.toString()).update(
            nuevosDatos as Map<String, Any>
        ).addOnSuccessListener {
            Toast.makeText(this,"Se edito el producto con el codigo: "+ binding.txtCodigoEditarManual.text.toString(), Toast.LENGTH_LONG ).show()
        }.addOnFailureListener(){
            Toast.makeText(this,"No se encontro el producto: "+ binding.txtCodigoEditarManual.text.toString(), Toast.LENGTH_LONG ).show()
        }

    }
    fun eliminarDato(){
        val db = Firebase.firestore
        val dato= hashMapOf("Codigo" to binding.txtCodigoEditarManual.text.toString())
        db.collection("Productos").document(binding.txtCodigoEditarManual.text.toString()).delete().addOnSuccessListener {
            Toast.makeText(this,"Se elimino el producto con el codigo: "+ binding.txtCodigoEditarManual.text.toString(), Toast.LENGTH_LONG ).show()
        }.addOnFailureListener(){
            Toast.makeText(this,"No se encontro el producto: "+ binding.txtCodigoEditarManual.text.toString(), Toast.LENGTH_LONG ).show()
        }
    }

}