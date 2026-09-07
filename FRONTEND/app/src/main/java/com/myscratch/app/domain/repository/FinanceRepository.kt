package com.myscratch.app.domain.repository

import com.myscratch.app.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

data class CategoryTotal(
    val category: String,
    val total: Double,
    val percentage: Float = 0f
)

data class FinanceSummary(
    val totalBalance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val totalDebtPayable: Double = 0.0,
    val totalDebtReceivable: Double = 0.0,
    val expectedNetWorth: Double = 0.0,
    val expenseCategories: List<CategoryTotal> = emptyList(),
    val incomeCategories: List<CategoryTotal> = emptyList()
)

interface FinanceRepository {
    fun getTransactions(userId: String): Flow<List<Transaction>>
    fun getSummary(userId: String): Flow<FinanceSummary>
    suspend fun refreshTransactions(userId: String)
    suspend fun getTransactionById(id: String): Transaction?
    suspend fun insertTransaction(transaction: Transaction)
    suspend fun updateTransaction(transaction: Transaction)
    suspend fun deleteTransaction(transactionId: String)
}
