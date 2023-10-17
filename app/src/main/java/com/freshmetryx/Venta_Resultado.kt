package com.freshmetryx

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.text.TextPaint
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream

class Venta_Resultado : AppCompatActivity() {

    lateinit var btnVolverCompra: ImageButton
    lateinit var btnPDF: ImageButton

    var tituloText = "Venta Realizada"
    var descripcionText = "Lorem ipsum dolor sit amet, consectetur adipiscing elit."

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_venta_resultado)

        //Volver al Menu de Ventas
        btnVolverCompra = findViewById(R.id.otraventa_button)
        btnVolverCompra.setOnClickListener {
            val intent = Intent(this, Menu_Ventas::class.java)
            startActivity(intent)
        }

        //Accion de Descargar PDF
        btnPDF = findViewById(R.id.descargarPDF_button)

        if (checkPermission()) {
            Toast.makeText(this, "Permiso Aceptado", Toast.LENGTH_SHORT).show()
        } else {
            requestPermission()
        }

        btnPDF.setOnClickListener {
            generarPDF()
        }
    }

    //Creacion del PDF
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
        val file = File(getExternalFilesDir(null), "Boleta.pdf")
        try {
            pdfDocument.writeTo(FileOutputStream(file))
            Toast.makeText(this, "PDF Descargado", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        pdfDocument.close()

    }

    //Funciones para los permisos del usuario para descargar el PDF
    private fun checkPermission(): Boolean {
        val permission1 = ContextCompat.checkSelfPermission(applicationContext, WRITE_EXTERNAL_STORAGE)
        val permission2 = ContextCompat.checkSelfPermission(applicationContext, READ_EXTERNAL_STORAGE)
        return permission1 == PackageManager.PERMISSION_GRANTED && permission2 == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE),
            200
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 200) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permisos concedidos", Toast.LENGTH_SHORT).show()
                // Ahora puedes realizar la operación que requiere permisos
            } else {
                Toast.makeText(this, "Permisos denegados", Toast.LENGTH_SHORT).show()
                // Maneja el caso en el que el usuario no concedió los permisos
            }
        }
    }
}
