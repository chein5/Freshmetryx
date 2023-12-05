package com.freshmetryx

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.freshmetryx.databinding.ActivityVentaListarBinding
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class Venta_Listar : AppCompatActivity() {
    private lateinit var binding: ActivityVentaListarBinding
    lateinit var list_hventas : ListView
    private lateinit var correo : String
    private lateinit var docId: String
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_venta_listar)

        //Configuracion del view Binding (es una funcion que te permite escribir codigo mas facilmente para interactuar directamente con las vistas)
        binding = ActivityVentaListarBinding.inflate(layoutInflater)
        setContentView(binding.root)



        correo = ""

        //capturar el correo desde el intent anterior
        correo = intent.getStringExtra("correo").toString()

        //precargar datos del negocio
        cargarNegocio()

        //funcion para cargar los datos en la lista de ventas
        llenarList()

    }

    override fun onResume() {
        super.onResume()
        cargarNegocio()
    }

    private fun cargarNegocio(){
        val db = Firebase.firestore
        db.collection("clientes").whereEqualTo("correo", correo).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {

                    docId = document.id
                    binding.txtvNombreNegocioLV.text = document.getString("nombre_negocio")
                    binding.txtvNombreClienteLV.text = document.getString("nombre_cliente")
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al cargar los datos del negocio", Toast.LENGTH_SHORT).show()
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun llenarList() {
        var lista_boletas: ArrayList<Boleta> = ArrayList()
        val db = FirebaseFirestore.getInstance()
        db.collection("clientes").whereEqualTo("correo", correo).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    Log.e("TAG", "${document.id} => ${document.data}")
                    docId = document.id
                    db.collection("clientes").document(docId).collection("Boleta").get().addOnSuccessListener { result ->
                        for (document in result) {
                            var boleta = Boleta(
                                document.get("fecha") as Timestamp,
                                document.get("total_cantProd") as Long,
                                document.get("total") as Long
                            )
                            lista_boletas.add(boleta)
                        }

                        list_hventas = findViewById(R.id.list_hventas)
                        val adapter: ArrayAdapter<Boleta> = ArrayAdapter<Boleta>(
                            this@Venta_Listar,
                            android.R.layout.simple_list_item_1,
                            lista_boletas
                        )
                        list_hventas.adapter = adapter
                        adapter.notifyDataSetChanged()
                    }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al cargar los datos del negocio", Toast.LENGTH_SHORT).show()
            }

    }
}