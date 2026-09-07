package com.myscratch.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.myscratch.app.domain.model.Note

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val folderId: String,
    val title: String,
    val content: String,
    val updatedAt: Long,
    val createdAt: Long
) {
    fun toDomain(): Note = Note(
        id = id,
        userId = userId,
        folderId = folderId,
        title = title,
        content = content,
        updatedAt = updatedAt,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(note: Note): NoteEntity = NoteEntity(
            id = note.id,
            userId = note.userId,
            folderId = note.folderId,
            title = note.title,
            content = note.content,
            updatedAt = note.updatedAt,
            createdAt = note.createdAt
        )
    }
}
