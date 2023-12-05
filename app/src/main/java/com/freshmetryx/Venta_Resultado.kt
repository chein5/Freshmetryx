package com.freshmetryx

import android.Manifest
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.text.TextPaint
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.freshmetryx.databinding.ActivityVentaResultadoBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.itextpdf.text.Document
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfWriter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Venta_Resultado : AppCompatActivity() {

    lateinit var btnVolverCompra: ImageButton
    lateinit var btnPDF: ImageButton
    lateinit var idVenta: String
    private lateinit var correo : String
    private lateinit var docId: String
    private val STORAGE_PERMISSION_CODE = 100
    private lateinit var binding: ActivityVentaResultadoBinding

    var tituloText = "Venta Realizada"
    var descripcionText = "Lorem ipsum dolor sit amet, consectetur adipiscing elit."
    var REQUEST_CODE= 1234
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_venta_resultado)

        binding = ActivityVentaResultadoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        correo = ""
        //capturar el correo y el id desde el intent anterior
        correo = intent.getStringExtra("correo").toString()
        idVenta = intent.getStringExtra("id").toString()


        //solicitarPermisos()
        //Volver al Menu de Ventas
        btnVolverCompra = findViewById(R.id.otraventa_button)
        btnVolverCompra.setOnClickListener {
            val intent = Intent(this, Menu_Ventas::class.java)
            intent.putExtra("correo", correo)
            startActivity(intent)
        }

        //Accion de Descargar PDF
        btnPDF = findViewById(R.id.descargarPDF_button)

        //Solicitar permisos para crear el PDF

        btnPDF.setOnClickListener {
            requestStoragePermission()
            exportToPdf(idVenta)
        }
    }



    @RequiresApi(Build.VERSION_CODES.Q)
    fun exportToPdf(idVenta:String) {
        val db = Firebase.firestore
        db.collection("clientes").whereEqualTo("correo", correo).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    Log.e("TAG", "${document.id} => ${document.data}")
                    docId = document.id
                    db.collection("clientes").document(docId).collection("Boleta").document(idVenta).get()
                        .addOnSuccessListener { documentSnapshot ->
                            if (documentSnapshot.exists()) {
                                val sdf = SimpleDateFormat("dd/MM/yyyy_HH:mm:ss")
                                val currentDate = sdf.format(Date())
                                val document = Document()
                                val resolver = contentResolver
                                val contentValues = ContentValues().apply {
                                    put(MediaStore.MediaColumns.DISPLAY_NAME, "DetalleVenta_${currentDate}.pdf")
                                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                                    }
                                }
                                val pdfUri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                                val outputStream: OutputStream = resolver.openOutputStream(pdfUri!!)!!

                                PdfWriter.getInstance(document, outputStream)
                                document.open()
                                var fecha_hora = documentSnapshot.getTimestamp("fecha")
                                val formatoFecha = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale("es", "ES"))
                                val fechaFormateada = formatoFecha.format(fecha_hora!!.toDate())

                                val data = documentSnapshot.data
                                val formattedData = formatDataForPdf(data)
                                val paragraph = Paragraph(formattedData)
                                document.add(paragraph)

                                document.close()
                                Toast.makeText(this, "PDF guardado en Descargas", Toast.LENGTH_LONG).show()

                                // Open the PDF
                                val openPdfIntent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(pdfUri, "application/pdf")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                startActivity(openPdfIntent)
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.w(TAG, "Error getting documents: ", exception)
                        }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al cargar los datos del negocio", Toast.LENGTH_SHORT).show()
            }

    }

    fun formatDataForPdf(data: Map<String, Any?>?): String {
        // Customize this function to format your data as needed
        val formattedData = StringBuilder()
        data?.forEach { (key, value) ->
            formattedData.append("$key: $value\n")
        }
        return formattedData.toString()
    }
    private fun requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Show an explanation to the user asynchronously
                // After the user sees the explanation, try again to request the permission
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE)
            }
        } else {
            // Permission has already been granted
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            STORAGE_PERMISSION_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permission was granted
                } else {
                    // Permission was denied
                }
                return
            }
            else -> {
                // Ignore all other requests
            }
        }
    }
    override fun onBackPressed() {
        val intent = Intent(this, Venta_Carrito::class.java)
        startActivity(intent)
        this.finish()
    }



}
