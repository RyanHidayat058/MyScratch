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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myscratch.app.domain.model.Note
import com.myscratch.app.ui.components.BiometricHelper
import com.myscratch.app.ui.components.ClassyCard
import com.myscratch.app.ui.components.ClassyTextField
import com.myscratch.app.ui.theme.AppColors
import com.myscratch.app.viewmodel.NotesViewModel
import java.util.UUID

data class ChecklistItemData(
    val id: String = UUID.randomUUID().toString(),
    var text: String,
    var isChecked: Boolean
)

@Composable
fun NoteEditorScreen(
    noteId: String,
    folderId: String,
    notesViewModel: NotesViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val uiState by notesViewModel.uiState.collectAsState()

    val existingNote = remember(noteId, uiState.notes) {
        if (noteId != "new") {
            uiState.notes.find { it.id == noteId }
        } else null
    }

    var title by remember { mutableStateOf(existingNote?.title ?: "") }
    var content by remember { mutableStateOf(existingNote?.content ?: "") }
    var isLocked by remember { mutableStateOf(existingNote?.isLocked ?: false) }
    var selectedFolderId by remember {
        mutableStateOf(
            existingNote?.folderId ?: if (folderId.isNotBlank()) folderId else (uiState.folders.firstOrNull()?.id ?: "general")
        )
    }

    // Mode Checklist vs Mode Text
    val initialHasChecklist = remember(existingNote) {
        existingNote?.content?.let { it.contains("[ ]") || it.contains("[x]") } ?: false
    }
    var isChecklistMode by remember { mutableStateOf(initialHasChecklist) }

    val checklistItems = remember { mutableStateListOf<ChecklistItemData>() }

    // Parse content to checklist items when entering checklist mode or initially
    fun parseContentToChecklist(raw: String) {
        checklistItems.clear()
        val lines = raw.split("\n")
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.startsWith("[x] ")) {
                checklistItems.add(ChecklistItemData(text = trimmed.removePrefix("[x] "), isChecked = true))
            } else if (trimmed.startsWith("[ ] ")) {
                checklistItems.add(ChecklistItemData(text = trimmed.removePrefix("[ ] "), isChecked = false))
            } else if (trimmed.isNotBlank()) {
                checklistItems.add(ChecklistItemData(text = trimmed, isChecked = false))
            }
        }
        if (checklistItems.isEmpty()) {
            checklistItems.add(ChecklistItemData(text = "", isChecked = false))
        }
    }

    fun syncChecklistToContent(): String {
        return checklistItems
            .filter { it.text.isNotBlank() }
            .joinToString("\n") { item ->
                if (item.isChecked) "[x] ${item.text}" else "[ ] ${item.text}"
            }
    }

    LaunchedEffect(Unit) {
        if (initialHasChecklist) {
            parseContentToChecklist(content)
        }
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

        // Top Bar
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
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = if (existingNote != null) "Edit Catatan" else "Catatan Baru",
                        color = AppColors.textPrimary,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (isLocked) {
                        Text(
                            text = "🔒 Terkunci Biometrik",
                            color = AppColors.coral,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Biometric Lock Toggle
                IconButton(
                    onClick = {
                        BiometricHelper.authenticate(
                            context = context,
                            title = if (isLocked) "Buka Kunci Catatan" else "Kunci Catatan",
                            subtitle = "Verifikasi identitas Anda untuk mengubah status kunci",
                            onSuccess = {
                                isLocked = !isLocked
                                Toast.makeText(
                                    context,
                                    if (isLocked) "Catatan dikunci dengan biometrik 🔒" else "Kunci catatan dinonaktifkan 🔓",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            onError = { err ->
                                Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                ) {
                    Icon(
                        imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = "Kunci Catatan",
                        tint = if (isLocked) AppColors.coral else AppColors.textMuted
                    )
                }

                if (existingNote != null) {
                    IconButton(
                        onClick = {
                            notesViewModel.deleteNote(existingNote.id)
                            Toast.makeText(context, "Catatan dipindahkan ke Kotak Sampah 🗑️", Toast.LENGTH_SHORT).show()
                            onNavigateBack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Hapus ke Sampah",
                            tint = AppColors.coral
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

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
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Mode Switcher: Teks Biasa vs Checklist
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = AppColors.surfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.border)
            ) {
                Row(modifier = Modifier.padding(4.dp)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (!isChecklistMode) AppColors.azure else Color.Transparent,
                        modifier = Modifier.clickable {
                            if (isChecklistMode) {
                                content = syncChecklistToContent()
                                isChecklistMode = false
                            }
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notes,
                                contentDescription = null,
                                tint = if (!isChecklistMode) Color.White else AppColors.textMuted,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Teks Bebas",
                                color = if (!isChecklistMode) Color.White else AppColors.textMuted,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isChecklistMode) AppColors.azure else Color.Transparent,
                        modifier = Modifier.clickable {
                            if (!isChecklistMode) {
                                parseContentToChecklist(content)
                                isChecklistMode = true
                            }
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.FormatListBulleted,
                                contentDescription = null,
                                tint = if (isChecklistMode) Color.White else AppColors.textMuted,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Checklist",
                                color = if (isChecklistMode) Color.White else AppColors.textMuted,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            if (isLocked) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AppColors.coral.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "Dilindungi",
                        color = AppColors.coral,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Main Editor Card
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

                if (!isChecklistMode) {
                    // Standard Text Mode
                    ClassyTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = "Isi Catatan",
                        placeholder = "Tulis isi catatan Anda di sini...",
                        singleLine = false,
                        maxLines = 15,
                        modifier = Modifier.height(260.dp)
                    )
                } else {
                    // Interactive Checklist Mode
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AppColors.surfaceVariant, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Daftar Tugas / Belanjaan",
                            color = AppColors.textSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        checklistItems.forEachIndexed { index, item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = item.isChecked,
                                    onCheckedChange = { isChecked ->
                                        checklistItems[index] = item.copy(isChecked = isChecked)
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = AppColors.emerald,
                                        uncheckedColor = AppColors.textMuted,
                                        checkmarkColor = Color.White
                                    )
                                )

                                BasicTextField(
                                    value = item.text,
                                    onValueChange = { newText ->
                                        checklistItems[index] = item.copy(text = newText)
                                    },
                                    textStyle = TextStyle(
                                        color = if (item.isChecked) AppColors.textMuted else AppColors.textPrimary,
                                        fontSize = 14.sp,
                                        textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None
                                    ),
                                    cursorBrush = SolidColor(AppColors.azure),
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 6.dp)
                                )

                                IconButton(
                                    onClick = { checklistItems.removeAt(index) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Hapus Baris",
                                        tint = AppColors.textMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Add item button
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AppColors.surface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.border),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    checklistItems.add(ChecklistItemData(text = "", isChecked = false))
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = AppColors.azure,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Tambah Item Checklist",
                                    color = AppColors.azure,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val finalContent = if (isChecklistMode) syncChecklistToContent() else content.trim()
                        if (title.isNotBlank() || finalContent.isNotBlank()) {
                            val activeTitle = if (title.isBlank()) "Tanpa Judul" else title.trim()
                            if (existingNote != null) {
                                notesViewModel.updateNote(
                                    existingNote.copy(
                                        title = activeTitle,
                                        content = finalContent,
                                        folderId = selectedFolderId,
                                        isLocked = isLocked
                                    ),
                                    onSuccess = onNavigateBack
                                )
                            } else {
                                notesViewModel.addNote(
                                    folderId = selectedFolderId,
                                    title = activeTitle,
                                    content = finalContent,
                                    isLocked = isLocked,
                                    onSuccess = onNavigateBack
                                )
                            }
                        }
                    },
                    enabled = title.isNotBlank() || (if (isChecklistMode) checklistItems.any { it.text.isNotBlank() } else content.isNotBlank()),
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
