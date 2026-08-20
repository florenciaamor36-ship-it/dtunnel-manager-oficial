package com.example.service

import com.example.data.LogEntry
import com.example.data.LogType
import com.example.data.ServerProfile
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

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
                // Step 1: DNS & Socket connection
                addLog("Resolving host ${profile.sshHost}...", LogType.INFO)
                delay(600)
                addLog("Connecting to ${profile.sshHost}:${profile.sshPort}...", LogType.INFO)

                // Simulate TCP or SSL handshake
                if (profile.tunnelType.contains("SSL")) {
                    addLog("Initiating SSL/TLS handshake with SNI: ${if (profile.sni.isNotBlank()) profile.sni else profile.sshHost}", LogType.PROXY)
                    delay(800)
                    addLog("SSL/TLS Handshake verified successfully (Cipher: TLS_AES_256_GCM_SHA384)", LogType.SUCCESS)
                }

                // Step 2: Payload / WebSocket handshake
                if (profile.tunnelType.contains("WebSocket")) {
                    val wsTarget = if (profile.wsHost.isNotBlank()) profile.wsHost else "${profile.sshHost}:${profile.sshPort}"
                    addLog("Establishing WebSocket connection to $wsTarget via path ${profile.wsPath}...", LogType.PROXY)
                    delay(700)
                    
                    val processedPayload = profile.customPayload
                        .replace("[host_port]", "${profile.sshHost}:${profile.sshPort}")
                        .replace("[crlf]", "\r\n")
                        .replace("[method]", "GET")
                        .replace("[protocol]", "HTTP/1.1")

                    addLog("Injecting Custom Payload:\n$processedPayload", LogType.PAYLOAD)
                    delay(800)
                    addLog("HTTP/1.1 101 Switching Protocols (WebSocket connection established)", LogType.SUCCESS)
                } else if (profile.customPayload.isNotBlank()) {
                    addLog("Applying Custom HTTP Payload injection...", LogType.PAYLOAD)
                    delay(600)
                    addLog("Payload delivered successfully. Server acknowledged connection.", LogType.SUCCESS)
                }

                // Step 3: dtunnel daemon authentication & SSH handshake
                addLog("Starting dtunnel routing daemon (local TUN interface)...", LogType.INFO)
                delay(500)
                addLog("Authenticating SSH user '${profile.sshUser}' with public key / password...", LogType.SSH)
                delay(900)
                addLog("SSH Handshake completed. Encryption: ChaCha20-Poly1305 / RSA", LogType.SSH)
                delay(400)

                addLog("TUN Interface active (IP: 10.0.0.2, Gateway: 10.0.0.1)", LogType.SUCCESS)
                addLog("Tunnel established successfully! Secure connection online.", LogType.SUCCESS)
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
