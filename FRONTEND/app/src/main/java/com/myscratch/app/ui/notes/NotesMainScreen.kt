package com.myscratch.app.ui.notes

import android.widget.Toast
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
import androidx.compose.material.icons.filled.AutoDelete
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Restore
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myscratch.app.domain.model.Folder
import com.myscratch.app.domain.model.Note
import com.myscratch.app.ui.components.BiometricHelper
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
    val context = LocalContext.current
    val uiState by notesViewModel.uiState.collectAsState()

    androidx.compose.runtime.LaunchedEffect(Unit) {
        notesViewModel.refresh()
    }

    var isAddFolderDialogOpen by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }
    var noteToDelete by remember { mutableStateOf<Note?>(null) }
    var noteToPermanentlyDelete by remember { mutableStateOf<Note?>(null) }
    var isEmptyTrashDialogOpen by remember { mutableStateOf(false) }

    fun openNoteSafely(note: Note) {
        if (note.isLocked) {
            BiometricHelper.authenticate(
                context = context,
                title = "Buka Catatan Terkunci",
                subtitle = "Autentikasi sidik jari atau PIN untuk melihat '${note.title}'",
                onSuccess = {
                    onNavigateToEditNote(note.id, note.folderId)
                },
                onError = { err ->
                    Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            onNavigateToEditNote(note.id, note.folderId)
        }
    }

    // Add folder dialog
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

    // Soft delete confirmation
    if (noteToDelete != null) {
        AlertDialog(
            onDismissRequest = { noteToDelete = null },
            containerColor = AppColors.surface,
            title = {
                Text(text = "Pindahkan ke Sampah", color = AppColors.textPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = "Pindahkan '${noteToDelete?.title}' ke Kotak Sampah? Anda masih dapat memulihkannya nanti.",
                    color = AppColors.textSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        noteToDelete?.let {
                            notesViewModel.deleteNote(it.id)
                            Toast.makeText(context, "Catatan dipindahkan ke Kotak Sampah", Toast.LENGTH_SHORT).show()
                        }
                        noteToDelete = null
                    }
                ) {
                    Text(text = "Pindahkan", color = AppColors.coral, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { noteToDelete = null }) {
                    Text(text = "Batal", color = AppColors.textMuted)
                }
            }
        )
    }

    // Permanent delete confirmation
    if (noteToPermanentlyDelete != null) {
        AlertDialog(
            onDismissRequest = { noteToPermanentlyDelete = null },
            containerColor = AppColors.surface,
            title = {
                Text(text = "Hapus Permanen", color = AppColors.textPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = "Hapus '${noteToPermanentlyDelete?.title}' secara permanen? Tindakan ini tidak dapat dibatalkan.",
                    color = AppColors.textSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        noteToPermanentlyDelete?.let {
                            notesViewModel.permanentlyDeleteNote(it.id)
                            Toast.makeText(context, "Catatan dihapus permanen", Toast.LENGTH_SHORT).show()
                        }
                        noteToPermanentlyDelete = null
                    }
                ) {
                    Text(text = "Hapus Permanen", color = AppColors.coral, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { noteToPermanentlyDelete = null }) {
                    Text(text = "Batal", color = AppColors.textMuted)
                }
            }
        )
    }

    // Empty trash dialog
    if (isEmptyTrashDialogOpen) {
        AlertDialog(
            onDismissRequest = { isEmptyTrashDialogOpen = false },
            containerColor = AppColors.surface,
            title = {
                Text(text = "Kosongkan Kotak Sampah", color = AppColors.textPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = "Apakah Anda yakin ingin menghapus SEMUA catatan di kotak sampah secara permanen?",
                    color = AppColors.textSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        notesViewModel.emptyTrash()
                        isEmptyTrashDialogOpen = false
                        Toast.makeText(context, "Kotak sampah dikosongkan", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text(text = "Kosongkan", color = AppColors.coral, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { isEmptyTrashDialogOpen = false }) {
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

            // Header
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
                            text = if (uiState.isTrashView) "Kotak Sampah" else "Catatan Pribadi",
                            color = AppColors.textPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (uiState.isTrashView) "Catatan terhapus sementara" else "Folder & dokumen terenkripsi",
                            color = AppColors.textMuted,
                            fontSize = 12.sp
                        )
                    }
                }

                if (!uiState.isTrashView) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = AppColors.surfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.border),
                        modifier = Modifier.clickable { isAddFolderDialogOpen = true }
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
                } else {
                    if (uiState.trashedNotes.isNotEmpty()) {
                        TextButton(onClick = { isEmptyTrashDialogOpen = true }) {
                            Icon(Icons.Default.AutoDelete, contentDescription = null, tint = AppColors.coral, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Kosongkan", color = AppColors.coral, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Search Bar
            ClassyTextField(
                value = uiState.searchQuery,
                onValueChange = { notesViewModel.setSearchQuery(it) },
                label = if (uiState.isTrashView) "Cari di Kotak Sampah" else "Cari Catatan",
                placeholder = "Ketik judul atau isi...",
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = AppColors.textMuted)
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Folders & Trash selector chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Active Notes chip
                item {
                    val isCatatanAktif = !uiState.isTrashView && uiState.selectedFolderId == null
                    FolderChip(
                        name = "Semua Catatan",
                        isSelected = isCatatanAktif,
                        onClick = {
                            notesViewModel.toggleTrashView(false)
                            notesViewModel.selectFolder(null)
                        }
                    )
                }

                // Folders chips (only when not in trash)
                if (!uiState.isTrashView) {
                    items(uiState.folders) { folder ->
                        val isSelected = uiState.selectedFolderId == folder.id
                        FolderChip(
                            name = folder.name,
                            isSelected = isSelected,
                            onClick = {
                                notesViewModel.toggleTrashView(false)
                                notesViewModel.selectFolder(folder.id)
                            }
                        )
                    }
                }

                // Trash Chip
                item {
                    val isTrashSelected = uiState.isTrashView
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isTrashSelected) AppColors.coral else AppColors.surfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isTrashSelected) AppColors.coral else AppColors.border
                        ),
                        modifier = Modifier.clickable {
                            notesViewModel.toggleTrashView(!isTrashSelected)
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = if (isTrashSelected) Color.White else AppColors.coral,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Kotak Sampah (${uiState.trashedNotes.size})",
                                color = if (isTrashSelected) Color.White else AppColors.textPrimary,
                                fontSize = 13.sp,
                                fontWeight = if (isTrashSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Content: Active Notes List vs Trashed Notes List
            if (!uiState.isTrashView) {
                // Active Notes
                if (uiState.notes.isEmpty()) {
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
                        items(uiState.notes) { note ->
                            NoteCardItem(
                                note = note,
                                onNoteClick = { openNoteSafely(note) },
                                onDeleteClick = { noteToDelete = note }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            } else {
                // Trashed Notes View
                if (uiState.trashedNotes.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Kotak sampah kosong ✨",
                                color = AppColors.textPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Catatan yang Anda hapus akan muncul di sini.",
                                color = AppColors.textMuted,
                                fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(uiState.trashedNotes) { note ->
                            TrashedNoteCardItem(
                                note = note,
                                onRestoreClick = {
                                    notesViewModel.restoreNote(note.id)
                                    Toast.makeText(context, "'${note.title}' berhasil dipulihkan", Toast.LENGTH_SHORT).show()
                                },
                                onPermanentDeleteClick = {
                                    noteToPermanentlyDelete = note
                                }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }
        }

        // Floating Action Button (Only show on active notes)
        if (!uiState.isTrashView) {
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
}

@Composable
fun NoteCardItem(
    note: Note,
    onNoteClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
    val dateStr = dateFormat.format(Date(note.updatedAt))

    // Check if note is a checklist
    val isChecklist = note.content.contains("[ ]") || note.content.contains("[x]")
    val totalChecklist = if (isChecklist) {
        val total = note.content.split("\n").count { it.contains("[ ]") || it.contains("[x]") }
        val done = note.content.split("\n").count { it.contains("[x]") }
        "$done/$total selesai"
    } else null

    ClassyCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNoteClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    if (note.isLocked) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Terkunci",
                            tint = AppColors.coral,
                            modifier = Modifier
                                .size(16.dp)
                                .padding(end = 4.dp)
                        )
                    }
                    Text(
                        text = note.title,
                        color = AppColors.textPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Pindahkan ke Sampah",
                        tint = AppColors.coral,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (note.isLocked) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AppColors.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = AppColors.coral,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Catatan Terkunci — Ketuk untuk membuka dengan sidik jari/PIN",
                            color = AppColors.textMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                Text(
                    text = note.content,
                    color = AppColors.textSecondary,
                    fontSize = 13.sp,
                    maxLines = 3,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateStr,
                    color = AppColors.textMuted,
                    fontSize = 11.sp
                )

                if (totalChecklist != null) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = AppColors.azure.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "✓ $totalChecklist",
                            color = AppColors.azure,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TrashedNoteCardItem(
    note: Note,
    onRestoreClick: () -> Unit,
    onPermanentDeleteClick: () -> Unit
) {
    ClassyCard(
        modifier = Modifier.fillMaxWidth()
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

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Restore button
                    IconButton(
                        onClick = onRestoreClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restore,
                            contentDescription = "Pulihkan",
                            tint = AppColors.emerald,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Permanent delete button
                    IconButton(
                        onClick = onPermanentDeleteClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteForever,
                            contentDescription = "Hapus Permanen",
                            tint = AppColors.coral,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (note.isLocked) "🔒 Catatan Terkunci" else note.content,
                color = AppColors.textMuted,
                fontSize = 13.sp,
                maxLines = 2,
                lineHeight = 18.sp
            )
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
