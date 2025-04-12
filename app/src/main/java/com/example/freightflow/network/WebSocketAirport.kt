package com.example.freightflow.network

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.freightflow.model.Flight
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import io.reactivex.disposables.CompositeDisposable
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent
import java.util.concurrent.TimeUnit

class WebSocketAirport(private val serverUrl: String = "ws://10.0.2.2:8080/ws") {

    // Callback properties with additional logging
    var onFlightUpdated: ((Flight) -> Unit)? = null
        set(value) {
            Log.d(TAG, "Flight update callback set")
            field = value
        }

    var onConnectionChanged: ((Boolean) -> Unit)? = null
    var onError: ((String) -> Unit)? = null

    private var stompClient: StompClient? = null
    private val disposables = CompositeDisposable()
    private val gson = Gson()
    private val mainHandler = Handler(Looper.getMainLooper())
    private var _isConnected = false
    private var reconnectAttempts = 0
    private val maxReconnectAttempts = 5

    val isConnected: Boolean
        get() = _isConnected

    fun connect() {
        if (stompClient?.isConnected == true) {
            Log.d(TAG, "Already connected, skipping reconnect")
            return
        }

        Log.d(TAG, "Attempting to connect to $serverUrl")
        try {
            stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, serverUrl).apply {
                disposables.add(
                    lifecycle().subscribe { event ->
                        Log.d(TAG, "Lifecycle event: ${event.type}")
                        when (event.type) {
                            LifecycleEvent.Type.OPENED -> handleConnected()
                            LifecycleEvent.Type.ERROR -> handleError(event.exception)
                            LifecycleEvent.Type.CLOSED -> handleDisconnected()
                            LifecycleEvent.Type.FAILED_SERVER_HEARTBEAT -> {
                                Log.w(TAG, "Server heartbeat failed")
                                attemptReconnect()
                            }
                            else -> {}
                        }
                    }
                )
                connect()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Initial connection failed", e)
            handleError(e)
        }
    }

    private fun handleConnected() {
        _isConnected = true
        reconnectAttempts = 0
        Log.d(TAG, "Successfully connected to flight WebSocket")
        mainHandler.post {
            onConnectionChanged?.invoke(true)
        }
        setupFlightSubscriptions()
    }

    private fun handleError(exception: Throwable?) {
        _isConnected = false
        val errorMsg = exception?.message ?: "Unknown error"
        Log.e(TAG, "WebSocket error: $errorMsg", exception)
        mainHandler.post {
            onError?.invoke(errorMsg)
            onConnectionChanged?.invoke(false)
        }
        attemptReconnect()
    }

    private fun handleDisconnected() {
        _isConnected = false
        Log.w(TAG, "WebSocket connection closed")
        mainHandler.post {
            onConnectionChanged?.invoke(false)
        }
    }

    private fun setupFlightSubscriptions() {
        Log.d(TAG, "Setting up flight subscriptions")

        // Primary flight updates channel
        subscribeToTopic("/all/updateFlight") { message ->
            try {
                Log.d(TAG, "Raw flight update: ${message.payload}")
                val flight = gson.fromJson(message.payload, Flight::class.java)
                Log.d(TAG, "Parsed flight: $flight")
                mainHandler.post {
                    onFlightUpdated?.invoke(flight)
                }
            } catch (e: JsonSyntaxException) {
                Log.e(TAG, "JSON parsing error", e)
                mainHandler.post {
                    onError?.invoke("Invalid flight data format")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Flight processing error", e)
            }
        }

        // Optional: Add heartbeat monitoring
        subscribeToTopic("/all/heartbeat") {
            Log.d(TAG, "Received heartbeat from server")
        }
    }

    private fun subscribeToTopic(topic: String, callback: (ua.naiksoftware.stomp.dto.StompMessage) -> Unit) {
        stompClient?.topic(topic)?.subscribe(
            { message ->
                Log.d(TAG, "Message received on $topic")
                callback(message)
            },
            { error ->
                Log.e(TAG, "Subscription error on $topic", error)
                mainHandler.post {
                    onError?.invoke("Subscription error: ${error.message}")
                }
            }
        )?.let { disposables.add(it) }
    }

    private fun attemptReconnect() {
        if (reconnectAttempts >= maxReconnectAttempts) {
            Log.w(TAG, "Max reconnect attempts reached ($maxReconnectAttempts)")
            return
        }

        reconnectAttempts++
        val delay = calculateReconnectDelay()
        Log.d(TAG, "Attempting reconnect #$reconnectAttempts in ${delay}ms")

        disposables.clear()
        stompClient?.disconnect()

        mainHandler.postDelayed({
            if (!isConnected) {
                connect()
            }
        }, delay)
    }

    private fun calculateReconnectDelay(): Long {
        return when (reconnectAttempts) {
            1 -> TimeUnit.SECONDS.toMillis(2)
            2 -> TimeUnit.SECONDS.toMillis(5)
            3 -> TimeUnit.SECONDS.toMillis(10)
            else -> TimeUnit.SECONDS.toMillis(15)
        }
    }

    fun disconnect() {
        Log.d(TAG, "Disconnecting WebSocket")
        _isConnected = false
        reconnectAttempts = 0
        disposables.clear()
        stompClient?.disconnect()
        mainHandler.post {
            onConnectionChanged?.invoke(false)
        }
    }

    companion object {
        private const val TAG = "WebSocketAirport"

        // For debugging WebSocket frames
        private val frameLogger = io.reactivex.functions.Consumer<String> { frame ->
            Log.v("$TAG-Frames", frame)
        }
    }
}