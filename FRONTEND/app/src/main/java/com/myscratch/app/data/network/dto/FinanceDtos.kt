package com.myscratch.app.data.network.dto

import com.google.gson.annotations.SerializedName

data class TransactionRequestDto(
    val title: String,
    val amount: Double,
    val type: String,
    val category: String,
    val date: Long,
    val note: String? = null
)

data class TransactionDto(
    val id: Any,
    @SerializedName("user_id")
    val userId: Any? = null,
    val title: String,
    val amount: Double,
    val type: String,
    val category: String,
    val date: Long,
    val note: String? = null
)

data class TransactionsResponseDto(
    val success: Boolean,
    val transactions: List<TransactionDto> = emptyList()
)

data class TransactionMutationResponseDto(
    val success: Boolean,
    val message: String? = null,
    val transaction: TransactionDto? = null
)

data class FinanceSummaryResponseDto(
    val success: Boolean,
    val summary: SummaryDto? = null
)

data class SummaryDto(
    val totalBalance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val totalDebtPayable: Double = 0.0,
    val totalDebtReceivable: Double = 0.0,
    val expectedNetWorth: Double = 0.0,
    val categoryBreakdown: List<CategoryBreakdownDto> = emptyList()
)

data class CategoryBreakdownDto(
    val category: String,
    val total: Double,
    val percentage: Double
)
