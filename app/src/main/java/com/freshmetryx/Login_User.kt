package com.freshmetryx

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.actionCodeSettings
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class Login_User : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    lateinit var btnLogin : ImageButton
    lateinit var txtMail_Login : EditText
    lateinit var txtPass_Login : EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Freshmetryx);
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_user)

        auth = Firebase.auth
        val actionCodeSettings = actionCodeSettings {
            url = "https://www.example.com/finishSignUp?cartId=1234"
            handleCodeInApp = true
            setIOSBundleId("com.example.ios")
            setAndroidPackageName(
                "com.example.android",
                true, // installIfNotAvailable
                "12", // minimumVersion
            )
        }
        btnLogin = findViewById(R.id.btnLogin)
        btnLogin.setOnClickListener {
            accederLogin()
        }

    }



        fun accederLogin(){
            txtMail_Login = findViewById(R.id.txtMail_Login)
            txtPass_Login = findViewById(R.id.txtPass_Login)
            auth.signInWithEmailAndPassword(txtMail_Login.text.toString(), txtPass_Login.text.toString()).
                    addOnCompleteListener(this) {task ->
                        if (task.isSuccessful){
                            Log.d(TAG, "Inicio de sesion exitoso")
                            val user = auth.currentUser
                            val correo= user?.email
                            val intent = Intent(this, Home ::class.java)
                            intent.putExtra("correo", correo)
                            startActivity(intent)
                            //updateUI(user)
                        }else{
                            Log.w(TAG, "Fallo el inicio de sesion")
                            Toast.makeText(baseContext, "fallo la autenticacion",Toast.LENGTH_LONG).show()
                            //UpdateUI(null)
                        }
                    }
        }


}