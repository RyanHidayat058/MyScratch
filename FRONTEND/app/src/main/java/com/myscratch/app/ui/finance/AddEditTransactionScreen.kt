package com.myscratch.app.ui.finance

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myscratch.app.domain.model.TransactionType
import com.myscratch.app.ui.components.ClassyCard
import com.myscratch.app.ui.components.ClassyTextField
import com.myscratch.app.ui.components.PopupCalculatorDialog
import com.myscratch.app.ui.theme.AppColors
import com.myscratch.app.viewmodel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val DefaultIncomeCategories = listOf("Gaji", "Bonus", "Bisnis", "Investasi", "Hadiah", "Lainnya")
val DefaultExpenseCategories = listOf("Makanan", "Belanja", "Transportasi", "Tagihan", "Hiburan", "Kesehatan", "Edukasi", "Lainnya")
val DefaultDebtPayableCategories = listOf("Pinjaman Teman", "Pinjaman Keluarga", "Bank / Cicilan", "Kartu Kredit", "Modal Usaha", "Lainnya")
val DefaultDebtReceivableCategories = listOf("Dipinjam Teman", "Dipinjam Keluarga", "Rekan Kerja", "Talangan Belanja", "Piutang Usaha", "Lainnya")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTransactionScreen(
    transactionId: String?,
    financeViewModel: FinanceViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by financeViewModel.uiState.collectAsState()

    val existingTransaction = remember(transactionId, uiState.transactions) {
        if (transactionId != null) {
            uiState.transactions.find { it.id == transactionId }
        } else null
    }

    var title by remember { mutableStateOf(existingTransaction?.title ?: "") }
    var amountText by remember {
        mutableStateOf(
            if (existingTransaction != null) {
                if (existingTransaction.amount % 1.0 == 0.0) {
                    existingTransaction.amount.toLong().toString()
                } else {
                    existingTransaction.amount.toString()
                }
            } else ""
        )
    }
    var selectedType by remember { mutableStateOf(existingTransaction?.type ?: TransactionType.EXPENSE) }
    var selectedCategory by remember {
        mutableStateOf(
            existingTransaction?.category ?: when (selectedType) {
                TransactionType.INCOME -> DefaultIncomeCategories.first()
                TransactionType.EXPENSE -> DefaultExpenseCategories.first()
                TransactionType.DEBT_PAYABLE -> DefaultDebtPayableCategories.first()
                TransactionType.DEBT_RECEIVABLE -> DefaultDebtReceivableCategories.first()
            }
        )
    }
    var selectedDate by remember { mutableLongStateOf(existingTransaction?.date ?: System.currentTimeMillis()) }
    var showDatePickerDialog by remember { mutableStateOf(false) }
    var note by remember { mutableStateOf(existingTransaction?.note ?: "") }
    var isCalculatorVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.pendingAmountFromCalc) {
        uiState.pendingAmountFromCalc?.let {
            amountText = if (it % 1.0 == 0.0) it.toLong().toString() else it.toString()
            financeViewModel.consumePendingAmount()
        }
    }

    // Material 3 Date Picker Dialog
    if (showDatePickerDialog) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate
        )
        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            selectedDate = millis
                        }
                        showDatePickerDialog = false
                    }
                ) {
                    Text("Pilih", color = AppColors.emerald, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false }) {
                    Text("Batal", color = AppColors.textMuted)
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = AppColors.surface
            )
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = AppColors.surface,
                    titleContentColor = AppColors.textPrimary,
                    headlineContentColor = AppColors.textPrimary,
                    selectedDayContainerColor = AppColors.emerald,
                    selectedDayContentColor = Color.White,
                    todayContentColor = AppColors.azure,
                    todayDateBorderColor = AppColors.azure
                )
            )
        }
    }

    PopupCalculatorDialog(
        isOpen = isCalculatorVisible,
        onDismiss = { isCalculatorVisible = false },
        onUseResult = { calculatedValue ->
            amountText = if (calculatedValue % 1.0 == 0.0) calculatedValue.toLong().toString() else calculatedValue.toString()
            isCalculatorVisible = false
        }
    )

    val currentThemeColor = when (selectedType) {
        TransactionType.EXPENSE -> AppColors.coral
        TransactionType.INCOME -> AppColors.emerald
        TransactionType.DEBT_PAYABLE -> Color(0xFFF59E0B) // Amber
        TransactionType.DEBT_RECEIVABLE -> Color(0xFF38BDF8) // Azure
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

        // Top Bar with back button - padded safely below Android status bar
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
                    text = if (existingTransaction != null) "Edit Transaksi" else "Tambah Transaksi",
                    color = AppColors.textPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (existingTransaction != null) {
                IconButton(
                    onClick = {
                        financeViewModel.deleteTransaction(existingTransaction.id)
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

        // Type Selector with 4 types: Pengeluaran, Pemasukan, Hutang Saya, Hutang ke Saya
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColors.surfaceVariant, RoundedCornerShape(14.dp))
                .padding(4.dp)
        ) {
            // Row 1: Pengeluaran & Pemasukan
            Row(modifier = Modifier.fillMaxWidth()) {
                val isExpense = selectedType == TransactionType.EXPENSE
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (isExpense) AppColors.coral else Color.Transparent,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable {
                            selectedType = TransactionType.EXPENSE
                            if (selectedCategory !in DefaultExpenseCategories) {
                                selectedCategory = DefaultExpenseCategories.first()
                            }
                        }
                        .padding(vertical = 11.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Pengeluaran",
                        color = if (isExpense) Color.White else AppColors.textMuted,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                val isIncome = selectedType == TransactionType.INCOME
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (isIncome) AppColors.emerald else Color.Transparent,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable {
                            selectedType = TransactionType.INCOME
                            if (selectedCategory !in DefaultIncomeCategories) {
                                selectedCategory = DefaultIncomeCategories.first()
                            }
                        }
                        .padding(vertical = 11.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Pemasukan",
                        color = if (isIncome) Color.White else AppColors.textMuted,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Row 2: Hutang Saya & Hutang ke Saya
            Row(modifier = Modifier.fillMaxWidth()) {
                val isDebtPayable = selectedType == TransactionType.DEBT_PAYABLE
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (isDebtPayable) Color(0xFFF59E0B) else Color.Transparent,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable {
                            selectedType = TransactionType.DEBT_PAYABLE
                            if (selectedCategory !in DefaultDebtPayableCategories) {
                                selectedCategory = DefaultDebtPayableCategories.first()
                            }
                        }
                        .padding(vertical = 11.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Hutang Saya",
                        color = if (isDebtPayable) Color.White else AppColors.textMuted,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                val isDebtReceivable = selectedType == TransactionType.DEBT_RECEIVABLE
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (isDebtReceivable) Color(0xFF38BDF8) else Color.Transparent,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable {
                            selectedType = TransactionType.DEBT_RECEIVABLE
                            if (selectedCategory !in DefaultDebtReceivableCategories) {
                                selectedCategory = DefaultDebtReceivableCategories.first()
                            }
                        }
                        .padding(vertical = 11.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Hutang ke Saya",
                        color = if (isDebtReceivable) Color.White else AppColors.textMuted,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        ClassyCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Title Field
                ClassyTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = when (selectedType) {
                        TransactionType.EXPENSE -> "Judul Pengeluaran"
                        TransactionType.INCOME -> "Judul Pemasukan"
                        TransactionType.DEBT_PAYABLE -> "Keterangan Hutang Saya"
                        TransactionType.DEBT_RECEIVABLE -> "Keterangan Hutang ke Saya"
                    },
                    placeholder = when (selectedType) {
                        TransactionType.EXPENSE -> "e.g. Makan Siang, Beli Bensin"
                        TransactionType.INCOME -> "e.g. Gaji Pokok, Freelance"
                        TransactionType.DEBT_PAYABLE -> "e.g. Pinjam Dana ke Budi, Cicilan Laptop"
                        TransactionType.DEBT_RECEIVABLE -> "e.g. Dipinjam Anton, Talangan Makan"
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Title, contentDescription = null, tint = AppColors.textMuted)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Date Picker Field
                val dateFormatter = remember { SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID")) }
                val formattedDate = remember(selectedDate) { dateFormatter.format(Date(selectedDate)) }

                Column {
                    Text(
                        text = "Tanggal Transaksi",
                        color = AppColors.textSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = AppColors.surfaceVariant,
                        border = BorderStroke(1.dp, AppColors.border),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDatePickerDialog = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = "Pilih Tanggal",
                                    tint = currentThemeColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = formattedDate,
                                    color = AppColors.textPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Text(
                                text = "Ubah",
                                color = currentThemeColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Amount with Calculator Shortcut
                ClassyTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = "Nominal (Rp)",
                    placeholder = "0",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    trailingIcon = {
                        IconButton(onClick = { isCalculatorVisible = true }) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = "Buka Kalkulator",
                                tint = currentThemeColor
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Category selection
                Text(
                    text = "Pilih Kategori",
                    color = AppColors.textSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))

                val categories = when (selectedType) {
                    TransactionType.INCOME -> DefaultIncomeCategories
                    TransactionType.EXPENSE -> DefaultExpenseCategories
                    TransactionType.DEBT_PAYABLE -> DefaultDebtPayableCategories
                    TransactionType.DEBT_RECEIVABLE -> DefaultDebtReceivableCategories
                }

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { cat ->
                        val isSelected = selectedCategory == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 13.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = currentThemeColor,
                                selectedLabelColor = Color.White,
                                containerColor = AppColors.surfaceVariant,
                                labelColor = AppColors.textSecondary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Note
                ClassyTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = "Catatan Tambahan (Opsional)",
                    placeholder = "Keterangan, tenggat waktu, atau nomor kontak...",
                    singleLine = false,
                    maxLines = 3,
                    leadingIcon = {
                        Icon(Icons.Default.Description, contentDescription = null, tint = AppColors.textMuted)
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Save button
                Button(
                    onClick = {
                        val parsedAmount = amountText.toDoubleOrNull() ?: 0.0
                        if (title.isNotBlank() && parsedAmount > 0) {
                            if (existingTransaction != null) {
                                financeViewModel.updateTransaction(
                                    existingTransaction.copy(
                                        title = title.trim(),
                                        amount = parsedAmount,
                                        type = selectedType,
                                        category = selectedCategory,
                                        date = selectedDate,
                                        note = note.trim()
                                    ),
                                    onSuccess = onNavigateBack
                                )
                            } else {
                                financeViewModel.addTransaction(
                                    title = title.trim(),
                                    amount = parsedAmount,
                                    type = selectedType,
                                    category = selectedCategory,
                                    date = selectedDate,
                                    note = note.trim(),
                                    onSuccess = onNavigateBack
                                )
                            }
                        }
                    },
                    enabled = title.isNotBlank() && (amountText.toDoubleOrNull() ?: 0.0) > 0,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = currentThemeColor
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(
                        text = if (existingTransaction != null) "Simpan Perubahan" else "Simpan Transaksi",
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
