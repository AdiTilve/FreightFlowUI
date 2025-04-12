package com.example.freightflow.model

data class Assignment(
    val assignmentNumber: String,
    val flightNumber: String,
    val flightStatus: String,
    val cargoType: String,
    val priorityLevel: String,
    val assignmentType: String,
    val assignmentStatus: String
)