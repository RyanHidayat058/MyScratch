package com.myscratch.app.domain.repository

import com.myscratch.app.domain.model.VaultItem
import kotlinx.coroutines.flow.Flow

interface VaultRepository {
    fun getVaultItems(userId: String): Flow<List<VaultItem>>
    fun getVaultItemsByCategory(userId: String, category: String): Flow<List<VaultItem>>
    suspend fun refreshVaultItems(userId: String)
    suspend fun getVaultItemById(id: String): VaultItem?
    suspend fun insertVaultItem(item: VaultItem)
    suspend fun updateVaultItem(item: VaultItem)
    suspend fun deleteVaultItem(id: String)
}
