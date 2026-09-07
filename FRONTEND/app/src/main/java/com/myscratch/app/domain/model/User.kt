package com.myscratch.app.domain.model

data class User(
    val id: String,
    val name: String,
    val email: String,
    val photoUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
