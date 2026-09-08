package com.myscratch.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.myscratch.app.data.local.entity.VaultItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VaultDao {
    @Query("SELECT * FROM vault_items WHERE userId = :userId ORDER BY updatedAt DESC")
    fun getVaultItems(userId: String): Flow<List<VaultItemEntity>>

    @Query("SELECT * FROM vault_items WHERE userId = :userId AND category = :category ORDER BY updatedAt DESC")
    fun getVaultItemsByCategory(userId: String, category: String): Flow<List<VaultItemEntity>>

    @Query("SELECT * FROM vault_items WHERE id = :id LIMIT 1")
    suspend fun getVaultItemById(id: String): VaultItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVaultItem(item: VaultItemEntity)

    @Update
    suspend fun updateVaultItem(item: VaultItemEntity)

    @Query("DELETE FROM vault_items WHERE id = :id")
    suspend fun deleteVaultItem(id: String)
}
