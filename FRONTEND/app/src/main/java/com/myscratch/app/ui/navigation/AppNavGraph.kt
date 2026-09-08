package com.myscratch.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.myscratch.app.MyScratchApp
import com.myscratch.app.ui.auth.LoginScreen
import com.myscratch.app.ui.auth.OtpVerificationScreen
import com.myscratch.app.ui.auth.RegisterScreen
import com.myscratch.app.ui.components.PopupCalculatorDialog
import com.myscratch.app.ui.finance.AddEditTransactionScreen
import com.myscratch.app.ui.finance.FinanceDashboardScreen
import com.myscratch.app.ui.home.HomeScreen
import com.myscratch.app.ui.notes.NoteEditorScreen
import com.myscratch.app.ui.notes.NotesMainScreen
import com.myscratch.app.ui.profile.ProfileScreen
import com.myscratch.app.ui.vault.VaultScreen
import com.myscratch.app.viewmodel.AuthViewModel
import com.myscratch.app.viewmodel.FinanceViewModel
import com.myscratch.app.viewmodel.NotesViewModel
import com.myscratch.app.viewmodel.VaultViewModel
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModel.provideFactory(MyScratchApp.instance.authRepository)
    ),
    onGoogleSignInClick: () -> Unit,
    isTablet: Boolean = false
) {
    val authState by authViewModel.uiState.collectAsState()
    val currentUser = authState.currentUser

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Login.route

    var isGlobalCalculatorOpen by remember { mutableStateOf(false) }

    PopupCalculatorDialog(
        isOpen = isGlobalCalculatorOpen,
        onDismiss = { isGlobalCalculatorOpen = false }
    )

    // Smooth navigation reaction when auth state changes
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            if (currentRoute == Screen.Login.route || currentRoute == Screen.Register.route || currentRoute.startsWith("otp")) {
                navController.navigate(Screen.Home.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        } else {
            if (currentRoute != Screen.Login.route && currentRoute != Screen.Register.route && !currentRoute.startsWith("otp")) {
                navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    // ViewModels for the active user
    val activeUserId = currentUser?.id ?: "guest"
    val financeViewModel: FinanceViewModel = viewModel(
        key = "finance_$activeUserId",
        factory = FinanceViewModel.provideFactory(
            MyScratchApp.instance.financeRepository,
            activeUserId
        )
    )

    val notesViewModel: NotesViewModel = viewModel(
        key = "notes_$activeUserId",
        factory = NotesViewModel.provideFactory(
            MyScratchApp.instance.notesRepository,
            activeUserId
        )
    )

    val vaultViewModel: VaultViewModel = viewModel(
        key = "vault_$activeUserId",
        factory = VaultViewModel.provideFactory(
            MyScratchApp.instance.vaultRepository,
            activeUserId
        )
    )

    // Sync finance view model calculator dialog
    val financeUiState by financeViewModel.uiState.collectAsState()
    PopupCalculatorDialog(
        isOpen = financeUiState.isCalculatorOpen,
        onDismiss = { financeViewModel.closeCalculator() },
        onUseResult = { calculatedValue ->
            financeViewModel.setAmountFromCalculator(calculatedValue)
        }
    )

    NavHost(
        navController = navController,
        startDestination = if (currentUser == null) Screen.Login.route else Screen.Home.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                authViewModel = authViewModel,
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onNavigateToOtp = { email ->
                    navController.navigate(Screen.Otp.createRoute(email))
                },
                onGoogleSignInClick = onGoogleSignInClick
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                authViewModel = authViewModel,
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onNavigateToOtp = { email ->
                    navController.navigate(Screen.Otp.createRoute(email))
                }
            )
        }

        composable(
            route = Screen.Otp.route,
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val rawEmail = backStackEntry.arguments?.getString("email") ?: ""
            val decodedEmail = try {
                URLDecoder.decode(rawEmail, StandardCharsets.UTF_8.toString())
            } catch (_: Exception) {
                rawEmail
            }

            OtpVerificationScreen(
                email = decodedEmail,
                authViewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() },
                onSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Profile.route) {
            if (currentUser != null) {
                ProfileScreen(
                    user = currentUser,
                    authViewModel = authViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onLogoutSuccess = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }

        composable(Screen.Home.route) {
            if (currentUser != null) {
                HomeScreen(
                    user = currentUser,
                    financeViewModel = financeViewModel,
                    notesViewModel = notesViewModel,
                    vaultViewModel = vaultViewModel,
                    onNavigateToFinance = {
                        navController.navigate(Screen.Finance.route)
                    },
                    onNavigateToNotes = {
                        navController.navigate(Screen.Notes.route)
                    },
                    onNavigateToVault = {
                        navController.navigate(Screen.Vault.route)
                    },
                    onNavigateToProfile = {
                        navController.navigate(Screen.Profile.route)
                    },
                    onOpenCalculator = { isGlobalCalculatorOpen = true },
                    onLogout = { authViewModel.logout() },
                    isTablet = isTablet
                )
            }
        }

        composable(Screen.Finance.route) {
            FinanceDashboardScreen(
                userName = currentUser?.name ?: "Pengguna",
                financeViewModel = financeViewModel,
                onNavigateBack = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                    }
                },
                onNavigateToAddTransaction = {
                    navController.navigate(Screen.AddTransaction.route)
                },
                onNavigateToEditTransaction = { id ->
                    navController.navigate(Screen.EditTransaction.createRoute(id))
                },
                isTablet = isTablet
            )
        }

        composable(Screen.Notes.route) {
            NotesMainScreen(
                notesViewModel = notesViewModel,
                onNavigateBack = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                    }
                },
                onNavigateToEditNote = { noteId, folderId ->
                    navController.navigate(Screen.EditNote.createRoute(noteId, folderId))
                },
                isTablet = isTablet
            )
        }

        composable(Screen.AddTransaction.route) {
            AddEditTransactionScreen(
                transactionId = null,
                financeViewModel = financeViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.EditTransaction.route,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStack ->
            val id = backStack.arguments?.getString("id")
            AddEditTransactionScreen(
                transactionId = id,
                financeViewModel = financeViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.EditNote.route,
            arguments = listOf(
                navArgument("noteId") { type = NavType.StringType },
                navArgument("folderId") { type = NavType.StringType }
            )
        ) { backStack ->
            val noteId = backStack.arguments?.getString("noteId") ?: "new"
            val folderId = backStack.arguments?.getString("folderId") ?: "general"
            NoteEditorScreen(
                noteId = noteId,
                folderId = folderId,
                notesViewModel = notesViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Vault.route) {
            VaultScreen(
                vaultViewModel = vaultViewModel,
                onNavigateBack = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                    }
                },
                isTablet = isTablet
            )
        }
    }
}
