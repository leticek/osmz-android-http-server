package com.smieja.osmzapp

import android.os.Environment
import android.util.Log
import android.webkit.MimeTypeMap
import java.io.File
import java.net.Socket
import java.sql.Timestamp
import java.time.Instant

private data class ResponseData(val statusCode: Int, val status: String, val content: File) {
    private val mimeType: String? =
        MimeTypeMap.getSingleton().getMimeTypeFromExtension(content.extension)
    val header: String =
        "HTTP/1.0 $statusCode $status\nContent-Type: $mimeType\nContent-Length: ${content.length()}\nDate: ${
            Timestamp.from(Instant.now())
        }\n\n"
}

private enum class RequestMethod {
    GET;
}

private object Routes {
    const val INDEX = "/"
    const val INDEX_PAGE = "/index.html"
    const val PAGE_1 = "/page1.html"
    const val PAGE_2 = "/page2.html"
    const val IMAGE_1 = "/android1.png"
    const val IMAGE_2 = "/android2.jpg"
    const val NOT_FOUND = "/404.html"
    const val FAVICON = "/favicon.ico"
}

class Responder {
    private val pathToResources = Environment.getExternalStorageDirectory().path + "/ServerAssets"

    fun processRequest(socket: Socket) {
        kotlin.runCatching {
            Header(socket.dumpRequest())
        }.onSuccess { validHttpHeader ->
            when (validHttpHeader.path) {
                Routes.INDEX, Routes.INDEX_PAGE -> {
                    socket respondWith ResponseData(
                        200,
                        "OK",
                        File("$pathToResources${Routes.INDEX_PAGE}")
                    )
                    return
                }
                Routes.PAGE_1 -> {
                    socket respondWith ResponseData(
                        200,
                        "OK",
                        File("$pathToResources${Routes.PAGE_1}")
                    )
                    return
                }
                Routes.PAGE_2 -> {
                    socket respondWith ResponseData(
                        200,
                        "OK",
                        File("$pathToResources${Routes.PAGE_2}")
                    )
                    return

                }
                Routes.IMAGE_1 -> {
                    socket respondWith ResponseData(
                        200,
                        "OK",
                        File("$pathToResources${Routes.IMAGE_1}")
                    )
                    return
                }
                Routes.IMAGE_2 -> {
                    socket respondWith ResponseData(
                        200,
                        "OK",
                        File("$pathToResources${Routes.IMAGE_2}")
                    )
                    return
                }
                Routes.FAVICON -> {
                    socket respondWith ResponseData(
                        200,
                        "OK",
                        File("$pathToResources${Routes.FAVICON}")
                    )
                    return
                }
                else -> {
                    socket respondWith ResponseData(
                        404,
                        "Not Found",
                        File("$pathToResources${Routes.NOT_FOUND}")
                    )
                    return
                }
            }
        }.onFailure {
            socket.close()
            Log.i("SERVER", "Socket Closed")
        }
    }

    private infix fun Socket.respondWith(responseData: ResponseData) {
        this.getOutputStream().run {
            this.write(responseData.header.toByteArray())
            this.write(responseData.content.readBytes())
            this.close()
        }
        this.close()
        Log.i("SERVER", "Socket Closed")
    }

    private fun Socket.dumpRequest(): String {
        var headerLine: String
        this.getInputStream().bufferedReader().let {
            var line: String
            headerLine = it.readLine()
            if (headerLine.isEmpty())
                return@let
            Log.i("REQUEST_DUMP", headerLine)
            while (true) {
                line = it.readLine()
                when (line.length) {
                    0 -> return@let
                    else -> Log.i("REQUEST_DUMP", line)
                }
            }
        }
        return headerLine
    }

    private class Header(headerLine: String) {
        private val method: RequestMethod
        val path: String

        init {
            headerLine.split(" ").let {
                method = RequestMethod.valueOf(it.component1())
                path = it.component2()
            }
            Log.i("REQUEST_INFO", method.toString())
            Log.i("REQUEST_INFO", path)
        }
    }
}


