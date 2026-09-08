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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myscratch.app.ui.theme.AppColors
import com.myscratch.app.viewmodel.VaultViewModel

@Composable
fun PasswordGeneratorDialog(
    vaultViewModel: VaultViewModel,
    onDismissRequest: () -> Unit,
    onPasswordSelected: ((String) -> Unit)? = null
) {
    val context = LocalContext.current

    var length by remember { mutableFloatStateOf(16f) }
    var includeUpper by remember { mutableStateOf(true) }
    var includeLower by remember { mutableStateOf(true) }
    var includeNumbers by remember { mutableStateOf(true) }
    var includeSymbols by remember { mutableStateOf(true) }

    var generatedPassword by remember { mutableStateOf("") }

    fun regenerate() {
        generatedPassword = vaultViewModel.generateStrongPassword(
            length = length.toInt(),
            includeUpper = includeUpper,
            includeLower = includeLower,
            includeNumbers = includeNumbers,
            includeSymbols = includeSymbols
        )
    }

    LaunchedEffect(length, includeUpper, includeLower, includeNumbers, includeSymbols) {
        regenerate()
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        containerColor = AppColors.surface,
        title = {
            Text(
                text = "Generator Password Kuat",
                color = AppColors.textPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Generated Password Box
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AppColors.surfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = generatedPassword,
                            color = AppColors.violet,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.weight(1f)
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { regenerate() },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Buat Ulang",
                                    tint = AppColors.textSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Password", generatedPassword)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Password disalin ke clipboard", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Salin",
                                    tint = AppColors.azure,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Length Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Panjang Karakter",
                        color = AppColors.textSecondary,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "${length.toInt()}",
                        color = AppColors.violet,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Slider(
                    value = length,
                    onValueChange = { length = it },
                    valueRange = 8f..32f,
                    steps = 23,
                    colors = SliderDefaults.colors(
                        thumbColor = AppColors.violet,
                        activeTrackColor = AppColors.violet,
                        inactiveTrackColor = AppColors.border
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Switches
                GeneratorOptionRow(
                    label = "Huruf Besar (A-Z)",
                    checked = includeUpper,
                    onCheckedChange = { includeUpper = it }
                )
                GeneratorOptionRow(
                    label = "Huruf Kecil (a-z)",
                    checked = includeLower,
                    onCheckedChange = { includeLower = it }
                )
                GeneratorOptionRow(
                    label = "Angka (0-9)",
                    checked = includeNumbers,
                    onCheckedChange = { includeNumbers = it }
                )
                GeneratorOptionRow(
                    label = "Simbol Khusus (!@#$)",
                    checked = includeSymbols,
                    onCheckedChange = { includeSymbols = it }
                )
            }
        },
        confirmButton = {
            if (onPasswordSelected != null) {
                Button(
                    onClick = {
                        onPasswordSelected(generatedPassword)
                        onDismissRequest()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.violet)
                ) {
                    Text(text = "Gunakan Password", color = Color.White, fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = onDismissRequest,
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.violet)
                ) {
                    Text(text = "Tutup", color = Color.White)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = "Batal", color = AppColors.textMuted)
            }
        }
    )
}

@Composable
private fun GeneratorOptionRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = AppColors.textPrimary,
            fontSize = 13.sp
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = AppColors.violet,
                uncheckedTrackColor = AppColors.border
            )
        )
    }
}
