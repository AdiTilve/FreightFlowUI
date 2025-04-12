package com.example.freightflow.model

import java.sql.Date
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

data class Flight (
    val flightNumber: String,
    val arrivalAirport: String? = null,
    val destinationAirport: String? = null,
    val airlineName: String? = null,
    val state: String? = null,
    val terminal: String? = null,
    val landingTime: String? = null
){
    fun getFormattedLandingTime(): String {
        return try {
            // Parse ISO 8601 format (e.g., "2025-04-11T22:46:35.399+00:00")
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault())
            val date = isoFormat.parse(landingTime)

            // Format to just show time (HH:mm)
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            timeFormat.format(date)
        } catch (e: ParseException) {
            "N/A"  // Return default if parsing fails
        }
    }
}