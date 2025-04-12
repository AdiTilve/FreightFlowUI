package com.example.freightflow.network

import com.example.freightflow.model.Assignment
import com.example.freightflow.model.Flight
import com.example.freightflow.model.TrafficSignal
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface RouteApiService {
    @POST("api/routes/createRoute")
    fun createRoute(@Body request: RouteRequest): Call<ResponseBody>

    @GET("api/signals/{signalId}")
    fun getSignalById(@Path("signalId") signalId: String): Call<TrafficSignal>

    @GET("api/assignments") // Adjust the endpoint URL as needed
    fun getAssignments(): Call<Map<String, Assignment>>// Replace Assignment with your model
    @GET("api/assignments/{assignmentNumber}")
    fun getAssignment(@Path("assignmentNumber") assignmentId: String): Call<Assignment>

    @GET("flights/{flightNumber}")
    fun getFlight(@Path("flightNumber") flightNumber: String): Call<Flight>

}

