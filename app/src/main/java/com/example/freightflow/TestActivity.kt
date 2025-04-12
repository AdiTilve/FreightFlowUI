package com.example.freightflow  // Must match manifest
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.freightflow.R
import io.reactivex.disposables.Disposable
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent

class TestActivity : AppCompatActivity() {

    private var stompClient: StompClient? = null
    private var topicSubscription: Disposable? = null
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        messageEditText = findViewById(R.id.messageEditText)
        sendButton = findViewById(R.id.sendButton)

        connectWebSocket()

        sendButton.setOnClickListener {
            val message = messageEditText.text.toString().trim()
            if (message.isNotEmpty()) {
                sendMessage(message)
            } else {
                showToast("Please enter a message")
            }
        }
    }

    private fun connectWebSocket() {
        val serverUrl = "ws://10.0.2.2:8080/ws" // For emulator

        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, serverUrl)
        stompClient?.connect()

        topicSubscription = stompClient?.topic("/all/updateAssignment")
            ?.subscribe { message ->
                runOnUiThread {
                    showToast("Received: ${message.payload}")
                }
            }

        stompClient?.lifecycle()?.subscribe { event ->
            runOnUiThread {
                when (event.type) {
                    LifecycleEvent.Type.OPENED -> showToast("Connected")
                    LifecycleEvent.Type.ERROR -> showToast("Error: ${event.exception?.message}")
                    LifecycleEvent.Type.CLOSED -> showToast("Connection closed")
                    LifecycleEvent.Type.FAILED_SERVER_HEARTBEAT -> {
                        // Handle heartbeat failure if needed
                        showToast("Server heartbeat failed")
                    }
                }
            }
        }
    }

    private fun sendMessage(message: String) {
        if (stompClient?.isConnected == true) {
            stompClient?.send("/app/application", message)?.subscribe(
                { showToast("Message sent") },
                { error -> showToast("Send error: ${error.message}") }
            )
        } else {
            showToast("Not connected to server")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        topicSubscription?.dispose()
        stompClient?.disconnect()
        super.onDestroy()
    }
}