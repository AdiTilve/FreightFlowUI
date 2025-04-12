package com.example.freightflow.utils

import org.json.JSONObject
import com.google.android.gms.maps.model.LatLng
import kotlin.math.*

data class RouteStep(
    val startLat: Double,
    val startLng: Double,
    val endLat: Double,
    val endLng: Double,
    val distanceMeters: Int,
    val durationSeconds: Int,
    val instructions: String,
    val encodedPolyline: String
)

object RouteParser {
    fun parseRouteSteps(json: JSONObject): List<RouteStep> {
        val stepsList = mutableListOf<RouteStep>()

        val routes = json.getJSONArray("routes")
        if (routes.length() == 0) return stepsList

        val legs = routes.getJSONObject(0).getJSONArray("legs")
        val steps = legs.getJSONObject(0).getJSONArray("steps")

        for (i in 0 until steps.length()) {
            val step = steps.getJSONObject(i)

            val startLoc = step.getJSONObject("start_location")
            val endLoc = step.getJSONObject("end_location")
            val distance = step.getJSONObject("distance")
            val duration = step.getJSONObject("duration")
            val polyline = step.getJSONObject("polyline").getString("points")
            val instructions = step.getString("html_instructions")

            stepsList.add(
                RouteStep(
                    startLat = startLoc.getDouble("lat"),
                    startLng = startLoc.getDouble("lng"),
                    endLat = endLoc.getDouble("lat"),
                    endLng = endLoc.getDouble("lng"),
                    distanceMeters = distance.getInt("value"),
                    durationSeconds = duration.getInt("value"),
                    instructions = instructions,
                    encodedPolyline = polyline
                )
            )
        }

        return stepsList
    }
    fun haversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371000.0 // Earth radius in meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }
}
