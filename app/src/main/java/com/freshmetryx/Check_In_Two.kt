package com.freshmetryx

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.freshmetryx.databinding.ActivityCheckInTwoBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

class Check_In_Two : AppCompatActivity() {
    private val CAMERA_REQUEST_CODE = 200
    private var photoUri: Uri? = null
    private lateinit var correo :String
    private lateinit var idCheckin :String
    private lateinit var binding: ActivityCheckInTwoBinding
    private lateinit var docId: String
    private var isImageUploaded = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_in_two)


        //capturar el correo y el id desde el intent anterior
        correo = ""
        correo = intent.getStringExtra("correo").toString()
        idCheckin = ""
        idCheckin = intent.getStringExtra("idCheck").toString()

        docId = ""

        binding = ActivityCheckInTwoBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //Mostrar datos del negocio
        cargarNegocio()
        cargarImagen()

        binding.ibtnActivarCamaraC2.setOnClickListener {
            startCamera()
        }

        binding.ibtnSiguienteC2.setOnClickListener {
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
                    binding.txtvNombreNegocioC2.text = document.getString("nombre_negocio")
                    binding.txtvNombreClienteC2.text = document.getString("nombre_cliente")
                    cargarImagen()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al cargar los datos del negocio", Toast.LENGTH_SHORT).show()
            }
    }

    private fun cargarImagen(){
        val storageReference = Firebase.storage.getReferenceFromUrl("gs://freshmetryx-aa049.appspot.com")
        val imageRef = storageReference.child("/${correo}/photos/logo.jpg")


        imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
            // Use Glide to load the image into the ImageView
            Glide.with(this@Check_In_Two)
                .load(downloadUrl)
                .into(binding.imageView25)
        }.addOnFailureListener {
            // Handle any errors
            Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show()
        }
    }
    private fun startCamera() {
        if (!isImageUploaded) {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (takePictureIntent.resolveActivity(packageManager) != null) {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    null
                }
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.example.android.fileprovider",
                        it
                    )
                    photoUri = photoURI
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE)
                }
            } else {
                Toast.makeText(this, "Ya se subio una imagen", Toast.LENGTH_SHORT).show()
            }

        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            photoUri = Uri.fromFile(this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            uploadImageToFirestore()
        }
    }

    private fun uploadImageToFirestore() {
        val storageReference = Firebase.storage.getReferenceFromUrl("gs://freshmetryx-aa049.appspot.com")
        val imageRef = storageReference.child("/${correo}/images/damage/${photoUri?.lastPathSegment}")
        val uploadTask = imageRef.putFile(photoUri!!)

        uploadTask.addOnSuccessListener {
            Toast.makeText(this, "Imagen subida", Toast.LENGTH_SHORT).show()
            isImageUploaded = true
        }.addOnFailureListener { exception ->
            Toast.makeText(this, "Error al subir la imagen", Toast.LENGTH_SHORT).show()
        }
    }

    private fun guardarDatos() {
        // If the radio button is set to true and the image is not uploaded, show a Toast message and return
        if (binding.rdbSi2.isChecked && !isImageUploaded) {
            Toast.makeText(this, "Por favor suba una foto", Toast.LENGTH_SHORT).show()
            return
        }

        val db = Firebase.firestore
        val data = hashMapOf<String, Any>(
            "producto_damage" to binding.rdbSi2.isChecked.toString(),
            "todos_productos" to binding.rdbSi3.isChecked.toString()
        )

        // If the radio button is set to true, add the image URL to the data
        if (binding.rdbSi2.isChecked) {
            data["url_imgProducto"] = photoUri.toString()
        }

        db.collection("clientes").whereEqualTo("correo", correo).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    docId = document.id
                    val newDocRef = db.collection("clientes").document(docId).collection("checkIn").document(idCheckin)
                    newDocRef.update(data)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Datos guardados", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, Check_In_Three::class.java)
                            intent.putExtra("correo", correo)
                            intent.putExtra("idCheck", idCheckin) // pasarle el id del nuevo CheckIn al intent
                            startActivity(intent)
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error al guardar los datos: $e", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al cargar los datos del negocio", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onBackPressed() {
        if (isImageUploaded) {
            val storageReference = Firebase.storage.getReferenceFromUrl("gs://freshmetryx-aa049.appspot.com")
            val imageRef = storageReference.child("/${correo}/images/boleta/${photoUri?.lastPathSegment}")
            imageRef.delete()
                .addOnSuccessListener {
                    Toast.makeText(this, "Imagen borrada", Toast.LENGTH_SHORT).show()
                    isImageUploaded = false
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al borrar la foto: $e", Toast.LENGTH_SHORT).show()
                }
        }

        super.onBackPressed()
    }
}