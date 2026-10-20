package com.example.opportunitymanagement.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface CredentialDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCredential(
        credential: CredentialEntity
    ): Long

    @Query(
        """
        SELECT * FROM credentials
        WHERE userId = :userId
        LIMIT 1
        """
    )
    suspend fun findByUserId(userId: Long): CredentialEntity?

    @Update
    suspend fun updateCredential(
        credential: CredentialEntity
    ): Int

    @Query(
        """
        DELETE FROM credentials
        WHERE userId = :userId
        """
    )
    suspend fun deleteByUserId(userId: Long): Int
}
