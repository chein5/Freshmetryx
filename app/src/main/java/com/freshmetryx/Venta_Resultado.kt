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
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.itextpdf.text.Document
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfWriter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date

class Venta_Resultado : AppCompatActivity() {

    lateinit var btnVolverCompra: ImageButton
    lateinit var btnPDF: ImageButton
    lateinit var idVenta: String
    private lateinit var correo : String
    private lateinit var docId: String
    private val STORAGE_PERMISSION_CODE = 100
    var tituloText = "Venta Realizada"
    var descripcionText = "Lorem ipsum dolor sit amet, consectetur adipiscing elit."
    var REQUEST_CODE= 1234
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_venta_resultado)

        correo = ""
        //capturar el correo y el id desde el intent anterior
        correo = intent.getStringExtra("correo").toString()
        idVenta = intent.getStringExtra("id").toString()

        Toast.makeText(this, "Correo: "+correo, Toast.LENGTH_SHORT).show()
        Toast.makeText(this, "ID: "+idVenta, Toast.LENGTH_SHORT).show()

        //solicitarPermisos()
        //Volver al Menu de Ventas
        btnVolverCompra = findViewById(R.id.otraventa_button)
        btnVolverCompra.setOnClickListener {
            val intent = Intent(this, Menu_Ventas::class.java)
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

    /*
    fun generarPDF(){

        //Variables para el PDF
        var pdfDocument = PdfDocument()
        var paint = Paint()
        var titulo = TextPaint()
        var descripcion = TextPaint()

        //Dimensiones del PDF
        var paginaInfo = PdfDocument.PageInfo.Builder(816, 1054, 1).create() //Ancho x Alto x Paginas
        var pagina1 = pdfDocument.startPage(paginaInfo)

        var canvas = pagina1.canvas

        //Agregar una imagen al PDF
        var bitmap = BitmapFactory.decodeResource(resources, R.drawable.app_icon)
        var bitmapScaled = Bitmap.createScaledBitmap(bitmap, 80, 80, false) //Dimensiones de la imagen
        canvas.drawBitmap(bitmapScaled, 20f, 20f, paint)

        //Agregar texto al PDF
        titulo.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD))
        titulo.setTextSize(20f)
        canvas.drawText(tituloText, 120f, 60f, titulo)

        //Descripcion del PDF
        descripcion.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL))
        descripcion.setTextSize(15f)

        //Salto de linea en la descripcion
        var arrDescripcion = descripcionText.split("\n")
        var y = 100f
        for (linea in arrDescripcion) {
            canvas.drawText(linea, 120f, y, descripcion)
            y += descripcion.descent() - descripcion.ascent()
        }

        pdfDocument.finishPage(pagina1)

        //Guardar el PDF
        val file = File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "Boleta.pdf")
        try {
            pdfDocument.writeTo(FileOutputStream(file))
            Toast.makeText(this, "PDF Descargado", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        pdfDocument.close()

    }

    //Solicitar permisos para crear el pdf
    fun solicitarPermisos() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_CODE)
        Toast.makeText(this, "Permisos concedidos", Toast.LENGTH_LONG).show()
    }



// ...

     */

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

                                val data = documentSnapshot.data
                                val paragraph = Paragraph(data.toString())
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
