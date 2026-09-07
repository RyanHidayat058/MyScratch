package com.myscratch.app.ui.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myscratch.app.domain.model.User
import com.myscratch.app.ui.components.ClassyCard
import com.myscratch.app.ui.components.ClassyTextField
import com.myscratch.app.ui.theme.AppColors
import com.myscratch.app.viewmodel.AuthViewModel

@Composable
fun ProfileScreen(
    user: User,
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    onLogoutSuccess: () -> Unit
) {
    val context = LocalContext.current
    val uiState by authViewModel.uiState.collectAsState()

    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showChangeEmailDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Change Password Dialog States
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var passwordOtp by remember { mutableStateOf("") }
    var isPasswordOtpSent by remember { mutableStateOf(false) }

    // Change Email Dialog States
    var newEmail by remember { mutableStateOf("") }
    var emailOtp by remember { mutableStateOf("") }
    var isEmailOtpSent by remember { mutableStateOf(false) }

    // Dialog 1: Change Password
    if (showChangePasswordDialog) {
        AlertDialog(
            onDismissRequest = {
                showChangePasswordDialog = false
                isPasswordOtpSent = false
                oldPassword = ""
                newPassword = ""
                passwordOtp = ""
            },
            containerColor = AppColors.surface,
            title = {
                Text(
                    text = "Ganti Kata Sandi",
                    color = AppColors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = if (!isPasswordOtpSent)
                            "Masukkan kata sandi saat ini dan kata sandi baru. Kode OTP keamanan akan dikirimkan ke email Anda."
                        else
                            "Kode OTP telah dikirimkan ke email Anda (${user.email}). Masukkan kode 6 digit untuk verifikasi:",
                        color = AppColors.textSecondary,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    if (!isPasswordOtpSent) {
                        ClassyTextField(
                            value = oldPassword,
                            onValueChange = { oldPassword = it },
                            label = "Kata Sandi Saat Ini",
                            visualTransformation = PasswordVisualTransformation()
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        ClassyTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = "Kata Sandi Baru (Min. 6 Karakter)",
                            visualTransformation = PasswordVisualTransformation()
                        )
                    } else {
                        ClassyTextField(
                            value = passwordOtp,
                            onValueChange = { if (it.length <= 6) passwordOtp = it },
                            label = "Kode OTP 6 Digit",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (!isPasswordOtpSent) {
                            if (oldPassword.isNotBlank() && newPassword.length >= 6) {
                                authViewModel.requestChangePasswordOtp { success, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    if (success) isPasswordOtpSent = true
                                }
                            } else {
                                Toast.makeText(context, "Lengkapi kata sandi dengan benar (min 6 karakter)", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            if (passwordOtp.length == 6) {
                                authViewModel.verifyChangePassword(oldPassword, newPassword, passwordOtp) { success, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                    if (success) {
                                        showChangePasswordDialog = false
                                        isPasswordOtpSent = false
                                        oldPassword = ""
                                        newPassword = ""
                                        passwordOtp = ""
                                    }
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.emerald)
                ) {
                    Text(
                        text = if (!isPasswordOtpSent) "Minta Kode OTP" else "Simpan Kata Sandi",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showChangePasswordDialog = false
                    isPasswordOtpSent = false
                    oldPassword = ""
                    newPassword = ""
                    passwordOtp = ""
                }) {
                    Text(text = "Batal", color = AppColors.textMuted)
                }
            }
        )
    }

    // Dialog 2: Change Email
    if (showChangeEmailDialog) {
        AlertDialog(
            onDismissRequest = {
                showChangeEmailDialog = false
                isEmailOtpSent = false
                newEmail = ""
                emailOtp = ""
            },
            containerColor = AppColors.surface,
            title = {
                Text(
                    text = "Ganti Alamat Email",
                    color = AppColors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = if (!isEmailOtpSent)
                            "Masukkan alamat email baru Anda. Kode OTP verifikasi akan dikirimkan ke email baru tersebut."
                        else
                            "Kode OTP telah dikirimkan ke email baru ($newEmail). Masukkan kode 6 digit:",
                        color = AppColors.textSecondary,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    if (!isEmailOtpSent) {
                        ClassyTextField(
                            value = newEmail,
                            onValueChange = { newEmail = it },
                            label = "Alamat Email Baru",
                            placeholder = "nama@emailbaru.com"
                        )
                    } else {
                        ClassyTextField(
                            value = emailOtp,
                            onValueChange = { if (it.length <= 6) emailOtp = it },
                            label = "Kode OTP 6 Digit",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (!isEmailOtpSent) {
                            if (newEmail.isNotBlank() && newEmail.contains("@")) {
                                authViewModel.requestChangeEmailOtp(newEmail) { success, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    if (success) isEmailOtpSent = true
                                }
                            } else {
                                Toast.makeText(context, "Masukkan email yang valid", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            if (emailOtp.length == 6) {
                                authViewModel.verifyChangeEmail(newEmail, emailOtp) { success, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                    if (success) {
                                        showChangeEmailDialog = false
                                        isEmailOtpSent = false
                                        newEmail = ""
                                        emailOtp = ""
                                    }
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.emerald)
                ) {
                    Text(
                        text = if (!isEmailOtpSent) "Minta Kode OTP" else "Konfirmasi Email Baru",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showChangeEmailDialog = false
                    isEmailOtpSent = false
                    newEmail = ""
                    emailOtp = ""
                }) {
                    Text(text = "Batal", color = AppColors.textMuted)
                }
            }
        )
    }

    // Dialog 3: Delete Account Confirmation
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            containerColor = AppColors.surface,
            title = {
                Text(
                    text = "Hapus Akun Permanen?",
                    color = AppColors.coral,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "PERINGATAN: Tindakan ini tidak dapat dibatalkan. Seluruh data transaksi keuangan, hutang piutang, dan catatan pribadi Anda akan dihapus secara permanen dari server.",
                    color = AppColors.textSecondary,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAccountDialog = false
                        authViewModel.deleteAccount { success, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            if (success) {
                                onLogoutSuccess()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.coral)
                ) {
                    Text(text = "Hapus Akun Saya", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text(text = "Batal", color = AppColors.textMuted)
                }
            }
        )
    }

    // Dialog 4: Logout Confirmation
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
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
                    text = "Apakah Anda yakin ingin keluar dari akun MyScratch ini?",
                    color = AppColors.textSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    authViewModel.logout()
                    onLogoutSuccess()
                }) {
                    Text(text = "Keluar", color = AppColors.coral, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
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
            .verticalScroll(rememberScrollState())
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
                text = "Profil & Akun Pengguna",
                color = AppColors.textPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Profile Identity Card
        ClassyCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(AppColors.emeraldBg, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.name.take(1).uppercase(),
                        color = AppColors.emerald,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = user.name,
                        color = AppColors.textPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = user.email,
                        color = AppColors.textSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = AppColors.emerald.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "Akun Terverifikasi",
                            color = AppColors.emerald,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Pengaturan Keamanan & Akun",
            color = AppColors.textPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Setting Options Card
        ClassyCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(8.dp)) {
                ProfileOptionRow(
                    icon = Icons.Default.Lock,
                    iconTint = AppColors.azure,
                    title = "Ganti Kata Sandi",
                    subtitle = "Perbarui kata sandi Anda dengan verifikasi OTP email",
                    onClick = { showChangePasswordDialog = true }
                )

                Divider(color = AppColors.border, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 12.dp))

                ProfileOptionRow(
                    icon = Icons.Default.Email,
                    iconTint = AppColors.emerald,
                    title = "Ganti Alamat Email",
                    subtitle = "Ubah email yang terdaftar dengan kode OTP baru",
                    onClick = { showChangeEmailDialog = true }
                )

                Divider(color = AppColors.border, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 12.dp))

                ProfileOptionRow(
                    icon = Icons.AutoMirrored.Filled.Logout,
                    iconTint = AppColors.textMuted,
                    title = "Keluar dari Akun",
                    subtitle = "Logout dari sesi MyScratch pada perangkat ini",
                    onClick = { showLogoutDialog = true }
                )

                Divider(color = AppColors.border, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 12.dp))

                ProfileOptionRow(
                    icon = Icons.Default.DeleteForever,
                    iconTint = AppColors.coral,
                    title = "Hapus Akun Permanen",
                    subtitle = "Hapus akun dan semua riwayat data keuangan & catatan",
                    titleColor = AppColors.coral,
                    onClick = { showDeleteAccountDialog = true }
                )
            }
        }

        Spacer(modifier = Modifier.height(36.dp))
    }
}

@Composable
fun ProfileOptionRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    titleColor: Color = AppColors.textPrimary,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(iconTint.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = titleColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = AppColors.textMuted,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}
