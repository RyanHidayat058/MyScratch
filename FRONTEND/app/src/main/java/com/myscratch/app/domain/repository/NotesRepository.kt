package com.myscratch.app.domain.repository

import com.myscratch.app.domain.model.Folder
import com.myscratch.app.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface NotesRepository {
    fun getFolders(userId: String): Flow<List<Folder>>
    fun getNotes(userId: String): Flow<List<Note>>
    fun getNotesByFolder(userId: String, folderId: String): Flow<List<Note>>
    fun getTrashedNotes(userId: String): Flow<List<Note>>
    suspend fun refreshNotesAndFolders(userId: String)
    suspend fun getFolderById(folderId: String): Folder?
    suspend fun getNoteById(noteId: String): Note?
    suspend fun insertFolder(folder: Folder)
    suspend fun updateFolder(folder: Folder)
    suspend fun deleteFolder(folderId: String)
    suspend fun insertNote(note: Note)
    suspend fun updateNote(note: Note)
    suspend fun deleteNote(noteId: String)
    suspend fun softDeleteNote(noteId: String)
    suspend fun restoreNote(noteId: String)
    suspend fun permanentlyDeleteNote(noteId: String)
    suspend fun emptyTrash(userId: String)
}
