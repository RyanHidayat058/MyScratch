package com.myscratch.app.domain.model

data class Note(
    val id: String,
    val userId: String,
    val folderId: String,
    val title: String,
    val content: String,
    val updatedAt: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)
