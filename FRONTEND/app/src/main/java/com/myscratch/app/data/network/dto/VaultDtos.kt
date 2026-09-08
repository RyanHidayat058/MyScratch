package com.myscratch.app.data.network.dto

import com.google.gson.annotations.SerializedName

data class VaultItemRequestDto(
    val category: String,
    val title: String,
    val username: String? = null,
    val password: String? = null,
    @SerializedName("extra_data")
    val extraData: String? = null,
    val notes: String? = null
)

data class VaultItemDto(
    val id: Any,
    val category: String = "account",
    val title: String,
    val username: String? = null,
    val password: String? = null,
    @SerializedName("extra_data")
    val extraData: String? = null,
    val notes: String? = null,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null
)

data class VaultItemsResponseDto(
    val success: Boolean,
    val items: List<VaultItemDto> = emptyList()
)

data class VaultItemMutationResponseDto(
    val success: Boolean,
    val message: String? = null,
    val item: VaultItemDto? = null
)
