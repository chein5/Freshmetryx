package com.freshmetryx

import android.content.Intent
import android.os.Binder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.freshmetryx.databinding.ActivityMenuCheckInBinding

class Menu_Check_In : AppCompatActivity() {

    //declaracion de variables
    private lateinit var binding: ActivityMenuCheckInBinding
    private lateinit var correo : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_check_in)

        //inicializacion de binding para trabajar con las vistas directamente
        binding = ActivityMenuCheckInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        correo = ""
        correo = intent.getStringExtra("correo").toString()

        //Abrir el historial de pedidos
        binding.IbtnHistoriapedidoMenu.setOnClickListener {
            val intent = Intent(this, Historial_Check_In ::class.java)
            intent.putExtra("correo", correo)
            startActivity(intent)
        }

        //Iniciar el modulo de chech in
        binding.ibtnRealizarCheckinMenu.setOnClickListener {
            val intent = Intent(this, Check_In_One ::class.java)
            intent.putExtra("correo", correo)
            startActivity(intent)
        }
    }
}