package com.myscratch.app.ui.vault

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myscratch.app.domain.model.VaultCategory
import com.myscratch.app.domain.model.VaultItem
import com.myscratch.app.ui.components.ClassyTextField
import com.myscratch.app.ui.theme.AppColors
import com.myscratch.app.viewmodel.VaultViewModel

@Composable
fun AddEditVaultDialog(
    itemToEdit: VaultItem? = null,
    vaultViewModel: VaultViewModel,
    onDismissRequest: () -> Unit
) {
    var category by remember { mutableStateOf(itemToEdit?.category ?: "account") }
    var title by remember { mutableStateOf(itemToEdit?.title ?: "") }
    var username by remember { mutableStateOf(itemToEdit?.username ?: "") }
    var password by remember { mutableStateOf(itemToEdit?.password ?: "") }
    var extraData by remember { mutableStateOf(itemToEdit?.extraData ?: "") }
    var notes by remember { mutableStateOf(itemToEdit?.notes ?: "") }

    var isPasswordVisible by remember { mutableStateOf(false) }
    var isGeneratorOpen by remember { mutableStateOf(false) }

    if (isGeneratorOpen) {
        PasswordGeneratorDialog(
            vaultViewModel = vaultViewModel,
            onDismissRequest = { isGeneratorOpen = false },
            onPasswordSelected = { generated ->
                password = generated
            }
        )
    }

    val categories = listOf(
        VaultCategory.ACCOUNT to "Akun & Password",
        VaultCategory.BANK to "Kartu & Bank",
        VaultCategory.IDENTITY to "Identitas",
        VaultCategory.NOTE to "Catatan Rahasia"
    )

    AlertDialog(
        onDismissRequest = onDismissRequest,
        containerColor = AppColors.surface,
        title = {
            Text(
                text = if (itemToEdit != null) "Edit Data Rahasia" else "Simpan ke Brankas",
                color = AppColors.textPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Kategori",
                    color = AppColors.textSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { (cat, label) ->
                        val isSelected = category == cat.key
                        FilterChip(
                            selected = isSelected,
                            onClick = { category = cat.key },
                            label = { Text(label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AppColors.violet,
                                selectedLabelColor = Color.White,
                                containerColor = AppColors.surfaceVariant,
                                labelColor = AppColors.textSecondary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                ClassyTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = when (category) {
                        "account" -> "Nama Aplikasi / Website"
                        "bank" -> "Nama Bank / Kartu"
                        "identity" -> "Jenis Dokumen (e.g. KTP, SIM)"
                        else -> "Judul Catatan Rahasia"
                    },
                    placeholder = when (category) {
                        "account" -> "e.g. Netflix, Gmail, GitHub"
                        "bank" -> "e.g. BCA Prioritas, Mandiri"
                        "identity" -> "e.g. KTP Ryan, Paspor"
                        else -> "e.g. Seed Phrase Dompet Kripto"
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (category != "note") {
                    ClassyTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = when (category) {
                            "account" -> "Email / Username"
                            "bank" -> "Nomor Rekening / No. Kartu"
                            "identity" -> "Nomor Identitas (NIK/No. Paspor)"
                            else -> "ID / Pengenal"
                        },
                        placeholder = when (category) {
                            "account" -> "nama@email.com / username"
                            "bank" -> "1234-5678-xxxx"
                            "identity" -> "3201xxxxxxx"
                            else -> ""
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (category == "account" || category == "bank") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            ClassyTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = if (category == "bank") "PIN / CVV" else "Password",
                                placeholder = "Ketik password...",
                                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                        Icon(
                                            imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = "Lihat Password",
                                            tint = AppColors.textMuted
                                        )
                                    }
                                }
                            )
                        }

                        if (category == "account") {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = AppColors.violetBg,
                                border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.violet.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .padding(top = 16.dp)
                                    .clickable { isGeneratorOpen = true }
                            ) {
                                Box(
                                    modifier = Modifier.padding(12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoFixHigh,
                                        contentDescription = "Generate Password",
                                        tint = AppColors.violet,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                ClassyTextField(
                    value = extraData,
                    onValueChange = { extraData = it },
                    label = when (category) {
                        "account" -> "URL Login / Tautan Web"
                        "bank" -> "Nama Pemilik Rekening"
                        "identity" -> "Masa Berlaku / Instansi Penerbit"
                        else -> "Data Tambahan"
                    },
                    placeholder = "Opsional..."
                )

                Spacer(modifier = Modifier.height(12.dp))

                ClassyTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = "Catatan Rahasia",
                    placeholder = "Tambahan informasi sensitif...",
                    singleLine = false,
                    maxLines = 4,
                    modifier = Modifier.height(100.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        if (itemToEdit != null) {
                            vaultViewModel.updateVaultItem(
                                itemToEdit.copy(
                                    category = category,
                                    title = title,
                                    username = username.ifBlank { null },
                                    password = password.ifBlank { null },
                                    extraData = extraData.ifBlank { null },
                                    notes = notes.ifBlank { null }
                                ),
                                onSuccess = onDismissRequest
                            )
                        } else {
                            vaultViewModel.addVaultItem(
                                category = category,
                                title = title,
                                username = username.ifBlank { null },
                                password = password.ifBlank { null },
                                extraData = extraData.ifBlank { null },
                                notes = notes.ifBlank { null },
                                onSuccess = onDismissRequest
                            )
                        }
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.violet)
            ) {
                Text(
                    text = if (itemToEdit != null) "Perbarui" else "Simpan ke Brankas",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = "Batal", color = AppColors.textMuted)
            }
        }
    )
}
