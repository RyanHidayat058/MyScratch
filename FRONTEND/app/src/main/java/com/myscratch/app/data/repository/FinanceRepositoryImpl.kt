package com.myscratch.app.data.repository

import com.myscratch.app.data.local.dao.TransactionDao
import com.myscratch.app.data.local.entity.TransactionEntity
import com.myscratch.app.data.network.ApiClient
import com.myscratch.app.data.network.TokenManager
import com.myscratch.app.data.network.dto.TransactionRequestDto
import com.myscratch.app.domain.model.Transaction
import com.myscratch.app.domain.model.TransactionType
import com.myscratch.app.domain.repository.CategoryTotal
import com.myscratch.app.domain.repository.FinanceRepository
import com.myscratch.app.domain.repository.FinanceSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class FinanceRepositoryImpl(
    private val transactionDao: TransactionDao,
    private val tokenManager: TokenManager
) : FinanceRepository {

    private val apiService = ApiClient.getService(tokenManager)

    override fun getTransactions(userId: String): Flow<List<Transaction>> {
        return transactionDao.getTransactions(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getSummary(userId: String): Flow<FinanceSummary> {
        return transactionDao.getTransactions(userId).map { entities ->
            var income = 0.0
            var expense = 0.0
            var debtPayable = 0.0
            var debtReceivable = 0.0
            val expenseMap = mutableMapOf<String, Double>()
            val incomeMap = mutableMapOf<String, Double>()

            for (e in entities) {
                when (e.type) {
                    "INCOME" -> {
                        income += e.amount
                        incomeMap[e.category] = (incomeMap[e.category] ?: 0.0) + e.amount
                    }
                    "EXPENSE" -> {
                        expense += e.amount
                        expenseMap[e.category] = (expenseMap[e.category] ?: 0.0) + e.amount
                    }
                    "DEBT_PAYABLE" -> {
                        debtPayable += e.amount
                    }
                    "DEBT_RECEIVABLE" -> {
                        debtReceivable += e.amount
                    }
                    else -> {
                        expense += e.amount
                        expenseMap[e.category] = (expenseMap[e.category] ?: 0.0) + e.amount
                    }
                }
            }

            val expenseCategories = expenseMap.map { (cat, total) ->
                val pct = if (expense > 0) (total / expense).toFloat() else 0f
                CategoryTotal(category = cat, total = total, percentage = pct)
            }.sortedByDescending { it.total }

            val incomeCategories = incomeMap.map { (cat, total) ->
                val pct = if (income > 0) (total / income).toFloat() else 0f
                CategoryTotal(category = cat, total = total, percentage = pct)
            }.sortedByDescending { it.total }

            val currentCash = income - expense
            val expectedWorth = currentCash + debtReceivable - debtPayable

            FinanceSummary(
                totalBalance = currentCash,
                totalIncome = income,
                totalExpense = expense,
                totalDebtPayable = debtPayable,
                totalDebtReceivable = debtReceivable,
                expectedNetWorth = expectedWorth,
                expenseCategories = expenseCategories,
                incomeCategories = incomeCategories
            )
        }
    }

    override suspend fun refreshTransactions(userId: String) = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getTransactions()
            if (response.isSuccessful && response.body()?.success == true) {
                val list = response.body()?.transactions ?: emptyList()
                list.forEach { dto ->
                    val type = try {
                        TransactionType.valueOf(dto.type)
                    } catch (_: Exception) {
                        TransactionType.EXPENSE
                    }
                    val entity = TransactionEntity(
                        id = dto.id.toString(),
                        userId = userId,
                        title = dto.title,
                        amount = dto.amount,
                        type = type.name,
                        category = dto.category,
                        date = dto.date,
                        note = dto.note ?: ""
                    )
                    transactionDao.insertTransaction(entity)
                }
            }
        } catch (_: Exception) {}
        Unit
    }

    override suspend fun getTransactionById(id: String): Transaction? =
        withContext(Dispatchers.IO) {
            transactionDao.getTransactionById(id)?.toDomain()
        }

    override suspend fun insertTransaction(transaction: Transaction) =
        withContext(Dispatchers.IO) {
            transactionDao.insertTransaction(TransactionEntity.fromDomain(transaction))
            try {
                apiService.createTransaction(
                    TransactionRequestDto(
                        title = transaction.title,
                        amount = transaction.amount,
                        type = transaction.type.name,
                        category = transaction.category,
                        date = transaction.date,
                        note = transaction.note
                    )
                )
            } catch (_: Exception) {}
            Unit
        }

    override suspend fun updateTransaction(transaction: Transaction) =
        withContext(Dispatchers.IO) {
            transactionDao.updateTransaction(TransactionEntity.fromDomain(transaction))
            try {
                apiService.updateTransaction(
                    transaction.id,
                    TransactionRequestDto(
                        title = transaction.title,
                        amount = transaction.amount,
                        type = transaction.type.name,
                        category = transaction.category,
                        date = transaction.date,
                        note = transaction.note
                    )
                )
            } catch (_: Exception) {}
            Unit
        }

    override suspend fun deleteTransaction(transactionId: String) =
        withContext(Dispatchers.IO) {
            transactionDao.deleteTransaction(transactionId)
            try {
                apiService.deleteTransaction(transactionId)
            } catch (_: Exception) {}
            Unit
        }
}
