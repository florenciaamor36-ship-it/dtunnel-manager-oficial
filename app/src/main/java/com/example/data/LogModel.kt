package com.example.data

data class LogEntry(
    val timestamp: Long = System.currentTimeMillis(),
    val message: String,
    val type: LogType = LogType.INFO
)

enum class LogType {
    INFO, SUCCESS, PAYLOAD, PROXY, SSH, ERROR
}
