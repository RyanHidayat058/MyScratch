package com.myscratch.app.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myscratch.app.R
import com.myscratch.app.domain.model.User
import com.myscratch.app.ui.components.ClassyCard
import com.myscratch.app.ui.components.formatRupiah
import com.myscratch.app.ui.theme.AppColors
import com.myscratch.app.ui.theme.LocalThemeState
import com.myscratch.app.viewmodel.FinanceViewModel

@Composable
fun HomeScreen(
    user: User,
    financeViewModel: FinanceViewModel,
    onNavigateToFinance: () -> Unit,
    onNavigateToNotes: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onOpenCalculator: () -> Unit,
    onLogout: () -> Unit,
    isTablet: Boolean = false
) {
    val themeState = LocalThemeState.current
    var isLogoutDialogOpen by remember { mutableStateOf(false) }

    if (isLogoutDialogOpen) {
        AlertDialog(
            onDismissRequest = { isLogoutDialogOpen = false },
            containerColor = AppColors.surface,
            title = {
                Text(
                    text = "Konfirmasi Logout",
                    color = AppColors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Apakah Anda yakin ingin keluar dari akun ini?",
                    color = AppColors.textSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    isLogoutDialogOpen = false
                    onLogout()
                }) {
                    Text(
                        text = "Keluar",
                        color = AppColors.coral,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { isLogoutDialogOpen = false }) {
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

        // Top Header: Logo, App Name, Theme Switcher, and Logout
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.ic_myscratch_logo),
                    contentDescription = "MyScratch Logo",
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "MyScratch",
                        color = AppColors.textPrimary,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Dashboard & Manajemen",
                        color = AppColors.textMuted,
                        fontSize = 12.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Theme Toggle Button
                Surface(
                    shape = CircleShape,
                    color = AppColors.surfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.border),
                    modifier = Modifier.clickable { themeState.toggle() }
                ) {
                    Box(
                        modifier = Modifier.padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (themeState.isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Ganti Tema",
                            tint = if (themeState.isDark) Color(0xFFF59E0B) else Color(0xFF6366F1),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Logout Button
                Surface(
                    shape = CircleShape,
                    color = AppColors.surfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.border),
                    modifier = Modifier.clickable { isLogoutDialogOpen = true }
                ) {
                    Box(
                        modifier = Modifier.padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Logout",
                            tint = AppColors.coral,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Welcome Profile Card (Clickable to Profile Screen)
        ClassyCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToProfile() }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Selamat Datang,",
                        color = AppColors.textMuted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = user.name,
                        color = AppColors.textPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val todayFormat = remember { java.text.SimpleDateFormat("EEEE, dd MMMM yyyy", java.util.Locale("id", "ID")) }
                    Text(
                        text = todayFormat.format(java.util.Date()),
                        color = AppColors.textSecondary,
                        fontSize = 12.sp
                    )
                }

                // User Profile Avatar
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(AppColors.emeraldBg, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.name.take(1).uppercase(),
                        color = AppColors.emerald,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Menu & Fitur Aplikasi",
            color = AppColors.textPrimary,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Pilih fitur yang ingin Anda gunakan",
            color = AppColors.textMuted,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Feature App Grid - Exactly 2 features currently
        LazyVerticalGrid(
            columns = GridCells.Fixed(if (isTablet) 2 else 2),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Feature 1: Catatan Keuangan
            item {
                FeatureGridCard(
                    title = "Catatan Keuangan",
                    subtitle = "Kelola saldo, transaksi harian, hutang piutang, dan grafik analisis",
                    icon = Icons.Default.AccountBalanceWallet,
                    iconTint = AppColors.emerald,
                    iconBg = AppColors.emeraldBg,
                    badgeText = "Aktif",
                    badgeColor = AppColors.emerald,
                    onClick = onNavigateToFinance
                )
            }

            // Feature 2: Catatan Pribadi
            item {
                FeatureGridCard(
                    title = "Catatan Pribadi",
                    subtitle = "Folder kustom, ide penting, dan arsip dokumen pribadi",
                    icon = Icons.Default.Folder,
                    iconTint = AppColors.azure,
                    iconBg = AppColors.azureBg,
                    badgeText = "Aktif",
                    badgeColor = AppColors.azure,
                    onClick = onNavigateToNotes
                )
            }
        }
    }
}

@Composable
fun FeatureGridCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    badgeText: String,
    badgeColor: Color,
    onClick: () -> Unit
) {
    ClassyCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(iconBg, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = badgeColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = badgeText,
                        color = badgeColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Column {
                Text(
                    text = title,
                    color = AppColors.textPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    color = AppColors.textMuted,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    maxLines = 2
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
