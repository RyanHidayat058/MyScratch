package com.myscratch.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.myscratch.app.domain.model.Transaction
import com.myscratch.app.domain.model.TransactionType

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val title: String,
    val amount: Double,
    val type: String, // "INCOME" or "EXPENSE"
    val category: String,
    val date: Long,
    val note: String = ""
) {
    fun toDomain(): Transaction = Transaction(
        id = id,
        userId = userId,
        title = title,
        amount = amount,
        type = try {
            TransactionType.valueOf(type)
        } catch (_: Exception) {
            if (type == "INCOME") TransactionType.INCOME else TransactionType.EXPENSE
        },
        category = category,
        date = date,
        note = note
    )

    companion object {
        fun fromDomain(transaction: Transaction): TransactionEntity = TransactionEntity(
            id = transaction.id,
            userId = transaction.userId,
            title = transaction.title,
            amount = transaction.amount,
            type = transaction.type.name,
            category = transaction.category,
            date = transaction.date,
            note = transaction.note
        )
    }
}
