package com.myscratch.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.myscratch.app.ui.theme.AppColors

enum class NavItem(val title: String, val icon: ImageVector, val route: String) {
    FINANCE("Keuangan", Icons.Default.AccountBalanceWallet, "finance"),
    HISTORY("Riwayat", Icons.Default.History, "history"),
    NOTES("Catatan", Icons.Default.Folder, "notes")
}

@Composable
fun ResponsiveScaffold(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onOpenCalculator: () -> Unit,
    onLogout: () -> Unit,
    content: @Composable (isTablet: Boolean) -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(AppColors.background)) {
        val isTablet = maxWidth >= 600.dp

        if (isTablet) {
            // Tablet / Desktop Layout: Navigation Rail on left side
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail(
                    containerColor = AppColors.surface,
                    contentColor = AppColors.textPrimary,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    NavItem.values().forEach { item ->
                        val selected = currentRoute == item.route
                        NavigationRailItem(
                            selected = selected,
                            onClick = { onNavigate(item.route) },
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            label = { Text(item.title) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = AppColors.background,
                                selectedTextColor = AppColors.emerald,
                                indicatorColor = AppColors.emerald,
                                unselectedIconColor = AppColors.textMuted,
                                unselectedTextColor = AppColors.textMuted
                            )
                        )
                    }

                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))

                    NavigationRailItem(
                        selected = false,
                        onClick = onOpenCalculator,
                        icon = { Icon(Icons.Default.Calculate, contentDescription = "Kalkulator") },
                        label = { Text("Hitung") },
                        colors = NavigationRailItemDefaults.colors(
                            unselectedIconColor = AppColors.emerald,
                            unselectedTextColor = AppColors.emerald
                        )
                    )

                    NavigationRailItem(
                        selected = false,
                        onClick = onLogout,
                        icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Keluar") },
                        label = { Text("Logout") },
                        colors = NavigationRailItemDefaults.colors(
                            unselectedIconColor = AppColors.textMuted,
                            unselectedTextColor = AppColors.textMuted
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                        .background(AppColors.background)
                ) {
                    content(true)
                }
            }
        } else {
            // Mobile Phone Layout: Standard Bottom Navigation Bar
            Scaffold(
                containerColor = AppColors.background,
                bottomBar = {
                    NavigationBar(
                        containerColor = AppColors.surface,
                        contentColor = AppColors.textPrimary
                    ) {
                        NavItem.values().forEach { item ->
                            val selected = currentRoute == item.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = { onNavigate(item.route) },
                                icon = { Icon(item.icon, contentDescription = item.title) },
                                label = { Text(item.title) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = AppColors.background,
                                    selectedTextColor = AppColors.emerald,
                                    indicatorColor = AppColors.emerald,
                                    unselectedIconColor = AppColors.textMuted,
                                    unselectedTextColor = AppColors.textMuted
                                )
                            )
                        }
                    }
                }
            ) { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(AppColors.background)
                ) {
                    content(false)
                }
            }
        }
    }
}
