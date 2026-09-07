package com.myscratch.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myscratch.app.domain.repository.CategoryTotal
import com.myscratch.app.ui.theme.AppColors
import java.text.NumberFormat
import java.util.Locale

val ChartColors = listOf(
    Color(0xFF10B981), // Emerald
    Color(0xFF0284C7), // Azure
    Color(0xFFF43F5E), // Coral
    Color(0xFFF59E0B), // Amber
    Color(0xFFA855F7), // Purple
    Color(0xFF6366F1), // Indigo
    Color(0xFF14B8A6), // Teal
    Color(0xFFEC4899)  // Pink
)

fun formatRupiah(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return formatter.format(amount).replace("Rp", "Rp ").replace(",00", "")
}

@Composable
fun DonutChart(
    categories: List<CategoryTotal>,
    totalAmount: Double,
    modifier: Modifier = Modifier,
    size: Dp = 160.dp,
    strokeWidth: Dp = 22.dp
) {
    val bgRingColor = AppColors.borderSubtle
    val textPrimary = AppColors.textPrimary
    val textSecondary = AppColors.textSecondary
    val textMuted = AppColors.textMuted

    if (categories.isEmpty() || totalAmount <= 0) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Belum ada transaksi pada periode ini",
                color = textMuted,
                fontSize = 13.sp
            )
        }
        return
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(size),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(size)) {
                // Background subtle ring
                drawCircle(
                    color = bgRingColor,
                    radius = (this.size.minDimension - strokeWidth.toPx()) / 2f,
                    style = Stroke(width = strokeWidth.toPx())
                )

                var startAngle = -90f
                categories.forEachIndexed { index, item ->
                    val sweepAngle = (item.percentage * 360f)
                    val color = ChartColors[index % ChartColors.size]
                    if (sweepAngle > 0.5f) {
                        drawArc(
                            color = color,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle - 2f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                        )
                    }
                    startAngle += sweepAngle
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Total",
                    color = textMuted,
                    fontSize = 11.sp
                )
                Text(
                    text = formatRupiah(totalAmount),
                    color = textPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Legend list
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.take(5).forEachIndexed { index, item ->
                val color = ChartColors[index % ChartColors.size]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(color, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = item.category,
                            color = textSecondary,
                            fontSize = 13.sp
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = formatRupiah(item.total),
                            color = textPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${(item.percentage * 100).toInt()}%",
                            color = textMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
