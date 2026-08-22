package com.example.service

import com.example.data.LogEntry
import com.example.data.LogType
import com.example.data.ServerProfile
import com.jcraft.jsch.JSch
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

enum class TunnelState { DISCONNECTED, CONNECTING, CONNECTED, ERROR }

object TunnelEngine {
    private val _state = MutableSharedFlow<TunnelState>(replay = 1)
    val state: SharedFlow<TunnelState> = _state
    private val _logs = MutableSharedFlow<LogEntry>(replay = 50)
    val logs: SharedFlow<LogEntry> = _logs
    private var connectionJob: Job? = null
    private var session: com.jcraft.jsch.Session? = null
    private var channel: com.jcraft.jsch.Channel? = null

    init { _state.tryEmit(TunnelState.DISCONNECTED) }

    fun startTunnel(profile: ServerProfile, coroutineScope: CoroutineScope) {
        if (connectionJob?.isActive == true) return
        connectionJob = coroutineScope.launch(Dispatchers.IO) {
            _state.tryEmit(TunnelState.CONNECTING)
            try {
                require(profile.sshHost.isNotBlank()) { "El host está vacío" }
                require(profile.sshUser.isNotBlank()) { "El usuario está vacío" }
                require(profile.sshPort in 1..65535) { "Puerto inválido" }
                addLog("Abriendo sesión SSH real en ${profile.sshHost}:${profile.sshPort}...", LogType.SSH)
                val jsch = JSch()
                val s = jsch.getSession(profile.sshUser, profile.sshHost, profile.sshPort)
                s.setPassword(profile.sshPass)
                // Pending: expose and verify a server fingerprint in the profile before release.
                s.setConfig("StrictHostKeyChecking", "no")
                s.setConfig("PreferredAuthentications", "password,publickey")
                s.serverAliveInterval = 15_000
                s.serverAliveCountMax = 3
                s.connect(15_000)
                session = s
                addLog("Autenticación SSH aceptada.", LogType.SUCCESS)
                val c = s.openChannel("shell")
                c.connect(10_000)
                channel = c
                var ws: okhttp3.WebSocket? = null
                if (profile.tunnelType.contains("WebSocket", ignoreCase = true)) {
                    val host = profile.wsHost.ifBlank { profile.sshHost }
                    val scheme = if (profile.tunnelType.contains("SSL", ignoreCase = true)) "wss" else "ws"
                    val url = "$scheme://$host${profile.wsPath.ifBlank { "/ws" }}"
                    val headers = profile.wsHeaders.lines().mapNotNull { line ->
                        val separator = line.indexOf(':')
                        if (separator > 0) line.substring(0, separator).trim() to line.substring(separator + 1).trim() else null
                    }.toMap()
                    val opened = CompletableDeferred<okhttp3.WebSocket>()
                    var bridge: SshWebSocketBridge? = null
                    val transport = WebSocketTransport()
                    ws = transport.connect(url, headers, object : WebSocketTransport.Listener {
                        override fun onOpen(socket: okhttp3.WebSocket) { opened.complete(socket); addLog("Handshake WebSocket 101 aceptado.", LogType.SUCCESS) }
                        override fun onBytes(bytes: ByteArray) { bridge?.receive(bytes) }
                        override fun onClosed(reason: String) { addLog("WebSocket cerrado: $reason", LogType.INFO) }
                        override fun onFailure(error: Throwable) { opened.completeExceptionally(error) }
                    })
                    withTimeout(15_000) { opened.await() }
                    bridge = SshWebSocketBridge(c, ws!!, coroutineScope)
                    bridge.start(c.getInputStream())
                }
                _state.tryEmit(TunnelState.CONNECTED)
                addLog("Canal SSH mantenido activo.", LogType.SUCCESS)
                try { while (isActive && s.isConnected && c.isConnected) delay(5_000) }
                finally { ws?.close(1000, "stopping"); c.disconnect(); s.disconnect(); channel = null; session = null }
                if (isActive) _state.tryEmit(TunnelState.DISCONNECTED)
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) {
                addLog("Error SSH real: ${e.message ?: "fallo desconocido"}", LogType.ERROR)
                channel?.disconnect(); session?.disconnect(); channel = null; session = null
                _state.tryEmit(TunnelState.ERROR)
            }
        }
    }

    fun stopTunnel() {
        connectionJob?.cancel(); connectionJob = null
        channel?.disconnect(); session?.disconnect(); channel = null; session = null
        addLog("Túnel desconectado por el usuario.", LogType.INFO)
        _state.tryEmit(TunnelState.DISCONNECTED)
    }

    private fun addLog(message: String, type: LogType) { _logs.tryEmit(LogEntry(message = message, type = type)) }
}
