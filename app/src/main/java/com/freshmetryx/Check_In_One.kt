package com.freshmetryx

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import com.freshmetryx.databinding.ActivityCheckInOneBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

class Check_In_One : AppCompatActivity() {
    private val CAMERA_REQUEST_CODE = 200
    private var photoUri: Uri? = null
    private lateinit var binding: ActivityCheckInOneBinding
    private lateinit var correo : String
    private lateinit var docId: String
    private var isImageUploaded = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_in_one)
        val db = Firebase.firestore
        val items = ArrayList<String>()

        //inicializacion de binding para trabajar con las vistas directamente
        binding = ActivityCheckInOneBinding.inflate(layoutInflater)
        setContentView(binding.root)

        docId=""

        //Recibir correo
        correo = ""
        correo = intent.getStringExtra("correo").toString()

        //Cargar datos del negocio
        cargarNegocio()

        //Iniciar el modulo de camara para guardar registros
        binding.ibtnActivarCamaraCI1.setOnClickListener {
            startCamera()
        }

        binding.ibtnSiguienteChech1.setOnClickListener {
            guardarDatos()
        }

        db.collection("clientes").whereEqualTo("correo", correo).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    docId = document.id

                    db.collection("clientes").document(docId).collection("Proveedores").get()
                        .addOnSuccessListener { documents ->
                            val proveedoresList = ArrayList<String>()
                            for (document in documents) {

                                proveedoresList.add(document.getString("nombre")!!)
                            }
                            // Add an option to add a new "proveedor"
                            proveedoresList.add("Agregar Proveedor")
                            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, proveedoresList)
                            binding.spinner2.adapter = adapter

                            binding.spinner2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                                override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                                    val selectedItem = parent.getItemAtPosition(position).toString()
                                    // If the selected item is "Add new...", show an AlertDialog with an EditText to input the new "proveedor"
                                    if (selectedItem == "Agregar Proveedor") {
                                        val editText = EditText(this@Check_In_One)
                                        AlertDialog.Builder(this@Check_In_One)
                                            .setTitle("Nuevo Proveedor")
                                            .setMessage("Nombre del nuevo proveedor")
                                            .setView(editText)
                                            .setPositiveButton("Agregar") { dialog, _ ->
                                                val newProveedor = editText.text.toString()
                                                // Add the new "proveedor" to Firestore
                                                val data = hashMapOf("nombre" to newProveedor)
                                                db.collection("clientes").document(docId).collection("Proveedores").add(data)
                                                    .addOnSuccessListener {
                                                        // Refresh the spinner data
                                                        Toast.makeText(this@Check_In_One, "Proveedor agregado", Toast.LENGTH_SHORT).show()
                                                        proveedoresList.add(proveedoresList.size - 1, newProveedor)
                                                        (binding.spinner2.adapter as ArrayAdapter<String>).notifyDataSetChanged()
                                                        dialog.dismiss()
                                                    }
                                                    .addOnFailureListener { e ->
                                                        Toast.makeText(this@Check_In_One, "Error adding document: $e", Toast.LENGTH_SHORT).show()
                                                        dialog.dismiss()
                                                    }
                                            }
                                            .setNegativeButton("Cancelar") { dialog, _ ->
                                                dialog.cancel()
                                            }
                                            .show()
                                    }
                                }

                                override fun onNothingSelected(parent: AdapterView<*>) {
                                    // Do nothing
                                }
                            }
                        }

                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al cargar los datos del negocio", Toast.LENGTH_SHORT).show()
            }

        binding.rdbNo.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Toast.makeText(this, "Se ha seleccionado no boleta fisica", Toast.LENGTH_SHORT).show()
            }
            binding.ibtnActivarCamaraCI1.isEnabled = !isChecked
        }

        }


    //Mostrar datos del negocio
    private fun cargarNegocio(){
        val db = Firebase.firestore
        db.collection("clientes").whereEqualTo("correo", correo).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    docId = document.id
                    binding.txtvNombreNegocioAC1.text = document.getString("nombre_negocio")
                    binding.txtvNombreClienteAC1.text = document.getString("nombre_cliente")
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al cargar los datos del negocio", Toast.LENGTH_SHORT).show()
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
        val imageRef = storageReference.child("/${correo}/images/boleta/${photoUri?.lastPathSegment}")
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
        if (binding.rdbSi.isChecked && !isImageUploaded) {
            Toast.makeText(this, "Por favor suba una foto", Toast.LENGTH_SHORT).show()
            return
        }

        val db = Firebase.firestore
        val selectedRadioButtonId = binding.rdbgroupBoleta.checkedRadioButtonId
        val selectedRadioButton = findViewById<RadioButton>(selectedRadioButtonId)
        val data = hashMapOf(
            "proveedor" to binding.spinner2.selectedItem.toString(),
            "entrega_boleta" to selectedRadioButton.text.toString()
        )

        // If the radio button is set to true, add the image URL to the data
        if (binding.rdbSi.isChecked) {
            data["url_imgBoleta"] = photoUri.toString()
        }

        db.collection("clientes").whereEqualTo("correo", correo).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    docId = document.id
                    val newDocRef = db.collection("clientes").document(docId).collection("checkIn").document()
                    newDocRef.set(data)
                        .addOnSuccessListener {
                            val newDocId = newDocRef.id // id del checkIn agregado
                            Toast.makeText(this, "Datos guardados", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, Check_In_Two::class.java)
                            intent.putExtra("correo", correo)
                            intent.putExtra("idCheck", newDocId) // pasarle el id del nuevo CheckIn al intent
                            startActivity(intent)
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error saving data: $e", Toast.LENGTH_SHORT).show()
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