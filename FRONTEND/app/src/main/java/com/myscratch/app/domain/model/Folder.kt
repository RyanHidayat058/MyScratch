package com.myscratch.app.domain.model

data class Folder(
    val id: String,
    val userId: String,
    val name: String,
    val colorHex: String = "#38BDF8",
    val createdAt: Long = System.currentTimeMillis()
)
