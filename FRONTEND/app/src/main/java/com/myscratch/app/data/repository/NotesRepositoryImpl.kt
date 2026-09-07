package com.myscratch.app.data.repository

import com.myscratch.app.data.local.dao.FolderDao
import com.myscratch.app.data.local.dao.NoteDao
import com.myscratch.app.data.local.entity.FolderEntity
import com.myscratch.app.data.local.entity.NoteEntity
import com.myscratch.app.data.network.ApiClient
import com.myscratch.app.data.network.TokenManager
import com.myscratch.app.data.network.dto.FolderRequestDto
import com.myscratch.app.data.network.dto.NoteRequestDto
import com.myscratch.app.domain.model.Folder
import com.myscratch.app.domain.model.Note
import com.myscratch.app.domain.repository.NotesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class NotesRepositoryImpl(
    private val folderDao: FolderDao,
    private val noteDao: NoteDao,
    private val tokenManager: TokenManager
) : NotesRepository {

    private val apiService = ApiClient.getService(tokenManager)

    override fun getFolders(userId: String): Flow<List<Folder>> {
        return folderDao.getFolders(userId).map { list -> list.map { it.toDomain() } }
    }

    override fun getNotes(userId: String): Flow<List<Note>> {
        return noteDao.getNotes(userId).map { list -> list.map { it.toDomain() } }
    }

    override fun getNotesByFolder(userId: String, folderId: String): Flow<List<Note>> {
        return noteDao.getNotesByFolder(userId, folderId).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun refreshNotesAndFolders(userId: String) = withContext(Dispatchers.IO) {
        try {
            val foldersRes = apiService.getFolders()
            if (foldersRes.isSuccessful && foldersRes.body()?.success == true) {
                foldersRes.body()?.folders?.forEach { f ->
                    folderDao.insertFolder(
                        FolderEntity(
                            id = f.id.toString(),
                            userId = userId,
                            name = f.name,
                            colorHex = f.color,
                            createdAt = System.currentTimeMillis()
                        )
                    )
                }
            }

            val notesRes = apiService.getNotes()
            if (notesRes.isSuccessful && notesRes.body()?.success == true) {
                notesRes.body()?.notes?.forEach { n ->
                    noteDao.insertNote(
                        NoteEntity(
                            id = n.id.toString(),
                            userId = userId,
                            folderId = n.folderId?.toString() ?: "",
                            title = n.title ?: "Tanpa Judul",
                            content = n.content,
                            createdAt = System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                }
            }
        } catch (_: Exception) {}
        Unit
    }

    override suspend fun getFolderById(folderId: String): Folder? =
        withContext(Dispatchers.IO) {
            folderDao.getFolderById(folderId)?.toDomain()
        }

    override suspend fun getNoteById(noteId: String): Note? =
        withContext(Dispatchers.IO) {
            noteDao.getNoteById(noteId)?.toDomain()
        }

    override suspend fun insertFolder(folder: Folder) =
        withContext(Dispatchers.IO) {
            folderDao.insertFolder(FolderEntity.fromDomain(folder))
            try {
                apiService.createFolder(
                    FolderRequestDto(name = folder.name, color = folder.colorHex)
                )
            } catch (_: Exception) {}
            Unit
        }

    override suspend fun updateFolder(folder: Folder) =
        withContext(Dispatchers.IO) {
            folderDao.updateFolder(FolderEntity.fromDomain(folder))
            Unit
        }

    override suspend fun deleteFolder(folderId: String) =
        withContext(Dispatchers.IO) {
            folderDao.deleteFolder(folderId)
            noteDao.deleteNotesByFolder(folderId)
            try {
                apiService.deleteFolder(folderId)
            } catch (_: Exception) {}
            Unit
        }

    override suspend fun insertNote(note: Note) =
        withContext(Dispatchers.IO) {
            noteDao.insertNote(NoteEntity.fromDomain(note))
            try {
                apiService.createNote(
                    NoteRequestDto(
                        title = note.title,
                        content = note.content,
                        folderId = note.folderId,
                        isPinned = false
                    )
                )
            } catch (_: Exception) {}
            Unit
        }

    override suspend fun updateNote(note: Note) =
        withContext(Dispatchers.IO) {
            noteDao.updateNote(NoteEntity.fromDomain(note))
            try {
                apiService.updateNote(
                    note.id,
                    NoteRequestDto(
                        title = note.title,
                        content = note.content,
                        folderId = note.folderId,
                        isPinned = false
                    )
                )
            } catch (_: Exception) {}
            Unit
        }

    override suspend fun deleteNote(noteId: String) =
        withContext(Dispatchers.IO) {
            noteDao.deleteNote(noteId)
            try {
                apiService.deleteNote(noteId)
            } catch (_: Exception) {}
            Unit
        }
}
