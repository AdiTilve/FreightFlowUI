package com.example.freightflow.utils

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.freightflow.R
import com.example.freightflow.model.Assignment
import com.example.freightflow.model.Flight
import com.example.freightflow.network.RetrofitClient
import com.example.freightflow.network.RouteApiService
import com.example.freightflow.network.WebSocketAirport
import com.example.freightflow.network.WebSocketClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AssignmentsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AssignmentAdapter
    private lateinit var webSocketClient: WebSocketClient
    private lateinit var webSocketAirport: WebSocketAirport
    private val assignments = mutableListOf<Assignment>()
    private val TAG = "AssignmentsActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assignments)

        setupRecyclerView()
        loadInitialAssignments() // Load existing assignments first
        setupWebSockets() // Then setup real-time updates
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.assignmentsRecyclerView)
        adapter = AssignmentAdapter(assignments).apply {
            onStartAssignmentClicked = { assignment ->
                Log.d(TAG, "Assignment clicked: ${assignment.assignmentNumber}")
                // Handle click if needed
            }
        }

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@AssignmentsActivity)
            adapter = this@AssignmentsActivity.adapter
            setHasFixedSize(true)
        }
    }

    private fun loadInitialAssignments() {
        RetrofitClient.retrofitInstance.create(RouteApiService::class.java)
            .getAssignments()
            .enqueue(object : Callback<Map<String, Assignment>> {
                override fun onResponse(
                    call: Call<Map<String, Assignment>>,
                    response: Response<Map<String, Assignment>>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.let { assignmentsMap ->
                            assignments.clear()
                            assignments.addAll(assignmentsMap.values)
                            adapter.notifyDataSetChanged()
                            Log.d(TAG, "Loaded ${assignments.size} initial assignments")
                        }
                    } else {
                        Log.e(TAG, "Failed to load assignments: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<Map<String, Assignment>>, t: Throwable) {
                    Log.e(TAG, "Network error loading assignments", t)
                }
            })
    }

    private fun setupWebSockets() {
        // Setup assignment WebSocket (existing code)
        webSocketClient = WebSocketClient("ws://10.0.2.2:8080/ws").apply {
            onAssignmentUpdated = { assignment ->
                handleAssignmentUpdate(assignment)
            }
            onConnectionChanged = { connected ->
                Log.d(TAG, "Assignment WS: ${if (connected) "Connected" else "Disconnected"}")
            }
            onError = { error ->
                Log.e(TAG, "Assignment WS error: $error")
            }
            connect()
        }

        // Setup flight WebSocket (new code)
        webSocketAirport = WebSocketAirport("ws://10.0.2.2:8080/ws").apply {
            onFlightUpdated = { flight ->
                handleFlightUpdate(flight)
            }
            onConnectionChanged = { connected ->
                Log.d(TAG, "Flight WS: ${if (connected) "Connected" else "Disconnected"}")
            }
            onError = { error ->
                Log.e(TAG, "Flight WS error: $error")
            }
            connect()
        }
    }
    private fun handleFlightUpdate(flight: Flight) {
        runOnUiThread {
            // Find all assignments affected by this flight update
            assignments.forEachIndexed { index, assignment ->
                if (assignment.flightNumber == flight.flightNumber) {
                    // Update the flight-related fields in the assignment
                    val updatedAssignment = assignment.copy(
                        flightStatus = flight.state ?: "",
                        // Add other relevant fields
                    )

                    assignments[index] = updatedAssignment
                    adapter.notifyItemChanged(index)
                    Log.d(TAG, "Updated assignment ${assignment.assignmentNumber} due to flight ${flight.flightNumber} change")
                }
            }
        }
    }
    private fun handleAssignmentUpdate(assignment: Assignment) {
        runOnUiThread {
            val existingIndex = assignments.indexOfFirst {
                it.assignmentNumber == assignment.assignmentNumber
            }

            if (existingIndex != -1) {
                assignments[existingIndex] = assignment
                adapter.notifyItemChanged(existingIndex)
                Log.d(TAG, "Updated assignment: ${assignment.assignmentNumber}")
            } else {
                assignments.add(0, assignment) // Add new assignments at top
                adapter.notifyItemInserted(0)
                Log.d(TAG, "Added new assignment: ${assignment.assignmentNumber}")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        webSocketClient.disconnect()
        webSocketAirport.disconnect()
    }
}