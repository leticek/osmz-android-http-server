package com.smieja.osmzapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity() {
    private val socketServer: SocketServer by lazy {
        SocketServer()
    }
    private val scope = CoroutineScope(Dispatchers.IO)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btn1 = findViewById<Button>(R.id.button1)
        val btn2 = findViewById<Button>(R.id.button2)
        btn1.setOnClickListener {
            when (ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED) {
                true -> {
                    scope.launch {
                        socketServer.run()
                    }
                }
                else -> {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        READ_EXTERNAL_STORAGE
                    )
                }
            }
        }

        btn2.setOnClickListener {
            kotlin.runCatching {
                socketServer.close()
            }.exceptionOrNull()?.printStackTrace()
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            READ_EXTERNAL_STORAGE -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                runBlocking {
                    launch(Dispatchers.Default) {
                        socketServer.run()
                    }
                }
            }
        }
    }

    private fun startSocketServer() {
        socketServer

        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            socketServer.run()
        }
    }

    companion object {
        private const val READ_EXTERNAL_STORAGE = 1
    }
}