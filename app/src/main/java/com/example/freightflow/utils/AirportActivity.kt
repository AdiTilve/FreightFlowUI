package com.example.freightflow.utils

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.freightflow.databinding.ActivityAirportBinding
import com.example.freightflow.model.Assignment
import com.example.freightflow.model.Flight
import com.example.freightflow.network.RetrofitClient
import com.example.freightflow.network.RouteApiService
import com.example.freightflow.network.WebSocketAirport
import com.example.freightflow.network.WebSocketClient
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.awaitResponse

class AirportActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAirportBinding
    private lateinit var adapter: AirportAdapter
    private lateinit var webSocketAssignment: WebSocketClient
    private lateinit var webSocketFlight: WebSocketAirport
    private var currentFlightNumber: String? = null
    private var currentAssignmentNumber: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAirportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize adapter
        adapter = AirportAdapter()
        binding.airportRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@AirportActivity)
            adapter = this@AirportActivity.adapter
        }

        // Get intent extras
        currentFlightNumber = intent.getStringExtra("FLIGHT_NUMBER") ?: return
        currentAssignmentNumber = intent.getStringExtra("ASSIGNMENT_NUMBER")

        // Initialize WebSocket clients
        initAssignmentWebSocket()
        initFlightWebSocket()

        // Load initial data
        loadCombinedData(currentFlightNumber!!, currentAssignmentNumber)
    }

    private fun initAssignmentWebSocket() {
        webSocketAssignment = WebSocketClient().apply {
            onAssignmentUpdated = { assignment ->
                if (assignment.assignmentNumber == currentAssignmentNumber) {
                    runOnUiThread {
                        adapter.updateAssignment(assignment)
                        showToast("Assignment updated in real-time!")
                    }
                }
            }

            onConnectionChanged = { isConnected ->
                runOnUiThread {
                    val status = if (isConnected) "Assignment WS: Connected" else "Assignment WS: Disconnected"
                    showToast(status)
                }
            }

            onError = { error ->
                runOnUiThread {
                    showToast("Assignment WS error: $error", Toast.LENGTH_LONG)
                }
            }
        }
    }

    // In your AirportActivity
    private fun initFlightWebSocket() {
        webSocketFlight = WebSocketAirport().apply {
            onFlightUpdated = { flight ->
                Log.d("FlightUpdate", "Received update for ${flight.flightNumber}")
                if (flight.flightNumber == currentFlightNumber) {
                    runOnUiThread {
                        Log.d("FlightUpdate", "Updating UI for flight ${flight.flightNumber}")
                        adapter.updateFlight(flight)
                        binding.airportRecyclerView.post {
                            adapter.notifyDataSetChanged()
                        }
                    }
                }
            }

            onConnectionChanged = { isConnected ->
                Log.d("FlightWS", if (isConnected) "Connected" else "Disconnected")
                runOnUiThread {
                    val status = if (isConnected) "Flight WS: Connected" else "Flight WS: Disconnected"
                    showToast(status)
                }
            }

            onError = { error ->
                Log.e("FlightWS", "Error: $error")
                runOnUiThread {
                    showToast("Flight WS error: $error", Toast.LENGTH_LONG)
                }
            }
        }
        webSocketFlight.connect()
    }

    override fun onResume() {
        super.onResume()
        // Connect WebSockets when activity resumes
        webSocketAssignment.connect()
        webSocketFlight.connect()
    }

    override fun onPause() {
        super.onPause()
        // Disconnect WebSockets when activity pauses
        webSocketAssignment.disconnect()
        webSocketFlight.disconnect()
    }

    private fun loadCombinedData(flightNumber: String, assignmentNumber: String?) {
        val flightCall = RetrofitClient.retrofitInstance.create(RouteApiService::class.java)
            .getFlight(flightNumber)

        lifecycleScope.launch {
            try {
                // Get flight data
                val flight = async { executeApiCall(flightCall) }.await()

                // Get assignment data if number exists
                val assignment = assignmentNumber?.let { number ->
                    val assignmentCall = RetrofitClient.retrofitInstance.create(RouteApiService::class.java)
                        .getAssignment(number)
                    async { executeApiCall(assignmentCall) }.await()
                }

                // Update UI with both datasets
                adapter.updateData(flight, assignment)

            } catch (e: Exception) {
                Log.e("LOAD_ERROR", "Error loading combined data", e)
                showToast("Failed to load data: ${e.message}", Toast.LENGTH_LONG)
            }
        }
    }

    private suspend fun <T> executeApiCall(call: Call<T>): T? {
        return try {
            val response = call.awaitResponse()
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            null
        }
    }

    private fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, message, duration).show()
    }
}