package com.example.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.WebSocket
import okio.ByteString
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.atomic.AtomicBoolean

/** Transport-only byte relay; callers provide the SSH/TUN streams. */
class WebSocketByteBridge(
    private val socket: WebSocket,
    private val input: InputStream,
    private val output: OutputStream,
    private val scope: CoroutineScope
) {
    private val closed = AtomicBoolean(false)
    private var reader: Job? = null

    fun start(): Job {
        reader = scope.launch(Dispatchers.IO) {
            val buffer = ByteArray(16 * 1024)
            try {
                while (isActive && !closed.get()) {
                    val count = input.read(buffer)
                    if (count < 0 || (count > 0 && !socket.send(ByteString.of(buffer.copyOf(count))))) break
                }
            } finally { close() }
        }
        return reader!!
    }

    fun onBytes(bytes: ByteArray) {
        if (!closed.get()) {
            output.write(bytes)
            output.flush()
        }
    }

    fun close() {
        if (closed.compareAndSet(false, true)) {
            reader?.cancel()
            runCatching { socket.close(1000, "bridge closed") }
            runCatching { input.close() }
            runCatching { output.close() }
        }
    }
}
