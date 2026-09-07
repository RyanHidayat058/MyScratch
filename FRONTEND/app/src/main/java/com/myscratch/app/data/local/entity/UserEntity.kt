package com.myscratch.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.myscratch.app.domain.model.User

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val email: String,
    val passwordHash: String = "",
    val photoUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): User = User(
        id = id,
        name = name,
        email = email,
        photoUrl = photoUrl,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(user: User, passwordHash: String = ""): UserEntity = UserEntity(
            id = user.id,
            name = user.name,
            email = user.email,
            passwordHash = passwordHash,
            photoUrl = user.photoUrl,
            createdAt = user.createdAt
        )
    }
}
