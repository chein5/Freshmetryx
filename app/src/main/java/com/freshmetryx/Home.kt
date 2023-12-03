package com.freshmetryx

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import java.text.SimpleDateFormat
import android.widget.ImageButton
import android.widget.Toast
import com.freshmetryx.databinding.ActivityHomeBinding
import com.freshmetryx.databinding.ActivityProductoEditarBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Calendar
import java.util.Locale

class Home : AppCompatActivity() {

    //Declaracion de variables
    private lateinit var binding: ActivityHomeBinding
    private lateinit var correo : String
    private lateinit var docId: String
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Freshmetryx);
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        //Botones
        lateinit var btnQr : ImageButton
        lateinit var btn_ventaHome : ImageButton
        lateinit var btn_inventarioHome : ImageButton

        //variables
        val db = Firebase.firestore

        //Configuracion del view Binding (es una funcion que te permite escribir codigo mas facilmente para interactuar directamente con las vistas)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FirebaseApp.initializeApp(this)

        //Recibir correo
        correo = intent.getStringExtra("correo").toString()

        //Cargar datos del negocio
        cargarNegocio()

        //Activar boton de scanner
        btnQr = findViewById(R.id.btnEscaner)
        btnQr.setOnClickListener {
            val intent = Intent(this, MainActivity ::class.java)
            intent.putExtra("correo", correo)
            startActivity(intent)
        }

        //Iniciar una venta
        btn_ventaHome =findViewById(R.id.btn_ventaHome)
        btn_ventaHome.setOnClickListener {
            val intent = Intent(this, Venta_Carrito ::class.java)
            intent.putExtra("correo", correo)
            startActivity(intent)
        }


        //Abrir el menu de inventario
        btn_inventarioHome = findViewById(R.id.btnInventarioHome)
        btn_inventarioHome.setOnClickListener {
            val intent = Intent(this, Menu_Inventario ::class.java)
            intent.putExtra("correo", correo)
            startActivity(intent)
        }

        //Abrir el menu de cuenta
        binding.ibtnCuentaHome.setOnClickListener {
            val intent = Intent(this, Menu_Gestion::class.java)
            intent.putExtra("correo", correo)
            startActivity(intent)
        }
        //Abrir el menu de finanzas
        binding.ibtnFinanzasHome.setOnClickListener {
            val intent = Intent(this, Menu_Ventas::class.java)
            intent.putExtra("correo", correo)
            startActivity(intent)
        }

        //abrir el menu de proveedores
        binding.ibtnProveedoresHome.setOnClickListener {
            val intent = Intent(this, Menu_Check_In::class.java)
            intent.putExtra("correo", correo)
            startActivity(intent)
        }

        val fechaActual = Calendar.getInstance().time

        // Convierte la fecha actual a un Timestamp
        val timestampActual = Timestamp(fechaActual)

        // Formatea la fecha actual en el formato deseado para obtener la fecha del día actual
        val formatoFecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val fechaActualString = formatoFecha.format(fechaActual)

        // Convierte la fecha actual a un Timestamp que representa la medianoche de hoy
        val timestampInicio = Timestamp(formatoFecha.parse(fechaActualString)!!)

        // Convierte la fecha actual a un Timestamp que representa la medianoche del próximo día
        val fechaManana = Calendar.getInstance()
        fechaManana.add(Calendar.DAY_OF_MONTH, 1)
        val timestampFin = Timestamp(formatoFecha.parse(formatoFecha.format(fechaManana.time))!!)

        db.collection("clientes").whereEqualTo("correo", correo).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    Log.e("TAG", "${document.id} => ${document.data}")
                    docId = document.id
                    db.collection("clientes").document(docId).collection("Boleta").whereGreaterThanOrEqualTo("fecha", timestampInicio)
                        .whereLessThan("fecha", timestampFin)
                        .get()
                        .addOnSuccessListener { querySnapshot ->
                            var totalGeneral = 0.0 // Inicializa la variable para el total general

                            for (document in querySnapshot.documents) {
                                if (document != null && document["total"] is Number) {
                                    // Suma el total al total general
                                    totalGeneral += (document["total"] as Number).toDouble()
                                }
                            }

                            // Muestra el total general después de procesar todos los documentos
                            binding.txtIngresosHome.text = totalGeneral.toString()
                        }
                        .addOnFailureListener { e ->

                        }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al cargar los datos del negocio", Toast.LENGTH_SHORT).show()
            }


    }

    override fun onResume() {
        super.onResume()
        //Mostrar datos del negocio
        cargarNegocio()

        //captura la fecha actual
        val fechaActual = Calendar.getInstance().time

        // Convierte la fecha actual a un Timestamp
        val timestampActual = Timestamp(fechaActual)

        // Formatea la fecha actual en el formato deseado para obtener la fecha del día actual
        val formatoFecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val fechaActualString = formatoFecha.format(fechaActual)

        // Convierte la fecha actual a un Timestamp que representa la medianoche de hoy
        val timestampInicio = Timestamp(formatoFecha.parse(fechaActualString)!!)

        // Convierte la fecha actual a un Timestamp que representa la medianoche del próximo día
        val fechaManana = Calendar.getInstance()
        fechaManana.add(Calendar.DAY_OF_MONTH, 1)
        val timestampFin = Timestamp(formatoFecha.parse(formatoFecha.format(fechaManana.time))!!)

        val db = Firebase.firestore

        db.collection("clientes").whereEqualTo("correo", correo).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    Log.e("TAG", "${document.id} => ${document.data}")
                    docId = document.id
                    db.collection("clientes").document(docId).collection("Boleta").whereGreaterThanOrEqualTo("fecha", timestampInicio)
                        .whereLessThan("fecha", timestampFin)
                        .get()
                        .addOnSuccessListener { querySnapshot ->
                            var totalGeneral = 0.0 // Inicializa la variable para el total general

                            for (document in querySnapshot.documents) {
                                if (document != null && document["total"] is Number) {
                                    // Suma el total al total general
                                    totalGeneral += (document["total"] as Number).toDouble()
                                }
                            }

                            // Muestra el total general después de procesar todos los documentos
                            binding.txtIngresosHome.text = totalGeneral.toString()
                        }
                        .addOnFailureListener { e ->

                        }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al cargar los datos del negocio", Toast.LENGTH_SHORT).show()
            }
    }


    //Mostrar datos del negocio
    private fun cargarNegocio(){
        val db = Firebase.firestore
        db.collection("clientes").whereEqualTo("correo", correo).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    binding.txtvNombreNegocioHome.text = document.getString("nombre_negocio")
                    binding.txtvNombreUsuarioHome.text = document.getString("nombre_cliente")
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al cargar los datos del negocio", Toast.LENGTH_SHORT).show()
            }
    }
}