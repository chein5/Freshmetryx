package com.freshmetryx

import android.content.SharedPreferences
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView

class Gestion_Perfil : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gestion_perfil)

        val sharedPref: SharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)

        val colorButton1: ImageButton = findViewById(R.id.imageButton26)
        val colorButton2: ImageButton = findViewById(R.id.imageButton27)
        val textView1: TextView = findViewById(R.id.textView85)
        val textView2: TextView = findViewById(R.id.textView86)

        colorButton1.setOnClickListener {
            val editor: SharedPreferences.Editor = sharedPref.edit()
            editor.putString("color", "color_1")
            editor.apply()

            // Change the background color of the buttons
            colorButton1.setBackgroundColor(Color.parseColor("#FF0000")) // Change this to the color you want for "color_1"
            colorButton2.setBackgroundColor(Color.parseColor("#FF0000")) // Change this to the color you want for "color_1"

            // Change the text color of the text fields
            textView1.setTextColor(Color.parseColor("#FF0000")) // Change this to the color you want for "color_1"
            textView2.setTextColor(Color.parseColor("#FF0000")) // Change this to the color you want for "color_1"
        }

        colorButton2.setOnClickListener {
            val editor: SharedPreferences.Editor = sharedPref.edit()
            editor.putString("color", "color_2")
            editor.apply()

            // Change the background color of the buttons
            colorButton1.setBackgroundColor(Color.parseColor("#00FF00")) // Change this to the color you want for "color_2"
            colorButton2.setBackgroundColor(Color.parseColor("#00FF00")) // Change this to the color you want for "color_2"

            // Change the text color of the text fields
            textView1.setTextColor(Color.parseColor("#00FF00")) // Change this to the color you want for "color_2"
            textView2.setTextColor(Color.parseColor("#00FF00")) // Change this to the color you want for "color_2"
        }

        // Repeat the above steps for all color buttons

        val imageButton1: ImageButton = findViewById(R.id.imageButton19)
        imageButton1.setOnClickListener {
            val editor: SharedPreferences.Editor = sharedPref.edit()
            editor.putString("image", "user_1")
            editor.apply()
            // Apply the image to your theme or layout
        }
    }
}

