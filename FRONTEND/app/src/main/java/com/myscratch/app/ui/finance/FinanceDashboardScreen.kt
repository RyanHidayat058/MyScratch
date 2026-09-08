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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
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
import androidx.compose.material3.ExperimentalMaterial3Api
import com.myscratch.app.domain.model.Transaction
import com.myscratch.app.domain.model.TransactionType
import com.myscratch.app.ui.components.ClassyCard
import com.myscratch.app.ui.components.ClassyTextField
import com.myscratch.app.ui.components.DonutChart
import com.myscratch.app.ui.components.formatRupiah
import com.myscratch.app.ui.theme.AppColors
import com.myscratch.app.viewmodel.FinanceViewModel
import com.myscratch.app.viewmodel.TimePeriod
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceDashboardScreen(
    userName: String,
    financeViewModel: FinanceViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAddTransaction: () -> Unit,
    onNavigateToEditTransaction: (String) -> Unit,
    isTablet: Boolean = false
) {
    val uiState by financeViewModel.uiState.collectAsState()

    androidx.compose.runtime.LaunchedEffect(Unit) {
        financeViewModel.refresh()
    }

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Ringkasan, 1: Riwayat Lengkap
    var searchQuery by remember { mutableStateOf("") }
    var historyTypeFilter by remember { mutableIntStateOf(0) } // 0: Semua, 1: Pengeluaran, 2: Pemasukan
    var transactionToDelete by remember { mutableStateOf<Transaction?>(null) }

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

            // Top Header with Back button, Title, and Calculator Shortcut
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
                            text = "Catatan Keuangan",
                            color = AppColors.textPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Kelola finansial & riwayat",
                            color = AppColors.textMuted,
                            fontSize = 12.sp
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = AppColors.surfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.border),
                    modifier = Modifier.clickable { financeViewModel.openCalculator() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = "Kalkulator",
                            tint = AppColors.emerald,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Kalkulator",
                            color = AppColors.textPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Integrated Tabs: Ringkasan vs Riwayat Transaksi
            SecondaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = AppColors.surface,
                contentColor = AppColors.emerald
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = "Ringkasan & Analisis",
                            color = if (selectedTab == 0) AppColors.emerald else AppColors.textMuted,
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = "Riwayat Transaksi",
                            color = if (selectedTab == 1) AppColors.emerald else AppColors.textMuted,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tab 0: Ringkasan & Diagram
            if (selectedTab == 0) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        TotalBalanceCard(
                            currentBalance = uiState.summary.totalBalance,
                            expectedNetWorth = uiState.summary.expectedNetWorth,
                            totalDebtReceivable = uiState.summary.totalDebtReceivable,
                            totalDebtPayable = uiState.summary.totalDebtPayable
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        MetricsGrid(
                            totalIncome = uiState.summary.totalIncome,
                            totalExpense = uiState.summary.totalExpense,
                            totalDebtReceivable = uiState.summary.totalDebtReceivable,
                            totalDebtPayable = uiState.summary.totalDebtPayable
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        PeriodFilterSelector(
                            selectedPeriod = uiState.selectedPeriod,
                            onSelect = { financeViewModel.setPeriod(it) }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        ClassyCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = "Distribusi Pengeluaran",
                                    color = AppColors.textPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                DonutChart(
                                    categories = uiState.summary.expenseCategories,
                                    totalAmount = uiState.summary.totalExpense
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Transaksi Terbaru",
                                color = AppColors.textPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Buka Riwayat",
                                color = AppColors.azure,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.clickable { selectedTab = 1 }
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    if (uiState.transactions.isEmpty()) {
                        item {
                            EmptyTransactionsPlaceholder()
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    } else {
                        items(uiState.transactions.take(4)) { tx ->
                            TransactionItemRow(
                                transaction = tx,
                                onClick = { onNavigateToEditTransaction(tx.id) }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            } else {
                // Tab 1: Riwayat Transaksi Lengkap (Integrated History)
                val filteredHistory = uiState.transactions.filter { tx ->
                    val matchesType = when (historyTypeFilter) {
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
                    matchesType && matchesQuery
                }

                Column(modifier = Modifier.fillMaxSize()) {
                    ClassyTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = "Cari Riwayat",
                        placeholder = "Judul, kategori, catatan...",
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = AppColors.textMuted)
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val filterOptions = listOf("Semua", "Pengeluaran", "Pemasukan", "Hutang Saya", "Hutang ke Saya")
                        items(filterOptions.size) { idx ->
                            val isSelected = historyTypeFilter == idx
                            FilterChip(
                                selected = isSelected,
                                onClick = { historyTypeFilter = idx },
                                label = { Text(filterOptions[idx], fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = when (idx) {
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

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Total ${filteredHistory.size} transaksi",
                        color = AppColors.textMuted,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (filteredHistory.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Tidak ada transaksi yang cocok",
                                color = AppColors.textMuted,
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            items(filteredHistory) { tx ->
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
                                            Text(
                                                text = "${tx.category} • ${if (tx.note.isNotBlank()) tx.note else "Tanpa catatan"}",
                                                color = AppColors.textMuted,
                                                fontSize = 12.sp,
                                                maxLines = 1
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "${if (tx.type == TransactionType.INCOME) "+ " else "- "}${formatRupiah(tx.amount)}",
                                                color = if (tx.type == TransactionType.INCOME) AppColors.emerald else AppColors.coral,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Row {
                                            IconButton(onClick = { onNavigateToEditTransaction(tx.id) }) {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = "Edit",
                                                    tint = AppColors.emerald,
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
                            item {
                                Spacer(modifier = Modifier.height(80.dp))
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = onNavigateToAddTransaction,
            containerColor = AppColors.emerald,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Tambah Transaksi")
        }
    }
}

@Composable
fun TotalBalanceCard(
    currentBalance: Double,
    expectedNetWorth: Double,
    totalDebtReceivable: Double,
    totalDebtPayable: Double
) {
    ClassyCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "HARTA SEKARANG (KAS RIIL)",
                    color = AppColors.textMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Box(
                    modifier = Modifier
                        .background(
                            if (currentBalance >= 0) AppColors.emeraldBg else AppColors.coralBg,
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (currentBalance >= 0) "Surplus" else "Defisit",
                        color = if (currentBalance >= 0) AppColors.emerald else AppColors.coral,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = formatRupiah(currentBalance),
                color = AppColors.textPrimary,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Subordinate Card: Harta Seharusnya Nanti
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = AppColors.surfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Harta Seharusnya Nanti",
                            color = AppColors.textSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        val diff = expectedNetWorth - currentBalance
                        if (diff != 0.0) {
                            Text(
                                text = if (diff > 0) "+${formatRupiah(diff)}" else "-${formatRupiah(-diff)}",
                                color = if (diff > 0) Color(0xFF38BDF8) else Color(0xFFF59E0B),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = formatRupiah(expectedNetWorth),
                        color = AppColors.textPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Estimasi total kekayaan jika hutang ke Anda (+${formatRupiah(totalDebtReceivable)}) tertagih & hutang Anda (-${formatRupiah(totalDebtPayable)}) dilunasi.",
                        color = AppColors.textMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
fun MetricsGrid(
    totalIncome: Double,
    totalExpense: Double,
    totalDebtReceivable: Double,
    totalDebtPayable: Double
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Row 1: Pemasukan & Pengeluaran
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ClassyCard(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(AppColors.emeraldBg, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = AppColors.emerald,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Pemasukan",
                            color = AppColors.textMuted,
                            fontSize = 11.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = formatRupiah(totalIncome),
                        color = AppColors.emerald,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            ClassyCard(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(AppColors.coralBg, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = null,
                                tint = AppColors.coral,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Pengeluaran",
                            color = AppColors.textMuted,
                            fontSize = 11.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = formatRupiah(totalExpense),
                        color = AppColors.coral,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Row 2: Hutang ke Saya (Piutang) & Hutang Saya (Kewajiban)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ClassyCard(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(Color(0x2038BDF8), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Hutang ke Saya",
                            color = AppColors.textMuted,
                            fontSize = 11.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = formatRupiah(totalDebtReceivable),
                        color = Color(0xFF38BDF8),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            ClassyCard(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(Color(0x20F59E0B), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Hutang Saya",
                            color = AppColors.textMuted,
                            fontSize = 11.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = formatRupiah(totalDebtPayable),
                        color = Color(0xFFF59E0B),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun PeriodFilterSelector(
    selectedPeriod: TimePeriod,
    onSelect: (TimePeriod) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(TimePeriod.values()) { period ->
            val isSelected = selectedPeriod == period
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(period) },
                label = {
                    Text(
                        text = period.title,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AppColors.emerald,
                    selectedLabelColor = Color.White,
                    containerColor = AppColors.surfaceVariant,
                    labelColor = AppColors.textSecondary
                )
            )
        }
    }
}

@Composable
fun TransactionItemRow(
    transaction: Transaction,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
    val formattedDate = dateFormat.format(Date(transaction.date))

    val (iconBg, iconTint, iconVector, prefix, amountColor) = when (transaction.type) {
        TransactionType.INCOME -> Tuple5(
            AppColors.emeraldBg,
            AppColors.emerald,
            Icons.AutoMirrored.Filled.TrendingUp,
            "+ ",
            AppColors.emerald
        )
        TransactionType.EXPENSE -> Tuple5(
            AppColors.coralBg,
            AppColors.coral,
            Icons.AutoMirrored.Filled.TrendingDown,
            "- ",
            AppColors.coral
        )
        TransactionType.DEBT_PAYABLE -> Tuple5(
            Color(0x20F59E0B),
            Color(0xFFF59E0B),
            Icons.Default.ArrowUpward,
            "Hutang: ",
            Color(0xFFF59E0B)
        )
        TransactionType.DEBT_RECEIVABLE -> Tuple5(
            Color(0x2038BDF8),
            Color(0xFF38BDF8),
            Icons.Default.ArrowDownward,
            "Piutang: ",
            Color(0xFF38BDF8)
        )
    }

    ClassyCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(iconBg, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconVector,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = transaction.title,
                        color = AppColors.textPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${transaction.category} • $formattedDate",
                        color = AppColors.textMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "$prefix${formatRupiah(transaction.amount)}",
                color = amountColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private data class Tuple5<A, B, C, D, E>(
    val a: A,
    val b: B,
    val c: C,
    val d: D,
    val e: E
)

@Composable
fun EmptyTransactionsPlaceholder() {
    ClassyCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Belum Ada Catatan Transaksi",
                color = AppColors.textSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tekan tombol + di bawah untuk menambahkan transaksi pertama Anda.",
                color = AppColors.textMuted,
                fontSize = 12.sp
            )
        }
    }
}
