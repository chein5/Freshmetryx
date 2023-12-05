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
import com.freshmetryx.databinding.ActivityGestionNegocioBinding
import com.freshmetryx.databinding.ActivityMenuGestionBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date

class Gestion_Negocio : AppCompatActivity() {

    //declaracion de variables
    private lateinit var correo: String
    private val db = Firebase.firestore
    private lateinit var binding: ActivityGestionNegocioBinding
    private var docId: String = ""
    private val GALLERY_REQUEST_CODE = 100
    private var photoUri: Uri? = null
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

        binding.imageButton.setOnClickListener {
            openGallery()
        }

        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference
        val photoRef: StorageReference = storageRef.child("${correo}/photos/logo.jpg")
        cargarImagen()


    }
    override fun onResume() {
        super.onResume()
        cargarDatos()
        cargarNegocio()
        cargarImagen()
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

    private fun cargarImagen(){
        val storageReference = Firebase.storage.getReferenceFromUrl("gs://freshmetryx-aa049.appspot.com")
        val imageRef = storageReference.child("/${correo}/photos/logo.jpg")


        imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
            // Use Glide to load the image into the ImageView
            Glide.with(this@Gestion_Negocio)
                .load(downloadUrl)
                .into(binding.imageView33)
        }.addOnFailureListener {
            // Handle any errors
            Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show()
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

    private fun openGallery() {
        val pickPhotoIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(pickPhotoIntent, GALLERY_REQUEST_CODE)
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

        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK) {
            photoUri = data?.data
            uploadImageToFirestore()
        }
    }

    private fun uploadImageToFirestore() {
        val storageReference = Firebase.storage.getReferenceFromUrl("gs://freshmetryx-aa049.appspot.com")
        val imageRef = storageReference.child("/${correo}/photos/logo.jpg")

        // Delete the existing photo if it exists
        imageRef.delete().addOnCompleteListener {
            // Regardless of whether the deletion was successful (the file might not have existed), upload the new photo
            val uploadTask = imageRef.putFile(photoUri!!)

            uploadTask.addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    Glide.with(this@Gestion_Negocio)
                        .load(downloadUrl)
                        .into(binding.imageView33)
                    Toast.makeText(this, "Imagen subida correctamente", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    // Handle any errors
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(this, "Error al subir la imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }


}