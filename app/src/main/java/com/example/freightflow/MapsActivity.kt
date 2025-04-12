package com.example.freightflow
//
//import android.content.Intent
//import android.graphics.Bitmap
//import android.graphics.BitmapFactory
//import android.graphics.Color
//import android.os.Bundle
//import android.os.Handler
//import android.os.Looper
//import android.util.Log
//import android.view.Gravity
//import android.view.View
//import android.widget.Button
//import android.widget.TextView
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import com.example.freightflow.model.TrafficSignal
//import com.example.freightflow.network.RouteApiService
//import com.example.freightflow.network.RetrofitClient
//import com.example.freightflow.network.RouteRequest
//import com.example.freightflow.utils.RouteParser
//import com.example.freightflow.utils.RouteStep
//import com.google.android.gms.maps.*
//import com.google.android.gms.maps.model.*
//import com.google.maps.android.PolyUtil
//import okhttp3.ResponseBody
//import org.json.JSONObject
//import retrofit2.Call
//import retrofit2.Callback
//import retrofit2.Response
//import kotlin.math.*
//
//
//class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
//
//    private lateinit var mapView: MapView
//    private var mMap: GoogleMap? = null
//    private val originLatLng = LatLng(32.92999719901092, -97.04590528910143)
//    private lateinit var startNavigationButton: Button
//    private var polylinePoints: List<LatLng>? = null
//    private var truckCircle: Circle? = null
//    private var routeSteps: List<RouteStep> = emptyList()
//    private val signalMarkers = mutableListOf<Pair<LatLng, String>>() // For alert popups
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_maps)
//
//        mapView = findViewById(R.id.mapView)
//        mapView.onCreate(savedInstanceState)
//        mapView.getMapAsync(this)
//
//        startNavigationButton = findViewById(R.id.startNavigation)
//        startNavigationButton.setOnClickListener {
//            startNavigation()
//        }
//    }
//
//    override fun onMapReady(googleMap: GoogleMap?) {
//        mMap = googleMap
//        mMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
//
//        Handler().postDelayed({
//            val destination = intent.getStringExtra("destination")
//            val destinationLatLng = getTerminalCoordinates(destination)
//            if (destinationLatLng != null) {
//                mMap?.addMarker(MarkerOptions().position(originLatLng).title("Origin"))
//                mMap?.addMarker(MarkerOptions().position(destinationLatLng).title("Destination"))
//                mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(originLatLng, 12f))
//
//                sendToBackend(originLatLng, destination)
//            } else {
//                Toast.makeText(this, "Invalid destination terminal", Toast.LENGTH_SHORT).show()
//            }
//        }, 100)
//    }
//
//    private fun getTerminalCoordinates(terminal: String?): LatLng? {
//        val terminals = mapOf(
//            "Terminal A" to LatLng(32.90519931784437, -97.03620410500054),
//            "Terminal B" to LatLng(32.90525030865262, -97.04492894670216),
//            "Terminal C" to LatLng(32.89779743017013, -97.03563013684561),
//            "Terminal D" to LatLng(32.89803930764378, -97.04472216437125),
//            "Terminal E" to LatLng(32.8906161218135, -97.03586014583706)
//        )
//        return terminals[terminal]
//    }
//
//    private fun sendToBackend(originLatLng: LatLng, destination: String?) {
//        val destinationLatLng = getTerminalCoordinates(destination) ?: return
//        val service = RetrofitClient.retrofitInstance.create(RouteApiService::class.java)
//        val request = RouteRequest(
//            originLatLng.latitude, originLatLng.longitude,
//            destinationLatLng.latitude, destinationLatLng.longitude
//        )
//
//        service.createRoute(request).enqueue(object : Callback<ResponseBody> {
//            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
//                if (response.isSuccessful) {
//                    val json = response.body()?.string()
//                    if (json != null) {
//                        val jsonObject = JSONObject(json)
//                        routeSteps = RouteParser.parseRouteSteps(jsonObject)
//
//                        val fullPolyline = mutableListOf<LatLng>()
//                        val sampleSignalIds = listOf("S1", "S2", "S3") // Normally from backend
//
//                        for (step in routeSteps) {
//                            val points = decodePolyline(step.encodedPolyline)
//                            fullPolyline.addAll(points)
//
//                            mMap?.addPolyline(
//                                PolylineOptions().addAll(points).color(Color.BLUE).width(8f)
//                            )
//                        }
//
//                        polylinePoints = fullPolyline
//                        matchSignalsToStepsFromBackend(sampleSignalIds)
//                    }
//                }
//            }
//
//            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
//                Log.e("MapsActivity", "Network Error: ${t.message}")
//            }
//        })
//    }
//
//    private fun decodePolyline(encoded: String): List<LatLng> {
//        return PolyUtil.decode(encoded)
//    }
//
//    private fun matchSignalsToStepsFromBackend(signalIds: List<String>) {
//        val service = RetrofitClient.retrofitInstance.create(RouteApiService::class.java)
//        for (signalId in signalIds) {
//            service.getSignalById(signalId).enqueue(object : Callback<TrafficSignal> {
//                override fun onResponse(call: Call<TrafficSignal>, response: Response<TrafficSignal>) {
//                    if (response.isSuccessful) {
//                        response.body()?.let { signal ->
//                            matchSignalToStep(signal)
//                        }
//                    }
//                }
//                override fun onFailure(call: Call<TrafficSignal>, t: Throwable) {
//                    Log.e("SignalAPI", "Failed to fetch signal $signalId: ${t.message}")
//                }
//            })
//        }
//    }
//
//    private fun matchSignalToStep(signal: TrafficSignal) {
//        var closestStep: RouteStep? = null
//        var minDistance = Double.MAX_VALUE
//
//        for (step in routeSteps) {
//            val dist = haversineDistance(signal.lat, signal.lng, step.endLat, step.endLng)
//            if (dist < 50 && dist < minDistance) {
//                minDistance = dist
//                closestStep = step
//            }
//        }
//
//        closestStep?.let { step ->
//            val speedMph = (signal.recommendedSpeed * 0.621371).roundToInt()
//            val message = when (signal.type) {
//                "GREEN" -> "Maintain $speedMph mph to pass green in ${signal.duration}s"
//                "RED" -> "Red signal ahead. Wait ${signal.duration}s"
//                "YELLOW" -> "Caution: Yellow light"
//                else -> "Signal info"
//            }
//
//            mMap?.addPolyline(
//                PolylineOptions().addAll(decodePolyline(step.encodedPolyline)).color(Color.RED).width(12f)
//            )
//
//            mMap?.addMarker(
//                MarkerOptions()
//                    .position(LatLng(signal.lat, signal.lng))
//                    .title("Signal: ${signal.type}")
//                    .snippet(message)
//                    .icon(getSignalIcon())
//
//            )
//
//            signalMarkers.add(Pair(LatLng(signal.lat, signal.lng), message))
//
//            Log.d("SignalMatch", "Matched signal ${signal.signalId} to step: ${step.instructions}")
//        }
//    }
//    private fun getSignalIcon(): BitmapDescriptor {
//        val resId = resources.getIdentifier("traffic_signal", "drawable", packageName)
//        val bitmap = BitmapFactory.decodeResource(resources, resId)
//        val resized = Bitmap.createScaledBitmap(bitmap, 80, 80, false)
//        return BitmapDescriptorFactory.fromBitmap(resized)
//    }
//
//
//    private fun haversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
//        val R = 6371000.0
//        val dLat = Math.toRadians(lat2 - lat1)
//        val dLon = Math.toRadians(lon2 - lon1)
//        val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) *
//                cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
//        return 2 * R * atan2(sqrt(a), sqrt(1 - a))
//    }
//
//    private fun showSignalPopup(message: String) {
//        val inflater = layoutInflater
//        val layout = inflater.inflate(R.layout.custom_toast, null)
//
//        val text = layout.findViewById<TextView>(R.id.toast_message)
//        text.text = message
//
//        val toast = Toast(applicationContext)
//        toast.duration = Toast.LENGTH_LONG
//        toast.view = layout
//        toast.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 300)
//        toast.show()
//    }
//
//
//
//    private fun startNavigation() {
//        startNavigationButton.visibility = View.GONE
//        if (polylinePoints.isNullOrEmpty()) return
//
//        truckCircle = mMap?.addCircle(
//            CircleOptions()
//                .center(polylinePoints!![0])
//                .radius(10.0)
//                .strokeColor(Color.BLUE)
//                .fillColor(Color.BLUE)
//                .strokeWidth(2f)
//        )
//
//        val handler = Handler(Looper.getMainLooper())
//        var index = 0
//
//        handler.post(object : Runnable {
//            override fun run() {
//                if (index < polylinePoints!!.size) {
//                    val point = polylinePoints!![index]
//                    truckCircle?.center = point
//                    mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 17f))
//
//                    for ((signalLocation, message) in signalMarkers) {
//                        val dist = haversineDistance(
//                            point.latitude, point.longitude,
//                            signalLocation.latitude, signalLocation.longitude
//                        )
//                        if (dist < 400) {
//                            showSignalPopup(message)
//                            signalMarkers.remove(Pair(signalLocation, message))
//                            break
//                        }
//                    }
//
//                    index++
//                    handler.postDelayed(this, 200)
//                } else {
//                    showToastAndRedirect()
//                }
//            }
//        })
//    }
//
//    private fun showToastAndRedirect() {
//        val toast = Toast(applicationContext)
//        val layout = layoutInflater.inflate(R.layout.custom_toast, null)
//        toast.view = layout
//        toast.setGravity(Gravity.CENTER, 0, 0)
//        toast.show()
//
//        Handler(Looper.getMainLooper()).postDelayed({
//            if (!isFinishing) {
//                startActivity(Intent(this, AirportActivity::class.java))
//                finish()
//            }
//        }, 3000)
//    }
//
//    override fun onResume() { super.onResume(); mapView.onResume() }
//    override fun onPause() { super.onPause(); mapView.onPause() }
//    override fun onDestroy() { super.onDestroy(); mapView.onDestroy() }
//    override fun onLowMemory() { super.onLowMemory(); mapView.onLowMemory() }
//}
