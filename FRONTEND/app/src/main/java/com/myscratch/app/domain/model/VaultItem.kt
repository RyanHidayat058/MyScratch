package com.myscratch.app.domain.model

enum class VaultCategory(val key: String, val title: String) {
    ACCOUNT("account", "Akun & Password"),
    BANK("bank", "Kartu & Bank"),
    IDENTITY("identity", "Identitas"),
    NOTE("note", "Catatan Rahasia");

    companion object {
        fun fromKey(key: String): VaultCategory =
            entries.find { it.key.equals(key, ignoreCase = true) } ?: ACCOUNT
    }
}

data class VaultItem(
    val id: String,
    val userId: String,
    val category: String,
    val title: String,
    val username: String? = null,
    val password: String? = null,
    val extraData: String? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
