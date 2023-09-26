package com.freshmetryx

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton

class Home : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Freshmetryx);
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        //Botones
        lateinit var btnQr : ImageButton
        btnQr = findViewById(R.id.btnEscaner)
        btnQr.setOnClickListener {
            val intent = Intent(this, MainActivity ::class.java)
            startActivity(intent)
        }
    }
}