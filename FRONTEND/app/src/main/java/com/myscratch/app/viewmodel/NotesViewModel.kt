package com.myscratch.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.myscratch.app.domain.model.Folder
import com.myscratch.app.domain.model.Note
import com.myscratch.app.domain.repository.NotesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

data class NotesUiState(
    val folders: List<Folder> = emptyList(),
    val notes: List<Note> = emptyList(),
    val selectedFolderId: String? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = false
)

class NotesViewModel(
    private val notesRepository: NotesRepository,
    private val userId: String
) : ViewModel() {

    private val _selectedFolderId = MutableStateFlow<String?>(null)
    private val _searchQuery = MutableStateFlow("")

    val uiState: StateFlow<NotesUiState> = combine(
        notesRepository.getFolders(userId),
        notesRepository.getNotes(userId),
        _selectedFolderId,
        _searchQuery
    ) { folders, notes, selectedFolderId, query ->
        val filteredByFolder = if (selectedFolderId != null) {
            notes.filter { it.folderId == selectedFolderId }
        } else {
            notes
        }

        val filteredNotes = if (query.isNotBlank()) {
            filteredByFolder.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.content.contains(query, ignoreCase = true)
            }
        } else {
            filteredByFolder
        }

        NotesUiState(
            folders = folders,
            notes = filteredNotes,
            selectedFolderId = selectedFolderId,
            searchQuery = query
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NotesUiState()
    )

    fun selectFolder(folderId: String?) {
        _selectedFolderId.value = folderId
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addFolder(name: String, colorHex: String = "#38BDF8", onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val folder = Folder(
                id = UUID.randomUUID().toString(),
                userId = userId,
                name = name,
                colorHex = colorHex
            )
            notesRepository.insertFolder(folder)
            onSuccess()
        }
    }

    fun deleteFolder(folderId: String) {
        viewModelScope.launch {
            notesRepository.deleteFolder(folderId)
            if (_selectedFolderId.value == folderId) {
                _selectedFolderId.value = null
            }
        }
    }

    fun addNote(
        folderId: String,
        title: String,
        content: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val note = Note(
                id = UUID.randomUUID().toString(),
                userId = userId,
                folderId = folderId,
                title = title,
                content = content
            )
            notesRepository.insertNote(note)
            onSuccess()
        }
    }

    fun updateNote(
        note: Note,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            notesRepository.updateNote(note.copy(updatedAt = System.currentTimeMillis()))
            onSuccess()
        }
    }

    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            notesRepository.deleteNote(noteId)
        }
    }

    companion object {
        fun provideFactory(
            notesRepository: NotesRepository,
            userId: String
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return NotesViewModel(notesRepository, userId) as T
            }
        }
    }
}
