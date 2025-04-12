package com.example.freightflow.utils

data class TrafficSignal(
    val signalId: String,
    val type: String,
    val duration: Int,
    val recommendedSpeedKmh: Double,
    val lat: Double,
    val lng: Double
)
