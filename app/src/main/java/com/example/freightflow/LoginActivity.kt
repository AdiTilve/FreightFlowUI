package com.example.freightflow

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import com.example.freightflow.R
import com.example.freightflow.SignupActivity
import com.example.freightflow.utils.AssignmentsActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private var isPasswordVisible: Boolean = false
    private lateinit var passwordEditText: EditText
    private lateinit var passwordToggleIcon: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.hide()  // Hides the ActionBar
        setContentView(R.layout.activity_login)
        val signUpButton: Button = findViewById(R.id.signUpButton)

        signUpButton.setOnClickListener {
            // Create an Intent to navigate to the SignupActivity
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }

        val loginButton: Button = findViewById(R.id.loginButton)

        // Set an OnClickListener to navigate to com.example.freightflow.utils.AirportActivity
        loginButton.setOnClickListener {
            // Create an Intent to go to com.example.freightflow.utils.AirportActivity
            val intent = Intent(this, AssignmentsActivity::class.java)
            startActivity(intent)
        }
    }
}

