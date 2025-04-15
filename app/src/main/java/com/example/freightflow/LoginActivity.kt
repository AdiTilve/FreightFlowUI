package com.example.freightflow

import UserApiService
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.lifecycle.lifecycleScope
import androidx.test.core.app.ActivityScenario.launch
import com.example.freightflow.R
import com.example.freightflow.SignupActivity
import com.example.freightflow.databinding.ActivityLoginBinding
import com.example.freightflow.model.Assignment
import com.example.freightflow.network.RetrofitClient
import com.example.freightflow.network.RouteApiService
import com.example.freightflow.utils.AssignmentsActivity
import io.ktor.client.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.awaitResponse

class LoginActivity : AppCompatActivity() {

    private var isPasswordVisible: Boolean = false
    private lateinit var username: String
    private lateinit var password: String
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
            var username = findViewById<EditText>(R.id.username).text.toString()
            var password = findViewById<EditText>(R.id.password).text.toString()
            RetrofitClient.retrofitInstance.create(UserApiService::class.java)
                .login(username, password).enqueue(object : Callback<String> {
                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        Log.d(TAG, "Made it in onresponse")
                        if (response.isSuccessful) {
                            Log.d(TAG, "is Successful")
//                            response.body()
                            val intent = Intent(applicationContext, AssignmentsActivity::class.java)
                            Log.d(TAG, "intent created")
                            startActivity(intent)
                        } else {
                            Toast.makeText(applicationContext, "Authentication Failed", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<String>, t: Throwable) {
                        Log.e(TAG, "Network error loading assignments", t)
                    }
                })
        }
    }
}

