package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ServerDao {
    @Query("SELECT * FROM server_profiles")
    fun getAllProfiles(): Flow<List<ServerProfile>>

    @Query("SELECT * FROM server_profiles WHERE isSelected = 1 LIMIT 1")
    fun getSelectedProfile(): Flow<ServerProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ServerProfile): Long

    @Update
    suspend fun updateProfile(profile: ServerProfile)

    @Delete
    suspend fun deleteProfile(profile: ServerProfile)

    @Query("UPDATE server_profiles SET isSelected = 0")
    suspend fun clearSelection()
}
