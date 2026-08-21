package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableNombre = "server_profiles")
data class ServerProfile(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val sshHost: String,
    val sshPort: Int = 22,
    val sshUser: String,
    val sshPass: String,
    val tunnelType: String = "SSH + WebSocket", // "SSH Direct", "SSH + WebSocket", "SSL + SSH", "SSL + WebSocket"
    val wsHost: String = "",
    val wsPath: String = "/ws",
    val wsHeaders: String = "Host: [host_port]\r\nUpgrade: websocket\r\nConnection: Upgrade",
    val sni: String = "",
    val customPayload: String = "GET / HTTP/1.1[crlf]Host: [host_port][crlf]Upgrade: websocket[crlf][crlf]",
    val isSelected: Boolean = false
)
