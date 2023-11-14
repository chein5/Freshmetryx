package com.freshmetryx

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.freshmetryx.databinding.ActivityMainBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.zxing.integration.android.IntentIntegrator

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    val db = Firebase.firestore
    private lateinit var txtNombre_Scan : TextView
    private lateinit var txtStock_Scan : TextView
    private lateinit var txtValor_Scan : TextView
    private lateinit var txtCodigo_Scan : TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        FirebaseApp.initializeApp(this)
        //Configuracion del view Binding (es una funcion que te permite escribir codigo mas facilmente para interactuar con las vistas)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnActivarQR.setOnClickListener{( initScanner())}


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
                mostrarDatos(result.contents.toString())
                Toast.makeText(this,"el valor escaneado es: "+ result.contents, Toast.LENGTH_LONG ).show()
                if (result != null){
                    mostrarDatos(result.contents.toString())
                }
            }
        }else{
            super.onActivityResult(requestCode, resultCode, data)

        }
    }

    fun mostrarDatos(consulta : String){
        val db = Firebase.firestore
        Log.e(TAG, "Numero de consulta: $consulta")
        txtNombre_Scan = findViewById(R.id.txtNombre_Scan)
        txtStock_Scan = findViewById(R.id.txtStock_Scan)
        txtValor_Scan = findViewById(R.id.txtValor_Scan)
        txtCodigo_Scan= findViewById(R.id.txtCodigo_Scan)
        val docRef= db.collection("Productos").document(consulta)

        docRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    Log.e(TAG, "Encontro el dato")
                    // Obtener los datos del documento
                    val data = documentSnapshot.data

                    // Mostrar los datos en los TextField
                    if (data != null) {
                        Toast.makeText(this,"Datos encontrados", Toast.LENGTH_LONG ).show()
                        // Suponiendo que tienes TextField llamados textfield1, textfield2, etc.
                        txtNombre_Scan.setText("Nombre: "+data["nombre"].toString())
                        txtStock_Scan.setText("Stock: "+data["stock"].toString())
                        txtValor_Scan.setText("Valor: " +data["valor"].toString())
                        txtCodigo_Scan.setText("Codigo: "+ consulta)
                        // Añade más líneas para otros campos según sea necesario
                    }
                } else {
                    Toast.makeText(this,"No se encontro el dato", Toast.LENGTH_LONG ).show()
                }
            }
            .addOnFailureListener { e ->
                // Manejar errores en la lectura del documento
                Log.e(TAG, "Error al obtener documento: $e")
            }


    }

}