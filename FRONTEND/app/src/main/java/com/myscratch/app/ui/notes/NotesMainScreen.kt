package com.myscratch.app.ui.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.myscratch.app.domain.model.Folder
import com.myscratch.app.domain.model.Note
import com.myscratch.app.ui.components.ClassyCard
import com.myscratch.app.ui.components.ClassyTextField
import com.myscratch.app.ui.theme.AppColors
import com.myscratch.app.viewmodel.NotesViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotesMainScreen(
    notesViewModel: NotesViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEditNote: (String, String) -> Unit,
    isTablet: Boolean = false
) {
    val uiState by notesViewModel.uiState.collectAsState()

    var isAddFolderDialogOpen by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }
    var noteToDelete by remember { mutableStateOf<Note?>(null) }
    var selectedNoteForTablet by remember { mutableStateOf<Note?>(null) }

    if (isAddFolderDialogOpen) {
        AlertDialog(
            onDismissRequest = { isAddFolderDialogOpen = false },
            containerColor = AppColors.surface,
            title = {
                Text(text = "Buat Folder Baru", color = AppColors.textPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        text = "Kelompokkan catatan Anda ke dalam folder.",
                        color = AppColors.textSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    ClassyTextField(
                        value = newFolderName,
                        onValueChange = { newFolderName = it },
                        label = "Nama Folder",
                        placeholder = "e.g. Ide Bisnis, Kuliah, Rencana"
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFolderName.isNotBlank()) {
                            notesViewModel.addFolder(newFolderName.trim(), "#38BDF8")
                            newFolderName = ""
                            isAddFolderDialogOpen = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.azure)
                ) {
                    Text(text = "Buat Folder", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { isAddFolderDialogOpen = false }) {
                    Text(text = "Batal", color = AppColors.textMuted)
                }
            }
        )
    }

    if (noteToDelete != null) {
        AlertDialog(
            onDismissRequest = { noteToDelete = null },
            containerColor = AppColors.surface,
            title = {
                Text(text = "Hapus Catatan", color = AppColors.textPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = "Apakah Anda yakin ingin menghapus catatan '${noteToDelete?.title}'?",
                    color = AppColors.textSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        noteToDelete?.let { notesViewModel.deleteNote(it.id) }
                        if (selectedNoteForTablet?.id == noteToDelete?.id) {
                            selectedNoteForTablet = null
                        }
                        noteToDelete = null
                    }
                ) {
                    Text(text = "Hapus", color = AppColors.coral, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { noteToDelete = null }) {
                    Text(text = "Batal", color = AppColors.textMuted)
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.background)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            NotesHeader(
                folders = uiState.folders,
                selectedFolderId = uiState.selectedFolderId,
                onNavigateBack = onNavigateBack,
                onSelectFolder = { notesViewModel.selectFolder(it) },
                onAddFolder = { isAddFolderDialogOpen = true },
                searchQuery = uiState.searchQuery,
                onSearchChange = { notesViewModel.setSearchQuery(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            NotesListSection(
                notes = uiState.notes,
                onNoteClick = { note ->
                    onNavigateToEditNote(note.id, note.folderId)
                },
                onDeleteClick = { note -> noteToDelete = note }
            )
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = {
                val targetFolderId = uiState.selectedFolderId
                    ?: uiState.folders.firstOrNull()?.id
                    ?: "default"
                onNavigateToEditNote("new", targetFolderId)
            },
            containerColor = AppColors.azure,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Tulis Catatan")
        }
    }
}

@Composable
fun NotesHeader(
    folders: List<Folder>,
    selectedFolderId: String?,
    onNavigateBack: () -> Unit,
    onSelectFolder: (String?) -> Unit,
    onAddFolder: () -> Unit,
    searchQuery: String,
    onSearchChange: (String) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali ke Beranda",
                        tint = AppColors.textPrimary
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = "Catatan Pribadi",
                        color = AppColors.textPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Folder & dokumen terstruktur",
                        color = AppColors.textMuted,
                        fontSize = 12.sp
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = AppColors.surfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.border),
                modifier = Modifier.clickable { onAddFolder() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CreateNewFolder,
                        contentDescription = "Folder Baru",
                        tint = AppColors.azure,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "+ Folder",
                        color = AppColors.textPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Search Bar
        ClassyTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            label = "Cari Catatan",
            placeholder = "Ketik judul atau isi...",
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = AppColors.textMuted)
            }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Folders List
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                val isAllSelected = selectedFolderId == null
                FolderChip(
                    name = "Semua Catatan",
                    isSelected = isAllSelected,
                    onClick = { onSelectFolder(null) }
                )
            }
            items(folders) { folder ->
                val isSelected = selectedFolderId == folder.id
                FolderChip(
                    name = folder.name,
                    isSelected = isSelected,
                    onClick = { onSelectFolder(folder.id) }
                )
            }
        }
    }
}

@Composable
fun FolderChip(name: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) AppColors.azure else AppColors.surfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) AppColors.azure else AppColors.border),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = null,
                tint = if (isSelected) Color.White else AppColors.textMuted,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = name,
                color = if (isSelected) Color.White else AppColors.textPrimary,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
fun NotesListSection(
    notes: List<Note>,
    onNoteClick: (Note) -> Unit,
    onDeleteClick: (Note) -> Unit
) {
    if (notes.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 40.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Belum ada catatan pada folder ini.",
                color = AppColors.textMuted,
                fontSize = 14.sp
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(notes) { note ->
                val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
                val dateStr = dateFormat.format(Date(note.updatedAt))

                ClassyCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNoteClick(note) }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = note.title,
                                color = AppColors.textPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { onDeleteClick(note) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Hapus",
                                    tint = AppColors.coral,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = note.content,
                            color = AppColors.textSecondary,
                            fontSize = 13.sp,
                            maxLines = 3,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = dateStr,
                            color = AppColors.textMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
