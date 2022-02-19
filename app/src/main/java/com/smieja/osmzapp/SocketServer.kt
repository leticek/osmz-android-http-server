package com.smieja.osmzapp

import android.content.Context
import android.util.Log
import java.net.ServerSocket
import java.net.Socket
import java.sql.Timestamp
import java.time.Instant

class SocketServer(context: Context, private val port: Int = 12345) {
    private val responseContent: String by lazy {
        context.assets.open("index.html").bufferedReader().readText()
    }

    private val response =
        "HTTP/1.0 200 OK\nContent-Type: text/html; charset=UTF-8\nContent-Length: ${responseContent.length}\nDate: ${
            Timestamp.from(Instant.now())
        }\n"

    private var serverSocket: ServerSocket? = null
    private var isRunning = false

    fun run() {
        when (isRunning) {
            true -> return
            false -> kotlin.runCatching {
                Log.i("SERVER", "Creating Socket")
                serverSocket = ServerSocket(port)
                isRunning = true
                while (isRunning) {
                    Log.i("SERVER", "Socket Waiting for connection")
                    val s = serverSocket!!.accept()
                    s.dumpRequest()
                    s.respond()
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

    private fun Socket.respond() {
        this.getOutputStream().bufferedWriter().use {
            it.write(response.addContent(responseContent))
        }
        this.close()
        Log.i("SERVER", "Socket Closed")
    }

    private fun Socket.dumpRequest() = this.getInputStream().bufferedReader().let {
        var line: String
        while (true) {
            line = it.readLine()
            when (line.length) {
                0 -> return@let
                else -> Log.i("REQUEST_DUMP", line)
            }
        }
    }

    private fun String.addContent(content: String) = this + "\n" + content
}
