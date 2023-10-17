package com.freshmetryx

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class Detalle_Carrito : AppCompatActivity() {
    lateinit var txt_totalDetalle : TextView
    lateinit var txt_cantProdDet : TextView
    lateinit var txt_TotSubDet : TextView
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_carrito)


        txt_totalDetalle = findViewById(R.id.txt_totalDetalle)
        txt_cantProdDet = findViewById(R.id.txt_cantProdDet)
        txt_TotSubDet = findViewById(R.id.txt_subTotDet)
        val intent = intent
        val id= intent.getStringExtra("id")
        val db = Firebase.firestore
        Log.e("ID", id.toString())

        if (id != null) {
            db.collection("Boleta")
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
                                cant_prod=cantidad
                            val totalProducto = cantidad * precio
                            totalGeneral += totalProducto
                        }
                        txt_TotSubDet.setText("Subtotal: $totalGeneral")
                        txt_cantProdDet.setText("C. de prod: $cant_prod")
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
}