package com.freshmetryx

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.annotation.RequiresApi
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore

class Venta_Listar : AppCompatActivity() {
    lateinit var list_hventas : ListView
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_venta_listar)
        llenarList()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun llenarList() {
        var lista_boletas: ArrayList<Boleta> = ArrayList()
        val db = FirebaseFirestore.getInstance()
        db.collection("Boleta").get().addOnSuccessListener { result ->
            for (document in result) {
                var boleta = Boleta(
                    document.get("fecha") as Timestamp,
                    document.get("total_cantProd") as Long,
                    document.get("total") as Long
                )
                lista_boletas.add(boleta)
            }

            list_hventas = findViewById(R.id.list_hventas)
            val adapter: ArrayAdapter<Boleta> = ArrayAdapter<Boleta>(
                this@Venta_Listar,
                android.R.layout.simple_list_item_1,
                lista_boletas
            )
            list_hventas.adapter = adapter
            adapter.notifyDataSetChanged()
        }
    }
}