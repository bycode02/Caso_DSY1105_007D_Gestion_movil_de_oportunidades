package com.example.opportunitymanagement.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "credentials",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userId"], unique = true)
    ]
)
data class CredentialEntity(
    @PrimaryKey(autoGenerate = true)
    val id : Long = 0,
    val userId : Long,
    val passwordHash : String,
    val passwordSalt : String
)