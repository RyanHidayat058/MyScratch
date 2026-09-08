package com.myscratch.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.myscratch.app.domain.model.VaultItem

@Entity(tableName = "vault_items")
data class VaultItemEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val category: String,
    val title: String,
    val username: String? = null,
    val password: String? = null,
    val extraData: String? = null,
    val notes: String? = null,
    val createdAt: Long,
    val updatedAt: Long
) {
    fun toDomain(): VaultItem = VaultItem(
        id = id,
        userId = userId,
        category = category,
        title = title,
        username = username,
        password = password,
        extraData = extraData,
        notes = notes,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(item: VaultItem): VaultItemEntity = VaultItemEntity(
            id = item.id,
            userId = item.userId,
            category = item.category,
            title = item.title,
            username = item.username,
            password = item.password,
            extraData = item.extraData,
            notes = item.notes,
            createdAt = item.createdAt,
            updatedAt = item.updatedAt
        )
    }
}
