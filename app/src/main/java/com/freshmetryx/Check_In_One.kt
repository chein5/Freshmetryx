package com.freshmetryx

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class Check_In_One : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_in_one)
        val spinner = findViewById<Spinner>(R.id.spinner2)
        val db = Firebase.firestore
        val items = ArrayList<String>()

// Fetch the data from the database
        db.collection("clientes").document("donde_rosa").collection("Proveedores").get().addOnSuccessListener { documents ->
            for (document in documents) {
                items.add(document.getString("nombre")!!)
            }
            // Add a special item to the list
            items.add("Agregar Proveedor")

            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selectedItem = parent.getItemAtPosition(position).toString()
                if (selectedItem == "Agregar Proveedor") {
                    // Show an AlertDialog with an EditText to input the new item
                    val editText = EditText(this@Check_In_One)
                    AlertDialog.Builder(this@Check_In_One)
                        .setTitle("Agregar Proveedor")
                        .setView(editText)
                        .setPositiveButton("Agregar") { dialog, whichButton ->
                            val newItem = editText.text.toString()
                            // Add the new item to the database
                            db.collection("clientes").document("donde_rosa").collection("Proveedores").add(hashMapOf("nombre" to newItem))
                                .addOnSuccessListener {
                                    // Refresh the Spinner data
                                    items.add(items.size - 1, newItem)
                                    (spinner.adapter as ArrayAdapter<String>).notifyDataSetChanged()
                                }
                        }
                        .setNegativeButton("Cancelar", null)
                        .show()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }
    }
}