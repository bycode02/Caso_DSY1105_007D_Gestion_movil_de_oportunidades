package com.example.opportunitymanagement.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuario")
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true ) val id : Long,
    val nombre : String,
    val email : String,
    val role : Role,
    val isActive : Boolean,
    val createAt : Long,
    val updateAt : Long
)

