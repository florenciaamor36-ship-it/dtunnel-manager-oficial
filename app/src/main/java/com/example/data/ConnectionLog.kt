package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "connection_logs")
data class ConnectionLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val profileId: Long? = null,
    val username: String = "",
    val protocol: String = "SSH",
    val host: String = "",
    val status: String = "Failure",
    val detail: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
