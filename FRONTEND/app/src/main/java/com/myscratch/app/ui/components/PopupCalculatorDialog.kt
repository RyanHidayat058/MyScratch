package com.myscratch.app.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.myscratch.app.ui.theme.AppColors
import com.myscratch.app.ui.theme.LocalThemeState
import java.text.DecimalFormat

@Composable
fun PopupCalculatorDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onUseResult: ((Double) -> Unit)? = null
) {
    if (!isOpen) return

    var expression by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf("0") }

    fun evaluate() {
        try {
            val sanitized = expression.replace("×", "*").replace("÷", "/").replace("−", "-")
            val calculated = evalMathExpression(sanitized)
            val formatter = DecimalFormat("#.##")
            resultText = formatter.format(calculated)
        } catch (_: Exception) {
            resultText = "Error"
        }
    }

    fun onKeyClick(key: String) {
        when (key) {
            "C" -> {
                expression = ""
                resultText = "0"
            }
            "DEL" -> {
                if (expression.isNotEmpty()) {
                    expression = expression.dropLast(1)
                    if (expression.isNotEmpty()) evaluate() else resultText = "0"
                }
            }
            "=" -> {
                evaluate()
                if (resultText != "Error") {
                    expression = resultText
                }
            }
            "+", "−", "×", "÷", "%" -> {
                if (expression.isNotEmpty()) {
                    val last = expression.last().toString()
                    if (last in listOf("+", "−", "×", "÷", "%")) {
                        expression = expression.dropLast(1) + key
                    } else {
                        expression += key
                    }
                }
            }
            else -> {
                expression += key
                evaluate()
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = AppColors.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.border),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header with Title & Close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Kalkulator Cepat",
                        color = AppColors.textPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = AppColors.textMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Display Area
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppColors.surfaceVariant, RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = if (expression.isEmpty()) "0" else expression,
                        color = AppColors.textMuted,
                        fontSize = 16.sp,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = resultText,
                        color = AppColors.textPrimary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Keypad Layout
                val buttons = listOf(
                    listOf("C", "DEL", "%", "÷"),
                    listOf("7", "8", "9", "×"),
                    listOf("4", "5", "6", "−"),
                    listOf("1", "2", "3", "+"),
                    listOf("0", ".", "=")
                )

                val isDark = LocalThemeState.current.isDark

                buttons.forEach { row ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { key ->
                            val isOperator = key in listOf("÷", "×", "−", "+", "%")
                            val isClear = key in listOf("C", "DEL")
                            val isEqual = key == "="
                            val weight = if (key == "0") 2f else 1f

                            Box(
                                modifier = Modifier
                                    .weight(weight)
                                    .height(52.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        when {
                                            isEqual -> AppColors.emerald
                                            isOperator -> AppColors.azureBg
                                            isClear -> AppColors.coralBg
                                            else -> AppColors.surfaceVariant
                                        }
                                    )
                                    .clickable { onKeyClick(key) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = key,
                                    color = when {
                                        isEqual -> if (isDark) Color(0xFF0B0D11) else Color.White
                                        isOperator -> AppColors.azure
                                        isClear -> AppColors.coral
                                        else -> AppColors.textPrimary
                                    },
                                    fontSize = 18.sp,
                                    fontWeight = if (isEqual || isOperator) FontWeight.Bold else FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                // If caller wants to use the result
                if (onUseResult != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val value = resultText.toDoubleOrNull() ?: 0.0
                            if (value > 0) {
                                onUseResult(value)
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.emerald),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(
                            text = "Gunakan Nilai Ini",
                            color = if (isDark) Color(0xFF0B0D11) else Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

private fun evalMathExpression(str: String): Double {
    return object : Any() {
        var pos = -1
        var ch = 0

        fun nextChar() {
            ch = if (++pos < str.length) str[pos].code else -1
        }

        fun eat(charToEat: Int): Boolean {
            while (ch == ' '.code) nextChar()
            if (ch == charToEat) {
                nextChar()
                return true
            }
            return false
        }

        fun parse(): Double {
            nextChar()
            val x = parseExpression()
            if (pos < str.length) throw RuntimeException("Unexpected: " + ch.toChar())
            return x
        }

        fun parseExpression(): Double {
            var x = parseTerm()
            while (true) {
                if (eat('+'.code)) x += parseTerm()
                else if (eat('-'.code)) x -= parseTerm()
                else return x
            }
        }

        fun parseTerm(): Double {
            var x = parseFactor()
            while (true) {
                if (eat('*'.code)) x *= parseFactor()
                else if (eat('/'.code)) x /= parseFactor()
                else if (eat('%'.code)) x = (x / 100.0)
                else return x
            }
        }

        fun parseFactor(): Double {
            if (eat('+'.code)) return +parseFactor()
            if (eat('-'.code)) return -parseFactor()

            var x: Double
            val startPos = pos
            if (eat('('.code)) {
                x = parseExpression()
                eat(')'.code)
            } else if ((ch in '0'.code..'9'.code) || ch == '.'.code) {
                while ((ch in '0'.code..'9'.code) || ch == '.'.code) nextChar()
                x = str.substring(startPos, pos).toDouble()
            } else {
                throw RuntimeException("Unexpected: " + ch.toChar())
            }
            return x
        }
    }.parse()
}
