package com.smieja.osmzapp

import android.util.Log
import java.net.ServerSocket


class SocketServer(private val port: Int = 12345) {
    private var serverSocket: ServerSocket? = null
    private var isRunning = false
    private val responder: Responder by lazy {
        Responder()
    }

    fun run() {
        when (isRunning) {
            true -> return
            false -> kotlin.runCatching {
                Log.i("SERVER", "Creating Socket")
                serverSocket = ServerSocket(port)
                isRunning = true
                while (isRunning) {
                    Log.i("SERVER", "Socket Waiting for connection")
                    responder.processRequest(serverSocket!!.accept())
                }
            }.exceptionOrNull().let {
                when (serverSocket?.isClosed) {
                    true -> Log.i("SERVER", "Socket exited gracefully")
                    else -> {
                        Log.i("SERVER", "Socket exit error")
                        it?.printStackTrace()
                    }
                }
            }
        }
    }

    fun close() {
        kotlin.runCatching {
            serverSocket?.close()
        }.exceptionOrNull()?.let {
            Log.i("SERVER", "Error, probably interrupted in accept(), see log")
            it.printStackTrace()
        }
        isRunning = false
    }
}
