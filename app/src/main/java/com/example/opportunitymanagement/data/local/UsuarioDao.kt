package com.example.opportunitymanagement.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun inserUser(user : UsuarioEntity): Long
    @Query("SELECT * from usuario where id= :id LIMIT 1")
    suspend fun findById(id : Long): UsuarioEntity?
    @Query("SELECT * FROM usuario where email= :email LIMIT 1")
    suspend fun findbyEmail(email : String): UsuarioEntity?
    @Query("SELECT * from usuario order by nombre asc")
    fun observeUsers(): Flow<List<UsuarioEntity>>
    @Update
    suspend fun updateUser(user: UsuarioEntity): Int
    @Query("UPDATE usuario SET isActive = :active,updateAt = :updatedAt WHERE id = :id")
    suspend fun setUserActive(
        id: Long,
        active: Boolean,
        updatedAt: Long
    ): Int
}
