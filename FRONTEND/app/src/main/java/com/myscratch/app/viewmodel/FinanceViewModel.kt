package com.myscratch.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.myscratch.app.domain.model.Transaction
import com.myscratch.app.domain.model.TransactionType
import com.myscratch.app.domain.repository.FinanceRepository
import com.myscratch.app.domain.repository.FinanceSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID

enum class TimePeriod(val title: String) {
    ALL("Semua"),
    THIS_MONTH("Bulan Ini"),
    THIS_WEEK("Minggu Ini"),
    TODAY("Hari Ini")
}

data class FinanceUiState(
    val transactions: List<Transaction> = emptyList(),
    val summary: FinanceSummary = FinanceSummary(),
    val selectedPeriod: TimePeriod = TimePeriod.ALL,
    val isCalculatorOpen: Boolean = false,
    val pendingAmountFromCalc: Double? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class FinanceViewModel(
    private val financeRepository: FinanceRepository,
    private val userId: String
) : ViewModel() {

    private val _selectedPeriod = MutableStateFlow(TimePeriod.ALL)
    private val _isCalculatorOpen = MutableStateFlow(false)
    private val _pendingAmount = MutableStateFlow<Double?>(null)

    val uiState: StateFlow<FinanceUiState> = combine(
        financeRepository.getTransactions(userId),
        financeRepository.getSummary(userId),
        _selectedPeriod,
        _isCalculatorOpen,
        _pendingAmount
    ) { transactions, summary, period, isCalcOpen, pendingAmt ->
        val filtered = filterTransactionsByPeriod(transactions, period)
        FinanceUiState(
            transactions = filtered,
            summary = summary,
            selectedPeriod = period,
            isCalculatorOpen = isCalcOpen,
            pendingAmountFromCalc = pendingAmt
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FinanceUiState()
    )

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            financeRepository.refreshTransactions(userId)
        }
    }

    fun setPeriod(period: TimePeriod) {
        _selectedPeriod.value = period
    }

    fun openCalculator() {
        _isCalculatorOpen.value = true
    }

    fun closeCalculator() {
        _isCalculatorOpen.value = false
    }

    fun setAmountFromCalculator(amount: Double) {
        _pendingAmount.value = amount
        _isCalculatorOpen.value = false
    }

    fun consumePendingAmount() {
        _pendingAmount.value = null
    }

    fun addTransaction(
        title: String,
        amount: Double,
        type: TransactionType,
        category: String,
        note: String = "",
        date: Long = System.currentTimeMillis(),
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val transaction = Transaction(
                id = UUID.randomUUID().toString(),
                userId = userId,
                title = title,
                amount = amount,
                type = type,
                category = category,
                date = date,
                note = note
            )
            financeRepository.insertTransaction(transaction)
            onSuccess()
        }
    }

    fun updateTransaction(
        transaction: Transaction,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            financeRepository.updateTransaction(transaction)
            onSuccess()
        }
    }

    fun deleteTransaction(id: String) {
        viewModelScope.launch {
            financeRepository.deleteTransaction(id)
        }
    }

    private fun filterTransactionsByPeriod(
        transactions: List<Transaction>,
        period: TimePeriod
    ): List<Transaction> {
        val now = Calendar.getInstance()
        return when (period) {
            TimePeriod.ALL -> transactions
            TimePeriod.TODAY -> {
                transactions.filter {
                    val c = Calendar.getInstance().apply { timeInMillis = it.date }
                    c.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                            c.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)
                }
            }
            TimePeriod.THIS_WEEK -> {
                val weekAgo = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, -7)
                }.timeInMillis
                transactions.filter { it.date >= weekAgo }
            }
            TimePeriod.THIS_MONTH -> {
                transactions.filter {
                    val c = Calendar.getInstance().apply { timeInMillis = it.date }
                    c.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                            c.get(Calendar.MONTH) == now.get(Calendar.MONTH)
                }
            }
        }
    }

    companion object {
        fun provideFactory(
            financeRepository: FinanceRepository,
            userId: String
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return FinanceViewModel(financeRepository, userId) as T
            }
        }
    }
}
