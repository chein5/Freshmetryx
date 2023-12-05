package com.freshmetryx

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.freshmetryx.databinding.ActivityCheckInThreeBinding
import com.freshmetryx.databinding.ActivityCheckInTwoBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class Check_In_Three : AppCompatActivity() {
    private lateinit var correo :String
    private lateinit var idCheckin :String
    private lateinit var binding: ActivityCheckInThreeBinding
    private lateinit var docId: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_in_three)

        binding = ActivityCheckInThreeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //capturar el correo y el id desde el intent anterior
        correo = ""
        correo = intent.getStringExtra("correo").toString()
        idCheckin = ""
        idCheckin = intent.getStringExtra("idCheck").toString()

        //mostrar datos del negocio
        cargarNegocio()

        binding.ibtnGuardarCheck3.setOnClickListener {
            guardarDatos()
        }

    }

    //Mostrar datos del negocio
    private fun cargarNegocio(){
        val db = Firebase.firestore
        db.collection("clientes").whereEqualTo("correo", correo).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    docId = document.id
                    binding.txtvNombreNegocioC3.text = document.getString("nombre_negocio")
                    binding.txtvNombreClienteC3.text = document.getString("nombre_cliente")
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al cargar los datos del negocio", Toast.LENGTH_SHORT).show()
            }
    }

    private fun guardarDatos() {
        if (binding.txtNombreRepartidor.text.isNullOrEmpty() ||
            binding.txtFirma.text.isNullOrEmpty() ||
            binding.txtPedido.text.isNullOrEmpty()) {
            Toast.makeText(this, "Rellene todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val totalPedido: Long = binding.txtPedido.text.toString().toLong()

        val data: Map<String, Any> = hashMapOf(
            "nombre_repartidor" to binding.txtNombreRepartidor.text.toString(),
            "firma_electronica" to binding.txtFirma.text.toString(),
            "total_pedido" to totalPedido
        )

        val db = Firebase.firestore

        db.collection("clientes").document(docId).collection("checkIn").document(idCheckin)
            .update(data)
            .addOnSuccessListener {
                Toast.makeText(this, "Se completo el check in", Toast.LENGTH_SHORT).show()
                var intent = Intent(this, Home::class.java)
                startActivity(intent)


            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al subir los datos: $e", Toast.LENGTH_SHORT).show()
            }
    }
}