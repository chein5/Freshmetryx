package com.freshmetryx

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.widget.Toast
import com.freshmetryx.databinding.ActivityMainBinding
import com.google.zxing.integration.android.IntentIntegrator

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //Configuracion del view Binding (es una funcion que te permite escribir codigo mas facilmente para interactuar con las vistas)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnActivarQR.setOnClickListener{( initScanner())}
    }
    //funcion que nos permitira abrir el scanner
    private fun initScanner(){
        val integrator= IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
        integrator.initiateScan()
    }
    /*Esta funcion nos permitira capturar el resultado del escaneo para mostrarlo con un toast
      primero verificamos si la variable recibio un resultado, para posteriormente mostrar el resultado entregado */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null){
            if(result.contents==null){
                Toast.makeText(this, "cancelado", Toast.LENGTH_LONG).show()
            }else{
                Toast.makeText(this,"el valor escaneado es: "+ result.contents, Toast.LENGTH_LONG ).show()
            }
        }else{
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

}