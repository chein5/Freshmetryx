package com.freshmetryx

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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
    private lateinit var correo: String
    private lateinit var docId: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_producto_editar)

        FirebaseApp.initializeApp(this)

        //Configuracion del view Binding (es una funcion que te permite escribir codigo mas facilmente para interactuar directamente con las vistas)
        binding = ActivityProductoEditarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Recibir correo
        correo = intent.getStringExtra("correo").toString()

        docId = ""

        //precargar datos del negocio
        cargarNegocio()

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

    private fun cargarNegocio(){
        //Mostrar datos del negocio
        val db = Firebase.firestore
        db.collection("clientes").whereEqualTo("correo", correo).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    Log.e("TAG", "${document.id} => ${document.data}")
                    docId = document.id
                    binding.txtvNombreNegocioPE.text = document.getString("nombre_negocio")
                    binding.txtvNombreClientePE.text = document.getString("nombre_cliente")
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al cargar los datos del negocio", Toast.LENGTH_SHORT).show()
            }
    }
    fun mostrarDatos() {
        val db = Firebase.firestore
        val codigo = binding.txtCodigoEditarManual.text.toString()

        // Verificar que el campo del código no esté vacío
        if (codigo.isEmpty()) {
            Toast.makeText(this, "Por favor, ingrese el código del producto", Toast.LENGTH_SHORT).show()
            return
        }

        // Verificar si el producto existe en la base de datos
        db.collection("clientes").document(docId).collection("Productos").document(codigo).get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                binding.txtNombreEditar.setText(documentSnapshot.get("nombre").toString())
                binding.txtValorEditar.setText(documentSnapshot.get("valor").toString())
                binding.txtStockEditar.setText(documentSnapshot.get("stock").toString())
            } else {
                Toast.makeText(this, "El producto con el código $codigo no existe", Toast.LENGTH_LONG).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Error al buscar el producto: $codigo", Toast.LENGTH_LONG).show()
        }
    }

    fun editarDatos() {
        // Obtener los valores de los campos de texto
        val nombre = binding.txtNombreEditar.text.toString()
        val stockString = binding.txtStockEditar.text.toString()
        val valorString = binding.txtValorEditar.text.toString()
        val codigo = binding.txtCodigoEditarManual.text.toString()

        // Verificar que todos los campos estén llenos
        if (nombre.isEmpty() || stockString.isEmpty() || valorString.isEmpty()) {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show()
            return
        }


        val stock = stockString.toLong()
        val valor = valorString.toLong()

        val db = Firebase.firestore
        val nuevosDatos = hashMapOf(
            "nombre" to nombre,
            "stock" to stock,
            "valor" to valor
        )

        db.collection("clientes").document(docId).collection("Productos").document(codigo).update(
            nuevosDatos as Map<String, Any>
        ).addOnSuccessListener {
            Toast.makeText(this, "Se editó el producto con el código: $codigo", Toast.LENGTH_LONG).show()
        }.addOnFailureListener {
            Toast.makeText(this, "No se encontró el producto: $codigo", Toast.LENGTH_LONG).show()
        }
    }

    fun eliminarDato() {
        // Obtener el código del campo de texto
        val codigo = binding.txtCodigoEditarManual.text.toString()

        // Verificar que el campo del código no esté vacío
        if (codigo.isEmpty()) {
            Toast.makeText(this, "Por favor, ingrese el código del producto", Toast.LENGTH_SHORT).show()
            return
        }

        val db = Firebase.firestore
        db.collection("clientes").whereEqualTo("correo", correo).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    Log.e("TAG", "${document.id} => ${document.data}")
                    docId = document.id
                    db.collection("clientes").document(docId).collection("Productos").document(codigo).delete().addOnSuccessListener {
                        Toast.makeText(this, "Se eliminó el producto con el código: $codigo", Toast.LENGTH_LONG).show()
                        // Clear all the text fields
                        binding.txtNombreEditar.setText("")
                        binding.txtStockEditar.setText("")
                        binding.txtValorEditar.setText("")
                        binding.txtCodigoEditarManual.setText("")
                    }.addOnFailureListener {
                        Toast.makeText(this, "No se encontró el producto: $codigo", Toast.LENGTH_LONG).show()
                    }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al cargar los datos del negocio", Toast.LENGTH_SHORT).show()
            }

    }


}