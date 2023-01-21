package com.sfu.useify.resetPages

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatButton
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.sfu.useify.MainActivity
import com.sfu.useify.R
import com.sfu.useify.ui.authentication.login.LoginActivity

class ResetPassword : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    private lateinit var email_et: EditText
    private lateinit var submit_btn: AppCompatButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset)

        auth = FirebaseAuth.getInstance()

        email_et = findViewById(R.id.edit_reset_email)
        submit_btn = findViewById(R.id.submit_reset_email_button)

        submit_btn.setOnClickListener {
            var email: String = email_et.text.toString()
            if (email.isEmpty()) {
                Toast.makeText(this, "Error: Email field cannot be empty", Toast.LENGTH_LONG).show()
            } else {
                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(this, OnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Reset link sent to your email, please check your mail box", Toast.LENGTH_LONG).show()
                            val intent : Intent = Intent(this, LoginActivity::class.java)
                            startActivity(intent)
                        } else {
                            Toast.makeText(this, "Unable to send reset mail: invalid email", Toast.LENGTH_LONG).show()
                        }
                    })
            }
        }


    }

    fun changeTheme(view: View){
        val nightModeFlags = resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK
        if(nightModeFlags == Configuration.UI_MODE_NIGHT_YES){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }

    }

    fun onResetPassword(view: View){
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}