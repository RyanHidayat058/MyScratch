package com.myscratch.app.data.repository

import com.myscratch.app.data.local.dao.VaultDao
import com.myscratch.app.data.local.entity.VaultItemEntity
import com.myscratch.app.data.network.ApiClient
import com.myscratch.app.data.network.TokenManager
import com.myscratch.app.data.network.dto.VaultItemRequestDto
import com.myscratch.app.domain.model.VaultItem
import com.myscratch.app.domain.repository.VaultRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class VaultRepositoryImpl(
    private val vaultDao: VaultDao,
    private val tokenManager: TokenManager
) : VaultRepository {

    private val apiService = ApiClient.getService(tokenManager)

    override fun getVaultItems(userId: String): Flow<List<VaultItem>> {
        return vaultDao.getVaultItems(userId).map { list -> list.map { it.toDomain() } }
    }

    override fun getVaultItemsByCategory(userId: String, category: String): Flow<List<VaultItem>> {
        return vaultDao.getVaultItemsByCategory(userId, category).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun refreshVaultItems(userId: String) = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getVaultItems()
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.items?.forEach { dto ->
                    vaultDao.insertVaultItem(
                        VaultItemEntity(
                            id = dto.id.toString(),
                            userId = userId,
                            category = dto.category,
                            title = dto.title,
                            username = dto.username,
                            password = dto.password,
                            extraData = dto.extraData,
                            notes = dto.notes,
                            createdAt = System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                }
            }
        } catch (_: Exception) {}
        Unit
    }

    override suspend fun getVaultItemById(id: String): VaultItem? =
        withContext(Dispatchers.IO) {
            vaultDao.getVaultItemById(id)?.toDomain()
        }

    override suspend fun insertVaultItem(item: VaultItem) =
        withContext(Dispatchers.IO) {
            vaultDao.insertVaultItem(VaultItemEntity.fromDomain(item))
            try {
                apiService.createVaultItem(
                    VaultItemRequestDto(
                        category = item.category,
                        title = item.title,
                        username = item.username,
                        password = item.password,
                        extraData = item.extraData,
                        notes = item.notes
                    )
                )
            } catch (_: Exception) {}
            Unit
        }

    override suspend fun updateVaultItem(item: VaultItem) =
        withContext(Dispatchers.IO) {
            vaultDao.updateVaultItem(VaultItemEntity.fromDomain(item))
            try {
                apiService.updateVaultItem(
                    item.id,
                    VaultItemRequestDto(
                        category = item.category,
                        title = item.title,
                        username = item.username,
                        password = item.password,
                        extraData = item.extraData,
                        notes = item.notes
                    )
                )
            } catch (_: Exception) {}
            Unit
        }

    override suspend fun deleteVaultItem(id: String) =
        withContext(Dispatchers.IO) {
            vaultDao.deleteVaultItem(id)
            try {
                apiService.deleteVaultItem(id)
            } catch (_: Exception) {}
            Unit
        }
}
