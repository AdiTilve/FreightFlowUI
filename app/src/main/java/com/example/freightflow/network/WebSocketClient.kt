package com.example.freightflow.network

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.freightflow.model.Assignment
import com.google.gson.Gson
import io.reactivex.disposables.CompositeDisposable
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent
import java.util.concurrent.TimeUnit

class WebSocketClient(private val serverUrl: String = "ws://10.0.2.2:8080/ws") {

    // Callback properties instead of interface
    var onAssignmentUpdated: ((Assignment) -> Unit)? = null
    var onConnectionChanged: ((Boolean) -> Unit)? = null
    var onError: ((String) -> Unit)? = null

    private var stompClient: StompClient? = null
    private val disposables = CompositeDisposable()
    private val gson = Gson()
    private val mainHandler = Handler(Looper.getMainLooper())
    private var _isConnected = false

    val isConnected: Boolean
        get() = _isConnected

    fun connect() {
        if (stompClient?.isConnected == true) return

        try {
            stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, serverUrl)
            stompClient?.apply {
                disposables.add(
                    lifecycle().subscribe { event ->
                        when (event.type) {
                            LifecycleEvent.Type.OPENED -> {
                                _isConnected = true
                                Log.d(TAG, "WebSocket connected")
                                mainHandler.post {
                                    onConnectionChanged?.invoke(true)
                                }
                                setupSubscriptions()
                            }
                            LifecycleEvent.Type.ERROR -> {
                                _isConnected = false
                                Log.e(TAG, "WebSocket error: ${event.exception?.message}")
                                mainHandler.post {
                                    onError?.invoke(event.exception?.message ?: "Unknown error")
                                }
                                attemptReconnect()
                            }
                            LifecycleEvent.Type.CLOSED -> {
                                _isConnected = false
                                Log.w(TAG, "WebSocket closed")
                                mainHandler.post {
                                    onConnectionChanged?.invoke(false)
                                }
                            }
                            else -> {}
                        }
                    }
                )
                connect()
            }
        } catch (e: Exception) {
            Log.e(TAG, "WebSocket connection failed", e)
            mainHandler.post {
                onError?.invoke(e.message ?: "Connection failed")
            }
            attemptReconnect()
        }
    }

    private fun setupSubscriptions() {
        stompClient?.topic("/all/updateAssignment")?.subscribe(
            { message ->
                try {
                    val assignment = gson.fromJson(message.payload, Assignment::class.java)
                    mainHandler.post {
                        onAssignmentUpdated?.invoke(assignment)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse assignment", e)
                    mainHandler.post {
                        onError?.invoke("Failed to parse assignment data")
                    }
                }
            },
            { error ->
                Log.e(TAG, "Assignment subscription error", error)
                mainHandler.post {
                    onError?.invoke(error.message ?: "Subscription error")
                }
            }
        )?.let { disposables.add(it) }
    }

    private fun attemptReconnect() {
        Log.d(TAG, "Attempting to reconnect in 5 seconds...")
        disposables.clear()
        stompClient?.disconnect()

        mainHandler.postDelayed({
            connect()
        }, TimeUnit.SECONDS.toMillis(5))
    }

    fun disconnect() {
        _isConnected = false
        disposables.clear()
        stompClient?.disconnect()
        mainHandler.post {
            onConnectionChanged?.invoke(false)
        }
    }

    companion object {
        private const val TAG = "WebSocketClient"
    }
}