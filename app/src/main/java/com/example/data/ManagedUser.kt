package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableNombre = "managed_users")
data class ManagedUser(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val username: String,
    val password: String,
    val hwid: String = "",
    val hwidRequired: Boolean = true,
    val expiresAt: String = "",
    val maxDevices: Int = 1,
    val protocols: String = "SSH",
    val status: String = "Active",
    val createdAt: Long = System.currentTimeMillis()
)
