package com.myscratch.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.myscratch.app.domain.model.VaultCategory
import com.myscratch.app.domain.model.VaultItem
import com.myscratch.app.domain.repository.VaultRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.security.SecureRandom
import java.util.UUID

data class VaultUiState(
    val items: List<VaultItem> = emptyList(),
    val selectedCategory: String? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = false
)

class VaultViewModel(
    private val vaultRepository: VaultRepository,
    private val userId: String
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow<String?>(null)
    private val _searchQuery = MutableStateFlow("")

    val uiState: StateFlow<VaultUiState> = combine(
        vaultRepository.getVaultItems(userId),
        _selectedCategory,
        _searchQuery
    ) { items, category, query ->
        val filteredByCategory = if (category != null) {
            items.filter { it.category.equals(category, ignoreCase = true) }
        } else {
            items
        }

        val filtered = if (query.isNotBlank()) {
            filteredByCategory.filter {
                it.title.contains(query, ignoreCase = true) ||
                        (it.username?.contains(query, ignoreCase = true) == true) ||
                        (it.notes?.contains(query, ignoreCase = true) == true)
            }
        } else {
            filteredByCategory
        }

        VaultUiState(
            items = filtered,
            selectedCategory = category,
            searchQuery = query
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = VaultUiState()
    )

    init {
        refreshItems()
    }

    fun refreshItems() {
        viewModelScope.launch {
            vaultRepository.refreshVaultItems(userId)
        }
    }

    fun selectCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addVaultItem(
        category: String,
        title: String,
        username: String?,
        password: String?,
        extraData: String?,
        notes: String?,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val item = VaultItem(
                id = UUID.randomUUID().toString(),
                userId = userId,
                category = category,
                title = title.trim(),
                username = username?.trim(),
                password = password,
                extraData = extraData?.trim(),
                notes = notes?.trim(),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            vaultRepository.insertVaultItem(item)
            onSuccess()
        }
    }

    fun updateVaultItem(
        item: VaultItem,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            vaultRepository.updateVaultItem(item.copy(updatedAt = System.currentTimeMillis()))
            onSuccess()
        }
    }

    fun deleteVaultItem(id: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            vaultRepository.deleteVaultItem(id)
            onSuccess()
        }
    }

    fun generateStrongPassword(
        length: Int = 16,
        includeUpper: Boolean = true,
        includeLower: Boolean = true,
        includeNumbers: Boolean = true,
        includeSymbols: Boolean = true
    ): String {
        val upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val lower = "abcdefghijklmnopqrstuvwxyz"
        val numbers = "0123456789"
        val symbols = "!@#$%^&*()_+-=[]{}|;:,.<>?"

        val charPool = buildString {
            if (includeUpper) append(upper)
            if (includeLower) append(lower)
            if (includeNumbers) append(numbers)
            if (includeSymbols) append(symbols)
        }

        if (charPool.isEmpty()) return ""

        val random = SecureRandom()
        val password = StringBuilder(length)
        for (i in 0 until length) {
            val index = random.nextInt(charPool.length)
            password.append(charPool[index])
        }
        return password.toString()
    }

    companion object {
        fun provideFactory(
            vaultRepository: VaultRepository,
            userId: String
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return VaultViewModel(vaultRepository, userId) as T
            }
        }
    }
}
