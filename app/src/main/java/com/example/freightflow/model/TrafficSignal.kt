package com.example.freightflow.model

data class TrafficSignal(
    val signalId: String,
    val type: String,
    val duration: Int,
    val recommendedSpeed: Double,
    val lat: Double,
    val lng: Double
)
