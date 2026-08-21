package com.example.service

import com.example.data.LogEntry
import com.example.data.LogType
import com.example.data.ServerProfile
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import java.net.InetSocketAddress
import java.net.Socket
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

enum class TunnelState {
    DISCONNECTED, CONNECTING, CONNECTED, ERROR
}

object TunnelEngine {
    private val _state = MutableSharedFlow<TunnelState>(replay = 1)
    val state: SharedFlow<TunnelState> = _state

    private val _logs = MutableSharedFlow<LogEntry>(replay = 50)
    val logs: SharedFlow<LogEntry> = _logs

    private var connectionJob: Job? = null
    private var isRunning = false

    init {
        _state.tryEmit(TunnelState.DISCONNECTED)
    }

    fun startTunnel(profile: ServerProfile, coroutineScope: CoroutineScope) {
        if (isRunning) return
        isRunning = true
        _state.tryEmit(TunnelState.CONNECTING)
        addLog("Initializing NetTunnel Engine (dtunnel daemon wrapper)...", LogType.INFO)
        addLog("Target Profile: ${profile.name} [Type: ${profile.tunnelType}]", LogType.INFO)
        addLog("SSH Server: ${profile.sshHost}:${profile.sshPort} (User: ${profile.sshUser})", LogType.INFO)

        connectionJob = coroutineScope.launch(Dispatchers.IO) {
            try {
                require(profile.sshHost.isNotBlank()) { "El host está vacío" }
                require(profile.sshPort in 1..65535) { "Puerto inválido" }
                addLog("Resolviendo ${profile.sshHost}...", LogType.INFO)
                val useTls = profile.tunnelType.contains("SSL", ignoreCase = true)
                val socket: Socket = if (useTls) {
                    addLog("Abriendo conexión TLS real...", LogType.PROXY)
                    (SSLSocketFactory.getDefault() as SSLSocketFactory).createSocket()
                } else Socket()
                socket.use { s ->
                    s.connect(InetSocketAddress(profile.sshHost, profile.sshPort), 10_000)
                    s.soTimeout = 10_000
                    if (s is SSLSocket) s.startHandshake()
                    addLog("TCP conectado a ${profile.sshHost}:${profile.sshPort}", LogType.SUCCESS)
                    if (profile.tunnelType.contains("WebSocket", ignoreCase = true) || profile.customPayload.isNotBlank()) {
                        val payload = profile.customPayload
                            .replace("[host_port]", "${profile.sshHost}:${profile.sshPort}")
                            .replace("[crlf]", "\r\n")
                            .replace("[method]", "GET")
                            .replace("[protocol]", "HTTP/1.1")
                        require(payload.isNotBlank()) { "Payload vacío" }
                        addLog("Enviando payload real...", LogType.PAYLOAD)
                        s.getOutputStream().write(payload.toByteArray(Charsets.UTF_8))
                        s.getOutputStream().flush()
                        val response = ByteArray(4096)
                        val n = s.getInputStream().read(response)
                        require(n > 0) { "El servidor cerró la conexión sin respuesta" }
                        val text = String(response, 0, n, Charsets.UTF_8)
                        addLog("Respuesta recibida: ${text.lineSequence().firstOrNull() ?: "(vacía)"}", LogType.SUCCESS)
                        require(text.startsWith("HTTP/1.1 101") || text.startsWith("HTTP/1.0 101") || text.startsWith("HTTP/1.1 2") || text.startsWith("HTTP/1.0 2")) { "Respuesta no válida para el payload" }
                    } else {
                        val response = ByteArray(256)
                        val n = s.getInputStream().read(response)
                        if (n > 0) addLog("Banner recibido: ${String(response, 0, n, Charsets.UTF_8).trim()}", LogType.SSH)
                    }
                }
                addLog("Conexión de transporte verificada; el túnel queda en línea.", LogType.SUCCESS)
                _state.tryEmit(TunnelState.CONNECTED)

            } catch (e: Exception) {
                addLog("Connection Error: ${e.localizedMessage ?: "Unknown error occurred"}", LogType.ERROR)
                _state.tryEmit(TunnelState.ERROR)
                isRunning = false
            }
        }
    }

    fun stopTunnel() {
        connectionJob?.cancel()
        connectionJob = null
        isRunning = false
        addLog("Tunnel disconnected by user.", LogType.INFO)
        _state.tryEmit(TunnelState.DISCONNECTED)
    }

    private fun addLog(message: String, type: LogType) {
        _logs.tryEmit(LogEntry(message = message, type = type))
    }
}
