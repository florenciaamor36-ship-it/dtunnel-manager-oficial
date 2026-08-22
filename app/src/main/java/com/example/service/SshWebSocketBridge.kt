package com.example.service

import com.jcraft.jsch.Channel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.WebSocket
import okio.ByteString
import java.io.InputStream
import java.util.concurrent.atomic.AtomicBoolean

class SshWebSocketBridge(
    private val channel: Channel,
    private val socket: WebSocket,
    private val scope: CoroutineScope
) {
    private val stopped = AtomicBoolean(false)
    private var reader: Job? = null

    fun start(input: InputStream) {
        reader = scope.launch(Dispatchers.IO) {
            val buffer = ByteArray(16 * 1024)
            try {
                while (isActive && !stopped.get() && channel.isConnected) {
                    val count = input.read(buffer)
                    if (count < 0) break
                    if (count > 0 && !socket.send(ByteString.of(buffer.copyOf(count)))) break
                }
            } finally { close() }
        }
    }

    fun receive(bytes: ByteArray) {
        if (stopped.get()) return
        val output = channel.getOutputStream()
        output.write(bytes)
        output.flush()
    }

    fun close() {
        if (stopped.compareAndSet(false, true)) {
            reader?.cancel()
            runCatching { socket.close(1000, "bridge closed") }
            runCatching { channel.disconnect() }
        }
    }
}
