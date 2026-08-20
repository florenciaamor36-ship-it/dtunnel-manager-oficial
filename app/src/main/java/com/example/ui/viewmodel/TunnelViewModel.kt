package com.example.ui.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.LogEntry
import com.example.data.ServerProfile
import com.example.data.TunnelRepository
import com.example.service.TunnelEngine
import com.example.service.TunnelService
import com.example.service.TunnelState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TunnelViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TunnelRepository

    val profiles: StateFlow<List<ServerProfile>>
    val selectedProfile: StateFlow<ServerProfile?>

    private val _tunnelState = MutableStateFlow(TunnelState.DISCONNECTED)
    val tunnelState: StateFlow<TunnelState> = _tunnelState

    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs

    init {
        val serverDao = AppDatabase.getDatabase(application).serverDao()
        repository = TunnelRepository(serverDao)

        profiles = repository.allProfiles.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        selectedProfile = repository.selectedProfile.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            null
        )

        // Observe TunnelEngine state and logs
        viewModelScope.launch {
            TunnelEngine.state.collect { state ->
                _tunnelState.value = state
            }
        }

        viewModelScope.launch {
            TunnelEngine.logs.collect { log ->
                val currentList = _logs.value.toMutableList()
                currentList.add(log)
                if (currentList.size > 200) currentList.removeAt(0)
                _logs.value = currentList
            }
        }

        // Add default profile if none exists
        viewModelScope.launch {
            repository.allProfiles.collect { list ->
                if (list.isEmpty()) {
                    val defaultProfile = ServerProfile(
                        name = "Free SG 01 - WebSocket SSH",
                        sshHost = "sg1.dtunnel.secure.net",
                        sshPort = 443,
                        sshUser = "nettunnel",
                        sshPass = "123456",
                        tunnelType = "SSH + WebSocket",
                        wsPath = "/ws-tunnel",
                        wsHeaders = "Host: sg1.dtunnel.secure.net\r\nUpgrade: websocket\r\nConnection: Upgrade",
                        sni = "speedtest.sg1.net",
                        customPayload = "GET /ws-tunnel HTTP/1.1[crlf]Host: sg1.dtunnel.secure.net[crlf]Upgrade: websocket[crlf]Connection: Upgrade[crlf][crlf]",
                        isSelected = true
                    )
                    val id = repository.insert(defaultProfile)
                    repository.selectProfile(defaultProfile.copy(id = id))
                }
            }
        }
    }

    fun startTunnel() {
        val profile = selectedProfile.value ?: profiles.value.firstOrNull() ?: return
        val context = getApplication<Application>()
        val intent = Intent(context, TunnelService::class.java).apply {
            action = TunnelService.ACTION_START
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
        TunnelEngine.startTunnel(profile, viewModelScope)
    }

    fun stopTunnel() {
        val context = getApplication<Application>()
        val intent = Intent(context, TunnelService::class.java).apply {
            action = TunnelService.ACTION_STOP
        }
        context.startService(intent)
    }

    fun saveProfile(profile: ServerProfile) {
        viewModelScope.launch {
            if (profile.id == 0L) {
                val id = repository.insert(profile)
                if (profile.isSelected) {
                    repository.selectProfile(profile.copy(id = id))
                }
            } else {
                repository.update(profile)
                if (profile.isSelected) {
                    repository.selectProfile(profile)
                }
            }
        }
    }

    fun deleteProfile(profile: ServerProfile) {
        viewModelScope.launch {
            repository.delete(profile)
        }
    }

    fun selectProfile(profile: ServerProfile) {
        viewModelScope.launch {
            repository.selectProfile(profile)
        }
    }

    fun clearLogs() {
        _logs.value = emptyList()
    }
}
