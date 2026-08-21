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
                _state.tryEmit(TunnelState.CONNECTED)
                addLog("Canal SSH mantenido activo.", LogType.SUCCESS)
                try { while (isActive && s.isConnected && c.isConnected) delay(5_000) }
                finally { c.disconnect(); s.disconnect(); channel = null; session = null }
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
