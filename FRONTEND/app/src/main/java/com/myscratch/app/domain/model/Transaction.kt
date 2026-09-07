package com.myscratch.app.domain.model

data class Transaction(
    val id: String,
    val userId: String,
    val title: String,
    val amount: Double,
    val type: TransactionType,
    val category: String,
    val date: Long = System.currentTimeMillis(),
    val note: String = ""
)
