package com.freshmetryx

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import java.text.SimpleDateFormat
import android.widget.ImageButton
import android.widget.Toast
import com.freshmetryx.databinding.ActivityHomeBinding
import com.freshmetryx.databinding.ActivityProductoEditarBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Calendar
import java.util.Locale

class Home : AppCompatActivity() {

    //Declaracion de variables
    private lateinit var binding: ActivityHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Freshmetryx);
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        //Botones
        lateinit var btnQr : ImageButton
        lateinit var btn_ventaHome : ImageButton
        lateinit var btn_inventarioHome : ImageButton

        //Configuracion del view Binding (es una funcion que te permite escribir codigo mas facilmente para interactuar directamente con las vistas)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FirebaseApp.initializeApp(this)
        //Activar boton de scanner
        btnQr = findViewById(R.id.btnEscaner)
        btnQr.setOnClickListener {
            val intent = Intent(this, MainActivity ::class.java)
            startActivity(intent)
        }

        //Iniciar una venta
        btn_ventaHome =findViewById(R.id.btn_ventaHome)
        btn_ventaHome.setOnClickListener {
            val intent = Intent(this, Menu_Ventas ::class.java)
            startActivity(intent)
        }


        //Abrir el menu de inventario
        btn_inventarioHome = findViewById(R.id.btnInventarioHome)
        btn_inventarioHome.setOnClickListener {
            val intent = Intent(this, Menu_Inventario ::class.java)
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

        val db = Firebase.firestore
        db.collection("Boleta").whereGreaterThanOrEqualTo("fecha", timestampInicio)
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

    override fun onResume() {
        super.onResume()
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
        db.collection("Boleta").whereGreaterThanOrEqualTo("fecha", timestampInicio)
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