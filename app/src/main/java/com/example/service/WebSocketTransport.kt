package com.example.service

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.util.concurrent.TimeUnit

/** Real WebSocket transport. It reports protocol events; SSH/TUN bridging is owned by the engine. */
class WebSocketTransport(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .pingInterval(20, TimeUnit.SECONDS)
        .build()
) {
    interface Listener {
        fun onOpen(socket: WebSocket)
        fun onBytes(bytes: ByteArray)
        fun onClosed(reason: String)
        fun onFailure(error: Throwable)
    }

    fun connect(url: String, headers: Map<String, String>, listener: Listener): WebSocket {
        require(url.startsWith("ws://") || url.startsWith("wss://")) { "URL WebSocket inválida" }
        val request = Request.Builder().url(url).apply {
            headers.forEach { (name, value) -> if (name.isNotBlank()) header(name, value) }
        }.build()
        return client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) = listener.onOpen(webSocket)
            override fun onMessage(webSocket: WebSocket, bytes: ByteString) = listener.onBytes(bytes.toByteArray())
            override fun onMessage(webSocket: WebSocket, text: String) = listener.onBytes(text.toByteArray(Charsets.ISO_8859_1))
            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) = listener.onClosed("$code: $reason")
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) = listener.onFailure(t)
        })
    }
}
