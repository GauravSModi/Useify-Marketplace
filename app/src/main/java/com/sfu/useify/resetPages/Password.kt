package com.sfu.useify.resetPages

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatButton
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.sfu.useify.MainActivity
import com.sfu.useify.R
import com.sfu.useify.ui.authentication.login.LoginActivity

class Password : AppCompatActivity(){
    private lateinit var newPassword_et: EditText
    private lateinit var submit_btn: AppCompatButton

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.password)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setTitle(R.string.change_password)

        auth = FirebaseAuth.getInstance()

        newPassword_et = findViewById(R.id.enter_new_password)
        submit_btn = findViewById(R.id.done_new_password_button)

        submit_btn.setOnClickListener {
            val newPassword: String = newPassword_et.text.toString()
            if (newPassword.isEmpty()) {
                Toast.makeText(this, "Please enter password", Toast.LENGTH_LONG).show()
            } else {
                auth.currentUser?.updatePassword(newPassword)
                    ?.addOnCompleteListener(this, OnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Password changes successfully", Toast.LENGTH_LONG).show()
                            val intent : Intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                        } else {
                            Toast.makeText(this, "Password not changed", Toast.LENGTH_LONG).show()
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

    // Handle clicks on menu items
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun goToMain(view: View){
        val intent = Intent(this, GeneralSettings::class.java)
        startActivity(intent)
    }
}