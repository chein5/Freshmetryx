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
        val spinner = findViewById<Spinner>(R.id.spinner2)
        val db = Firebase.firestore
        val items = ArrayList<String>()

        //inicializacion de binding para trabajar con las vistas directamente
        binding = ActivityCheckInOneBinding.inflate(layoutInflater)
        setContentView(binding.root)

        docId=""

        //Recibir correo
        correo = ""
        correo = intent.getStringExtra("correo").toString()

        //Iniciar el modulo de camara para guardar registros
        binding.ibtnActivarCamaraCI1.setOnClickListener {
            startCamera()
        }

        binding.ibtnSiguienteChech1.setOnClickListener {
            guardarDatosPT1()
        }
        db.collection("clientes").whereEqualTo("correo", correo).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    docId = document.id
                    // buscar los datos en la BD
                    db.collection("clientes").document(docId).collection("Proveedores").get().addOnSuccessListener { documents ->
                        for (document in documents) {
                            items.add(document.getString("nombre")!!)
                        }
                        //agregar un dato nuevo
                        items.add("Agregar Proveedor")

                        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spinner.adapter = adapter
                    }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al cargar los datos del negocio", Toast.LENGTH_SHORT).show()
            }


        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selectedItem = parent.getItemAtPosition(position).toString()
                if (selectedItem == "Agregar Proveedor") {
                    // mostrar un nuevo alert dialog para agregar
                    val editText = EditText(this@Check_In_One)
                    AlertDialog.Builder(this@Check_In_One)
                        .setTitle("Agregar Proveedor")
                        .setView(editText)
                        .setPositiveButton("Agregar") { dialog, whichButton ->
                            val newItem = editText.text.toString()
                            // agregar el nuevo elemento a la BD
                            db.collection("clientes").document("donde_rosa").collection("Proveedores").add(hashMapOf("nombre" to newItem))
                                .addOnSuccessListener {
                                    // cargar el spinner de nuevo
                                    items.add(items.size - 1, newItem)
                                    (spinner.adapter as ArrayAdapter<String>).notifyDataSetChanged()
                                }
                        }
                        .setNegativeButton("Cancelar", null)
                        .show()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
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
        val imageRef = storageReference.child("/${correo}/images/${photoUri?.lastPathSegment}")
        val uploadTask = imageRef.putFile(photoUri!!)

        uploadTask.addOnSuccessListener {
            Toast.makeText(this, "Imagen subida", Toast.LENGTH_SHORT).show()
            isImageUploaded = true
        }.addOnFailureListener { exception ->
            Toast.makeText(this, "Error al subir la imagen", Toast.LENGTH_SHORT).show()
        }
    }

    private fun guardarDatosPT1() {
        val db = Firebase.firestore

        // Get the selected item from the Spinner
        val selectedItem = binding.spinner2.selectedItem.toString()

        // Check the state of the CheckBox
        val isRepartidorLlegaAlLocal = binding.checkBox.isChecked

        // Check which RadioButton is selected in the RadioGroup
        val isBoletaEntregada = when (binding.rdbgroupBoleta.checkedRadioButtonId) {
            R.id.rdb_si -> true
            R.id.rdb_no -> false
            else -> false
        }

        // Get the name of the uploaded image
        val imageName = photoUri?.lastPathSegment

        // Create a new document in Firestore with this data
        val data = hashMapOf(
            "Proveedor" to selectedItem,
            "RepartidorLlegaAlLocal" to isRepartidorLlegaAlLocal,
            "BoletaEntregada" to isBoletaEntregada,
            "Imagen" to imageName
        )

        db.collection("clientes").document(correo).collection("CheckIns").add(data)
            .addOnSuccessListener {
                var docID = it.id
                Toast.makeText(this, "Data saved successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error saving data: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}