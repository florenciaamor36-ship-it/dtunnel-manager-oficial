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
import org.json.JSONArray
import org.json.JSONObject

class TunnelViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TunnelRepository
    private val serverDao: com.example.data.ServerDao
    private val userDao: com.example.data.ManagedUserDao

    val profiles: StateFlow<List<ServerProfile>>
    val selectedProfile: StateFlow<ServerProfile?>
    val managedUsers: StateFlow<List<com.example.data.ManagedUser>>

    private val _tunnelState = MutableStateFlow(TunnelState.DISCONNECTED)
    val tunnelState: StateFlow<TunnelState> = _tunnelState

    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs

    init {
        serverDao = AppDatabase.getDatabase(application).serverDao()
        repository = TunnelRepository(serverDao)
        userDao = AppDatabase.getDatabase(application).managedUserDao()

        managedUsers = userDao.observeAll().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

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
                        sshHost = "",
                        sshPort = 22,
                        sshUser = "",
                        sshPass = "",
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

    fun updateProfile(profile: ServerProfile) {
        viewModelScope.launch { repository.update(profile) }
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

    fun saveManagedUser(user: com.example.data.ManagedUser) {
        viewModelScope.launch { if (user.id == 0L) userDao.insert(user) else userDao.update(user) }
    }

    fun deleteManagedUser(user: com.example.data.ManagedUser) {
        viewModelScope.launch { userDao.delete(user) }
    }

    fun resetUserHwid(user: com.example.data.ManagedUser) {
        viewModelScope.launch { userDao.resetHwid(user.id) }
    }

    fun setUserStatus(user: com.example.data.ManagedUser, status: String) {
        viewModelScope.launch { userDao.setStatus(user.id, status) }
    }

    fun clearLogs() {
        _logs.value = emptyList()
    }

    fun exportSnapshot(): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("profiles", JSONArray(profiles.value.map { p -> JSONObject().apply {
            put("name", p.name); put("sshHost", p.sshHost); put("sshPort", p.sshPort); put("sshUser", p.sshUser)
            put("tunnelType", p.tunnelType); put("wsHost", p.wsHost); put("wsPath", p.wsPath); put("wsHeaders", p.wsHeaders)
            put("sni", p.sni); put("customPayload", p.customPayload); put("isSelected", p.isSelected)
        } }))
        root.put("users", JSONArray(managedUsers.value.map { u -> JSONObject().apply {
            put("username", u.username); put("hwid", u.hwid); put("hwidRequired", u.hwidRequired); put("expiresAt", u.expiresAt)
            put("maxDevices", u.maxDevices); put("protocols", u.protocols); put("status", u.status); put("createdAt", u.createdAt)
        } }))
        return root.toString(2)
    }

    fun importSnapshot(text: String) {
        viewModelScope.launch {
            val root = JSONObject(text)
            root.optJSONArray("profiles")?.let { array -> for (i in 0 until array.length()) {
                val p = array.getJSONObject(i)
                serverDao.insertProfile(ServerProfile(name = p.optString("name"), sshHost = p.optString("sshHost"), sshPort = p.optInt("sshPort", 22), sshUser = p.optString("sshUser"), sshPass = "", tunnelType = p.optString("tunnelType", "SSH + WebSocket"), wsHost = p.optString("wsHost"), wsPath = p.optString("wsPath", "/ws"), wsHeaders = p.optString("wsHeaders"), sni = p.optString("sni"), customPayload = p.optString("customPayload"), isSelected = p.optBoolean("isSelected", false)))
            } }
            root.optJSONArray("users")?.let { array -> for (i in 0 until array.length()) {
                val u = array.getJSONObject(i)
                userDao.insert(com.example.data.ManagedUser(username = u.optString("username"), password = "", hwid = u.optString("hwid"), hwidRequired = u.optBoolean("hwidRequired", true), expiresAt = u.optString("expiresAt"), maxDevices = u.optInt("maxDevices", 1), protocols = u.optString("protocols", "SSH"), status = u.optString("status", "Active"), createdAt = u.optLong("createdAt", System.currentTimeMillis())))
            } }
        }
    }
}
