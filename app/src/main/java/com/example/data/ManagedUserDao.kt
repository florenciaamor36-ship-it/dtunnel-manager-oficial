package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ManagedUserDao {
    @Query("SELECT * FROM managed_users ORDER BY username COLLATE NOCASE")
    fun observeAll(): Flow<List<ManagedUser>>

    @Query("SELECT * FROM managed_users WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): ManagedUser?

    @Insert
    suspend fun insert(user: ManagedUser): Long

    @Update
    suspend fun update(user: ManagedUser)

    @Delete
    suspend fun delete(user: ManagedUser)

    @Query("UPDATE managed_users SET hwid = '' WHERE id = :id")
    suspend fun resetHwid(id: Long)

    @Query("UPDATE managed_users SET status = :status WHERE id = :id")
    suspend fun setStatus(id: Long, status: String)
}
