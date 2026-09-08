package com.myscratch.app.ui.vault

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myscratch.app.domain.model.VaultCategory
import com.myscratch.app.domain.model.VaultItem
import com.myscratch.app.ui.components.ClassyCard
import com.myscratch.app.ui.components.ClassyTextField
import com.myscratch.app.ui.theme.AppColors
import com.myscratch.app.viewmodel.VaultViewModel

@Composable
fun VaultScreen(
    vaultViewModel: VaultViewModel,
    onNavigateBack: () -> Unit,
    isTablet: Boolean = false
) {
    val context = LocalContext.current
    val uiState by vaultViewModel.uiState.collectAsState()

    var isAddEditOpen by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<VaultItem?>(null) }
    var itemToDelete by remember { mutableStateOf<VaultItem?>(null) }
    var isGeneratorOpen by remember { mutableStateOf(false) }

    if (isAddEditOpen) {
        AddEditVaultDialog(
            itemToEdit = itemToEdit,
            vaultViewModel = vaultViewModel,
            onDismissRequest = {
                isAddEditOpen = false
                itemToEdit = null
            }
        )
    }

    if (isGeneratorOpen) {
        PasswordGeneratorDialog(
            vaultViewModel = vaultViewModel,
            onDismissRequest = { isGeneratorOpen = false }
        )
    }

    if (itemToDelete != null) {
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            containerColor = AppColors.surface,
            title = {
                Text(text = "Hapus Data Rahasia", color = AppColors.textPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = "Apakah Anda yakin ingin menghapus '${itemToDelete?.title}' dari Brankas Rahasia?",
                    color = AppColors.textSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        itemToDelete?.let {
                            vaultViewModel.deleteVaultItem(it.id) {
                                Toast.makeText(context, "Data berhasil dihapus", Toast.LENGTH_SHORT).show()
                            }
                        }
                        itemToDelete = null
                    }
                ) {
                    Text(text = "Hapus", color = AppColors.coral, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text(text = "Batal", color = AppColors.textMuted)
                }
            }
        )
    }

    val categories = listOf(
        null to "Semua",
        "account" to "Akun & Password",
        "bank" to "Kartu & Bank",
        "identity" to "Identitas",
        "note" to "Catatan Rahasia"
    )

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

            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = AppColors.textPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = "Brankas Rahasia",
                            color = AppColors.textPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Terenkripsi AES-256 & Biometrik",
                            color = AppColors.violet,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Password Generator Quick Tool Button
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = AppColors.violetBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.violet.copy(alpha = 0.5f)),
                    modifier = Modifier.clickable { isGeneratorOpen = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoFixHigh,
                            contentDescription = "Generator Password",
                            tint = AppColors.violet,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Generator",
                            color = AppColors.violet,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Search Bar
            ClassyTextField(
                value = uiState.searchQuery,
                onValueChange = { vaultViewModel.setSearchQuery(it) },
                label = "Cari di Brankas",
                placeholder = "Ketik nama akun, username, atau catatan...",
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = AppColors.textMuted)
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Category selector chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { (key, label) ->
                    val isSelected = uiState.selectedCategory == key
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) AppColors.violet else AppColors.surfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) AppColors.violet else AppColors.border
                        ),
                        modifier = Modifier.clickable { vaultViewModel.selectCategory(key) }
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color.White else AppColors.textPrimary,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // List of Vault Items
            if (uiState.items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 50.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(AppColors.violetBg, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = AppColors.violet,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Belum ada data di Brankas Rahasia",
                            color = AppColors.textPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Simpan password, PIN kartu, dan dokumen berharga Anda.",
                            color = AppColors.textMuted,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.items) { item ->
                        VaultCardItem(
                            item = item,
                            onEditClick = {
                                itemToEdit = item
                                isAddEditOpen = true
                            },
                            onDeleteClick = {
                                itemToDelete = item
                            }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = {
                itemToEdit = null
                isAddEditOpen = true
            },
            containerColor = AppColors.violet,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Tambah Data")
        }
    }
}

@Composable
fun VaultCardItem(
    item: VaultItem,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val context = LocalContext.current
    var isPasswordRevealed by remember { mutableStateOf(false) }

    val (categoryIcon, categoryName) = when (item.category) {
        "bank" -> Icons.Default.CreditCard to "Kartu & Bank"
        "identity" -> Icons.Default.Shield to "Identitas"
        "note" -> Icons.Default.Notes to "Catatan Rahasia"
        else -> Icons.Default.Key to "Akun & Password"
    }

    ClassyCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Category Icon + Title + Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(AppColors.violetBg, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = categoryIcon,
                            contentDescription = categoryName,
                            tint = AppColors.violet,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = item.title,
                            color = AppColors.textPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = categoryName,
                            color = AppColors.textMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = AppColors.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Hapus",
                            tint = AppColors.coral,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Username / Email Row
            if (!item.username.isNullOrBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppColors.surfaceVariant, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (item.category == "bank") "No. Rekening / Kartu" else "Username / Email",
                            color = AppColors.textMuted,
                            fontSize = 10.sp
                        )
                        Text(
                            text = item.username,
                            color = AppColors.textPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Username", item.username)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Username disalin", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Salin",
                            tint = AppColors.azure,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Password / PIN Row
            if (!item.password.isNullOrBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppColors.surfaceVariant, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (item.category == "bank") "PIN / CVV" else "Password",
                            color = AppColors.textMuted,
                            fontSize = 10.sp
                        )
                        Text(
                            text = if (isPasswordRevealed) item.password else "••••••••••••",
                            color = if (isPasswordRevealed) AppColors.violet else AppColors.textPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = if (isPasswordRevealed) FontFamily.Monospace else FontFamily.Default
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { isPasswordRevealed = !isPasswordRevealed },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (isPasswordRevealed) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Lihat",
                                tint = AppColors.textMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Password", item.password)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Password disalin", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Salin Password",
                                tint = AppColors.violet,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Extra Data (e.g. Card Holder or URL)
            if (!item.extraData.isNullOrBlank()) {
                Text(
                    text = item.extraData,
                    color = AppColors.textSecondary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Notes
            if (!item.notes.isNullOrBlank()) {
                Text(
                    text = item.notes,
                    color = AppColors.textMuted,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    maxLines = 2
                )
            }
        }
    }
}
