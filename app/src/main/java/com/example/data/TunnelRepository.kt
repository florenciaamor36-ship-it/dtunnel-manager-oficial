package com.example.data

import kotlinx.coroutines.flow.Flow

class TunnelRepository(private val serverDao: ServerDao) {
    val allProfiles: Flow<List<ServerProfile>> = serverDao.getAllProfiles()
    val selectedProfile: Flow<ServerProfile?> = serverDao.getSelectedProfile()

    suspend fun insert(profile: ServerProfile): Long {
        val id = serverDao.insertProfile(profile)
        // If it's the first or marked selected, ensure selection
        return id
    }

    suspend fun update(profile: ServerProfile) {
        serverDao.updateProfile(profile)
    }

    suspend fun delete(profile: ServerProfile) {
        serverDao.deleteProfile(profile)
    }

    suspend fun selectProfile(profile: ServerProfile) {
        serverDao.clearSelection()
        serverDao.updateProfile(profile.copy(isSelected = true))
    }
}
