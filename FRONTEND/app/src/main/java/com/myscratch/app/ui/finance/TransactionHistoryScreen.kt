package com.myscratch.app.ui.finance

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myscratch.app.domain.model.Transaction
import com.myscratch.app.domain.model.TransactionType
import com.myscratch.app.ui.components.ClassyCard
import com.myscratch.app.ui.components.ClassyTextField
import com.myscratch.app.ui.components.formatRupiah
import com.myscratch.app.ui.theme.AppColors
import com.myscratch.app.viewmodel.FinanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryScreen(
    financeViewModel: FinanceViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEditTransaction: (String) -> Unit
) {
    val uiState by financeViewModel.uiState.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) } // 0: Semua, 1: Pengeluaran, 2: Pemasukan
    var searchQuery by remember { mutableStateOf("") }
    var transactionToDelete by remember { mutableStateOf<Transaction?>(null) }

    val filteredTransactions = uiState.transactions.filter { tx ->
        val matchesTab = when (selectedTabIndex) {
            1 -> tx.type == TransactionType.EXPENSE
            2 -> tx.type == TransactionType.INCOME
            3 -> tx.type == TransactionType.DEBT_PAYABLE
            4 -> tx.type == TransactionType.DEBT_RECEIVABLE
            else -> true
        }
        val matchesQuery = searchQuery.isBlank() ||
                tx.title.contains(searchQuery, ignoreCase = true) ||
                tx.category.contains(searchQuery, ignoreCase = true) ||
                tx.note.contains(searchQuery, ignoreCase = true)
        matchesTab && matchesQuery
    }

    if (transactionToDelete != null) {
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            containerColor = AppColors.surface,
            title = {
                Text(text = "Hapus Transaksi", color = AppColors.textPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = "Apakah Anda yakin ingin menghapus transaksi '${transactionToDelete?.title}'?",
                    color = AppColors.textSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        transactionToDelete?.let { financeViewModel.deleteTransaction(it.id) }
                        transactionToDelete = null
                    }
                ) {
                    Text(text = "Hapus", color = AppColors.coral, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { transactionToDelete = null }) {
                    Text(text = "Batal", color = AppColors.textMuted)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.background)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Kembali",
                    tint = AppColors.textPrimary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Riwayat Transaksi",
                color = AppColors.textPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search Field
        ClassyTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = "Cari Transaksi",
            placeholder = "Cari judul, kategori, atau catatan...",
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = AppColors.textMuted)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Filter Chips: Semua / Pengeluaran / Pemasukan / Hutang Saya / Hutang ke Saya
        val tabs = listOf("Semua", "Pengeluaran", "Pemasukan", "Hutang Saya", "Hutang ke Saya")
        androidx.compose.foundation.lazy.LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(tabs.size) { index ->
                val isSelected = selectedTabIndex == index
                androidx.compose.material3.FilterChip(
                    selected = isSelected,
                    onClick = { selectedTabIndex = index },
                    label = { Text(tabs[index], fontSize = 12.sp) },
                    colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                        selectedContainerColor = when (index) {
                            1 -> AppColors.coral
                            2 -> AppColors.emerald
                            3 -> Color(0xFFF59E0B)
                            4 -> Color(0xFF38BDF8)
                            else -> AppColors.emerald
                        },
                        selectedLabelColor = Color.White,
                        containerColor = AppColors.surfaceVariant,
                        labelColor = AppColors.textSecondary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Results count
        Text(
            text = "Ditemukan ${filteredTransactions.size} transaksi",
            color = AppColors.textMuted,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (filteredTransactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Tidak ada transaksi yang cocok.",
                    color = AppColors.textMuted,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(filteredTransactions) { tx ->
                    ClassyCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = tx.title,
                                    color = AppColors.textPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                val dateFormat = remember { java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale("id", "ID")) }
                                Text(
                                    text = "${tx.category} • ${dateFormat.format(java.util.Date(tx.date))}${if (tx.note.isNotBlank()) " • ${tx.note}" else ""}",
                                    color = AppColors.textMuted,
                                    fontSize = 12.sp,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                val (prefix, amtColor) = when (tx.type) {
                                    TransactionType.INCOME -> "+ " to AppColors.emerald
                                    TransactionType.EXPENSE -> "- " to AppColors.coral
                                    TransactionType.DEBT_PAYABLE -> "Hutang: " to Color(0xFFF59E0B)
                                    TransactionType.DEBT_RECEIVABLE -> "Piutang: " to Color(0xFF38BDF8)
                                }
                                Text(
                                    text = "$prefix${formatRupiah(tx.amount)}",
                                    color = amtColor,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Row {
                                IconButton(onClick = { onNavigateToEditTransaction(tx.id) }) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Ubah",
                                        tint = AppColors.azure,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                IconButton(onClick = { transactionToDelete = tx }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Hapus",
                                        tint = AppColors.coral,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
