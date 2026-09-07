package com.myscratch.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.myscratch.app.domain.model.Folder

@Entity(tableName = "folders")
data class FolderEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val name: String,
    val colorHex: String,
    val createdAt: Long
) {
    fun toDomain(): Folder = Folder(
        id = id,
        userId = userId,
        name = name,
        colorHex = colorHex,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(folder: Folder): FolderEntity = FolderEntity(
            id = folder.id,
            userId = folder.userId,
            name = folder.name,
            colorHex = folder.colorHex,
            createdAt = folder.createdAt
        )
    }
}
