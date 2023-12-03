package com.freshmetryx

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.freshmetryx.databinding.ActivityGestionNegocioBinding
import com.freshmetryx.databinding.ActivityMenuGestionBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class Gestion_Negocio : AppCompatActivity() {

    //declaracion de variables
    private lateinit var correo: String
    private val db = Firebase.firestore
    private lateinit var binding: ActivityGestionNegocioBinding
    private var docId: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gestion_negocio)

        //binding para trabajar con elementos visuales mas facilmente
        binding = ActivityGestionNegocioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //capturar el correo desde el intent anterior
        correo = intent.getStringExtra("correo").toString()

        //precargar datos del negocio
        cargarNegocio()

        //Precargar los datos a editar
        cargarDatos()

        //Editar datos boton
        binding.ibtnEditarDatosEditar.setOnClickListener {
            editarDatos()
            cargarNegocio()

        }

        binding.ibtnVolverEditarDatos.setOnClickListener {
            finish()
        }

    }
    override fun onResume() {
        super.onResume()
        cargarDatos()
        cargarNegocio()
    }

    private fun cargarNegocio(){
        //Mostrar datos del negocio
        db.collection("clientes").whereEqualTo("correo", correo).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    docId = document.id
                    binding.txtvNombreNegocioEdicion.text = document.getString("nombre_negocio")
                    binding.txtvNombreUsuarioEdicion.text = document.getString("nombre_cliente")
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al cargar los datos del negocio", Toast.LENGTH_SHORT).show()
            }
    }
    private fun cargarDatos() {
        db.collection("clientes").whereEqualTo("correo", correo).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    docId = document.id
                    binding.txtNombreNegocioEditar.setText(document.getString("nombre_negocio"))
                    binding.txtRubroNegocioEditar.setText(document.getString("rubro"))
                    binding.txtNombreOwnerEditar.setText(document.getString("nombre_cliente"))
                    cargarNegocio()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al cargar los datos del negocio", Toast.LENGTH_SHORT).show()
            }
    }
    private fun editarDatos(){
        val nombreNegocio = binding.txtNombreNegocioEditar.text.toString()
        val rubroNegocio = binding.txtRubroNegocioEditar.text.toString()
        val nombreOwner = binding.txtNombreOwnerEditar.text.toString()

        if (nombreNegocio.isEmpty() || rubroNegocio.isEmpty() || nombreOwner.isEmpty()) {
            Toast.makeText(this, "No se puede editar si alguno de los campos está vacío", Toast.LENGTH_SHORT).show()
        } else {
            db.collection("clientes").document(docId).update(
                "nombre_negocio", nombreNegocio,
                "rubro", rubroNegocio,
                "nombre_cliente", nombreOwner
            )
                .addOnSuccessListener {
                    cargarDatos()
                    cargarNegocio()
                    Toast.makeText(this, "Datos actualizados correctamente", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    cargarDatos()
                    Toast.makeText(this, "Error al actualizar los datos", Toast.LENGTH_SHORT).show()
                }
        }
    }
}