package com.myscratch.app.data.network.dto

import com.google.gson.annotations.SerializedName

data class FolderRequestDto(
    val name: String,
    val color: String? = null
)

data class FolderDto(
    val id: Any,
    val name: String,
    val color: String = "#38BDF8",
    @SerializedName("notes_count")
    val notesCount: Int? = null
)

data class FoldersResponseDto(
    val success: Boolean,
    val folders: List<FolderDto> = emptyList()
)

data class FolderMutationResponseDto(
    val success: Boolean,
    val message: String? = null,
    val folder: FolderDto? = null
)

data class NoteRequestDto(
    val title: String?,
    val content: String,
    @SerializedName("folder_id")
    val folderId: Any? = null,
    @SerializedName("is_pinned")
    val isPinned: Boolean? = false
)

data class NoteDto(
    val id: Any,
    @SerializedName("folder_id")
    val folderId: Any? = null,
    val title: String? = null,
    val content: String,
    @SerializedName("is_pinned")
    val isPinned: Boolean = false,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null
)

data class NotesResponseDto(
    val success: Boolean,
    val notes: List<NoteDto> = emptyList()
)

data class NoteMutationResponseDto(
    val success: Boolean,
    val message: String? = null,
    val note: NoteDto? = null
)
