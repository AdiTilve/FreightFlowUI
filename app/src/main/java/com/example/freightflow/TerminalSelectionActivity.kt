package com.example.freightflow
//
//import android.Manifest
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.os.Bundle
//import android.util.Log
//import android.widget.ArrayAdapter
//import android.widget.Button
//import android.widget.Spinner
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat.startActivity
//import com.google.android.gms.location.FusedLocationProviderClient
//import com.google.android.gms.location.LocationServices
//import com.google.android.gms.tasks.OnSuccessListener
////import com.example.freightflow.MapsActivity  // Correctly import MapsActivity
//
//class TerminalSelectionActivity : AppCompatActivity() {
//    private lateinit var fusedLocationClient: FusedLocationProviderClient
//    private val LOCATION_PERMISSION_REQUEST_CODE = 1000
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_terminal)
//
//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
//
//        // Spinner setup (list of terminals)
//        val terminalSpinner: Spinner = findViewById(R.id.terminalSpinner)
//        val terminals = arrayOf("Terminal A", "Terminal B", "Terminal C", "Terminal D", "Terminal E")
//        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, terminals)
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        terminalSpinner.adapter = adapter
//
//        // Confirm button
//        val confirmButton: Button = findViewById(R.id.confirmTerminalButton)
//        confirmButton.setOnClickListener {
//            val selectedTerminal = terminalSpinner.selectedItem.toString()
//            Log.d("TerminalSelectionActivity", "Confirm button clicked")
//            // Get the current geolocation (GPS coordinates)
//            getCurrentLocation { currentLocation ->
//                if (currentLocation != null) {
//                    sendToBackend(currentLocation, selectedTerminal)
//                } else {
//                    Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//    }
//
//    private fun getCurrentLocation(callback: (String?) -> Unit) {
//        if (ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.ACCESS_COARSE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            Log.d("TerminalSelectionActivity", "Location permission not granted")
//
//            // Request permission
//            ActivityCompat.requestPermissions(
//                this,
//                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
//                LOCATION_PERMISSION_REQUEST_CODE
//            )
//            return
//        }
//
//        // Permission is granted, get the current location
//        fusedLocationClient.lastLocation
//            .addOnSuccessListener(this, OnSuccessListener { location ->
//                if (location != null) {
//                    val currentLocation = "${location.latitude}, ${location.longitude}"
//                    callback(currentLocation) // Pass the location to the callback
//                } else {
//                    callback(null) // Return null if location is not available
//                }
//            })
//    }
//
//    private fun sendToBackend(currentLocation: String, terminal: String) {
//        // Example of sending data to backend (e.g., through Retrofit or API call)
//        Log.d("TerminalSelectionActivity", "Going to Maps")
//        val intent = Intent(this, MapsActivity::class.java)
//        intent.putExtra("origin", currentLocation)
//        intent.putExtra("destination", terminal)
//        startActivity(intent)
//    }
////
//}