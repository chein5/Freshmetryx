package com.freshmetryx

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.freshmetryx.databinding.ActivityDetalleCarritoBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class Detalle_Carrito : AppCompatActivity() {
    lateinit var txt_totalDetalle : TextView
    lateinit var txt_cantProdDet : TextView
    lateinit var txt_TotSubDet : TextView
    lateinit var btnListo : ImageButton
    lateinit var btn_volverDetalle : ImageButton
    private lateinit var correo : String
    private lateinit var docId: String
    private lateinit var binding: ActivityDetalleCarritoBinding
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_carrito)

        //Configuracion del view Binding (es una funcion que te permite escribir codigo mas facilmente para interactuar con las vistas)
        binding = ActivityDetalleCarritoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //capturar el correo desde el intent anterior
        correo = intent.getStringExtra("correo").toString()

        docId = ""

        //precargar datos del negocio
        cargarNegocio()

        //Accion del boton listo
        btnListo = findViewById(R.id.btnListoCompra);
        btnListo.setOnClickListener {
            val intent = Intent(this, Venta_Resultado ::class.java)
            startActivity(intent)
            this.finish()
        }
        btn_volverDetalle= findViewById(R.id.btn_volverDetalle)
        btn_volverDetalle.setOnClickListener {
            this.finish()
        }

        txt_totalDetalle = findViewById(R.id.txt_totalDetalle)
        txt_cantProdDet = findViewById(R.id.txt_cantProdDet)
        txt_TotSubDet = findViewById(R.id.txt_subTotDet)
        val intent = intent
        val id= intent.getStringExtra("id")
        val db = Firebase.firestore
        Log.e("ID", id.toString())

        if (id != null) {
            db.collection("clientes").whereEqualTo("correo", correo).get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        Log.e("TAG", "${document.id} => ${document.data}")
                        docId = document.id
                        db.collection("clientes").document(docId).collection("Boleta")
                            .document(id)
                            .get()
                            .addOnSuccessListener { document ->
                                if (document != null && document.exists()) {
                                    val productos = document["productos"] as List<Map<String, Any>>
                                    Log.e("PRODUCTOS", "$productos")
                                    // Calcular el total general
                                    var totalGeneral = 0.0
                                    var cant_prod: Long = 0
                                    for (producto in productos) {
                                        val cantidad = producto["cantidad_producto"] as Long
                                        val precio = producto["precio_producto"] as Long
                                        cant_prod=cantidad+cant_prod
                                        val totalProducto = cantidad * precio
                                        totalGeneral += totalProducto
                                    }
                                    txt_TotSubDet.setText("Subtotal: $totalGeneral")
                                    txt_cantProdDet.setText("Cant Prod: $cant_prod")
                                    totalGeneral= totalGeneral*0.19+totalGeneral
                                    txt_totalDetalle.setText("Total: $totalGeneral")
                                    // El total general está en totalGeneral

                                    println("Total general: $totalGeneral")
                                } else {
                                    println("El documento no existe o no contiene la lista de productos.")
                                }
                            }
                            .addOnFailureListener { exception ->
                                println("Error al obtener el documento: $exception")
                            }
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Error al cargar los datos del negocio", Toast.LENGTH_SHORT).show()
                }

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
                    binding.txtvNombreNegocioDC.text = document.getString("nombre_negocio")
                    binding.txtvNombreClienteDC.text = document.getString("nombre_cliente")
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al cargar los datos del negocio", Toast.LENGTH_SHORT).show()
            }
    }
    override fun onBackPressed() {
        val intent = Intent(this, Venta_Carrito::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }
}