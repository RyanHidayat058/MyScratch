package com.myscratch.app.ui.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myscratch.app.domain.model.Note
import com.myscratch.app.ui.components.ClassyCard
import com.myscratch.app.ui.components.ClassyTextField
import com.myscratch.app.ui.theme.AppColors
import com.myscratch.app.viewmodel.NotesViewModel

@Composable
fun NoteEditorScreen(
    noteId: String,
    folderId: String,
    notesViewModel: NotesViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by notesViewModel.uiState.collectAsState()

    val existingNote = remember(noteId, uiState.notes) {
        if (noteId != "new") {
            uiState.notes.find { it.id == noteId }
        } else null
    }

    var title by remember { mutableStateOf(existingNote?.title ?: "") }
    var content by remember { mutableStateOf(existingNote?.content ?: "") }
    var selectedFolderId by remember {
        mutableStateOf(
            existingNote?.folderId ?: if (folderId.isNotBlank()) folderId else (uiState.folders.firstOrNull()?.id ?: "general")
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.background)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Top Bar - safely padded below status bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = AppColors.textPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (existingNote != null) "Edit Catatan" else "Catatan Baru",
                    color = AppColors.textPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (existingNote != null) {
                IconButton(
                    onClick = {
                        notesViewModel.deleteNote(existingNote.id)
                        onNavigateBack()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Hapus",
                        tint = AppColors.coral
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Folder selection
        if (uiState.folders.isNotEmpty()) {
            Text(
                text = "Pilih Folder",
                color = AppColors.textSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uiState.folders) { folder ->
                    val isSelected = selectedFolderId == folder.id
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFolderId = folder.id },
                        label = { Text(folder.name, fontSize = 13.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AppColors.azure,
                            selectedLabelColor = Color.White,
                            containerColor = AppColors.surfaceVariant,
                            labelColor = AppColors.textSecondary
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        ClassyCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                ClassyTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = "Judul Catatan",
                    placeholder = "Judul ringkas...",
                    leadingIcon = {
                        Icon(Icons.Default.Title, contentDescription = null, tint = AppColors.textMuted)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                ClassyTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = "Isi Catatan",
                    placeholder = "Tulis isi catatan Anda di sini...",
                    singleLine = false,
                    maxLines = 15,
                    modifier = Modifier.height(260.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (title.isNotBlank() || content.isNotBlank()) {
                            val activeTitle = if (title.isBlank()) "Tanpa Judul" else title.trim()
                            if (existingNote != null) {
                                notesViewModel.updateNote(
                                    existingNote.copy(
                                        title = activeTitle,
                                        content = content.trim(),
                                        folderId = selectedFolderId
                                    ),
                                    onSuccess = onNavigateBack
                                )
                            } else {
                                notesViewModel.addNote(
                                    folderId = selectedFolderId,
                                    title = activeTitle,
                                    content = content.trim(),
                                    onSuccess = onNavigateBack
                                )
                            }
                        }
                    },
                    enabled = title.isNotBlank() || content.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.azure),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(
                        text = if (existingNote != null) "Perbarui Catatan" else "Simpan Catatan",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}
