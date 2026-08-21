package com.example.service

import com.jcraft.jsch.Channel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.WebSocket
import java.io.InputStream
import java.util.concurrent.atomic.AtomicBoolean

/** Bridges bytes between a JSch channel and a WebSocket. No endpoint or credential is embedded. */
class SshWebSocketBridge(
    private val channel: Channel,
    private val socket: WebSocket,
    private val scope: CoroutineScope
) {
    private val closed = AtomicBoolean(false)
    private var readerJob: Job? = null

    fun start(input: InputStream) {
        readerJob = scope.launch(Dispatchers.IO) {
            val buffer = ByteArray(16 * 1024)
            try {
                while (isActive && !closed.get() && channel.isConnected) {
                    val count = input.read(buffer)
                    if (count < 0) break
                    if (count > 0 && !socket.send(okio.ByteString.of(buffer.copyOf(count)))) break
                }
            } finally { close() }
        }
    }

    fun onWebSocketBytes(bytes: ByteArray) {
        if (closed.get()) return
        val output = channel.outputStream
        output.write(bytes)
        output.flush()
    }

    fun close() {
        if (closed.compareAndSet(false, true)) {
            readerJob?.cancel()
            runCatching { socket.close(1000, "bridge closed") }
            runCatching { channel.disconnect() }
        }
    }
}
