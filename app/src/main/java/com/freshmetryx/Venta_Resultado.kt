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
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.itextpdf.text.Chunk
import com.itextpdf.text.Document
import com.itextpdf.text.Font
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
    fun exportToPdf(idVenta: String) {
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
                                var fecha_hora = documentSnapshot.getTimestamp("fecha")
                                val formatoFecha = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale("es", "ES"))
                                val fechaFormateada = formatoFecha.format(fecha_hora!!.toDate())
                                val contentValues = ContentValues().apply {
                                    put(MediaStore.MediaColumns.DISPLAY_NAME, "DetalleVenta_${fechaFormateada}.pdf")
                                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                                    }
                                }
                                val pdfUri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

                                pdfUri?.let {
                                    val outputStream: OutputStream? = resolver.openOutputStream(pdfUri)
                                    outputStream?.let {
                                        PdfWriter.getInstance(document, outputStream)
                                        document.open()

                                        // Contenido del PDF
                                        val data = documentSnapshot.data
                                        if (data != null) {
                                            // Configurar el tamaño y estilo del texto
                                            val font = Font(Font.FontFamily.HELVETICA, 20f, Font.BOLD)

                                            // Agregar contenido al PDF
                                            val paragraph = Paragraph().apply {
                                                // Agregar salto de línea antes de la siguiente línea
                                                spacingBefore = 10f
                                                add(Chunk("Detalle de Venta", Font(Font.FontFamily.HELVETICA, 24f, Font.BOLD)))
                                                add(Chunk("\n")) // Salto de línea
                                                add(Chunk("Fecha de Venta: ${currentDate}", Font(Font.FontFamily.HELVETICA, 16f, Font.NORMAL)))
                                                add(Chunk("\n\n")) // Dos saltos de línea

                                                // Agregar detalles de venta
                                                add(Chunk("Detalles de Venta: \n", Font(Font.FontFamily.HELVETICA, 18f, Font.BOLD)))

                                                // Obtener el mapa de productos
                                                val productosList = documentSnapshot["productos"] as? ArrayList<Map<String, Any>>
                                                productosList?.forEach { detallesProducto ->
                                                    val nombreProducto = detallesProducto["nombre_producto"] as? String
                                                    val cantidadProducto = detallesProducto["cantidad_producto"] as? Long
                                                    val precioProducto = detallesProducto["precio_producto"] as? Long
                                                    if (nombreProducto != null && cantidadProducto != null) {
                                                        add(Chunk(" $nombreProducto: $cantidadProducto unidades, precio: $ ${precioProducto}", Font(Font.FontFamily.HELVETICA, 14f, Font.NORMAL)))
                                                        add(Chunk("\n")) // Salto de línea después de cada detalle
                                                    }
                                                }
                                                val total = documentSnapshot.get("total")
                                                val total_cantProd = documentSnapshot.get("total_cantProd")
                                                add(Chunk("Total General: $$total  Cantidad de productos: ${total_cantProd}", Font(Font.FontFamily.HELVETICA, 14f, Font.NORMAL)))
                                            }
                                            document.add(paragraph)

                                            // Puedes continuar agregando más contenido según tus necesidades

                                            document.close()
                                            Toast.makeText(this, "PDF guardado en Descargas", Toast.LENGTH_LONG).show()

                                            // Abrir el PDF
                                            val openPdfIntent = Intent(Intent.ACTION_VIEW).apply {
                                                setDataAndType(pdfUri, "application/pdf")
                                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            }
                                            startActivity(openPdfIntent)
                                        } else {
                                            Log.e(TAG, "Document data is null")
                                        }
                                    } ?: Log.e(TAG, "OutputStream is null")
                                } ?: Log.e(TAG, "pdfUri is null")
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
