package com.myscratch.app.ui.theme

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.myscratch.app.MyScratchApp

class ThemeState(initialDark: Boolean = true) {
    var isDark by mutableStateOf(initialDark)
        private set

    fun toggle() {
        isDark = !isDark
        val prefs = MyScratchApp.instance.getSharedPreferences("myscratch_theme", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("is_dark_mode", isDark).apply()
    }

    companion object {
        fun create(): ThemeState {
            val prefs = MyScratchApp.instance.getSharedPreferences("myscratch_theme", Context.MODE_PRIVATE)
            val isDark = prefs.getBoolean("is_dark_mode", true)
            return ThemeState(isDark)
        }
    }
}

val LocalThemeState = compositionLocalOf { ThemeState(true) }

// Semantic Theme Colors accessible in any composable
object AppColors {
    val background: Color
        @Composable get() = if (LocalThemeState.current.isDark) DarkObsidian else LightBackground

    val surface: Color
        @Composable get() = if (LocalThemeState.current.isDark) DarkSurface else LightSurface

    val surfaceVariant: Color
        @Composable get() = if (LocalThemeState.current.isDark) DarkSurfaceVariant else LightSurfaceVariant

    val border: Color
        @Composable get() = if (LocalThemeState.current.isDark) DarkBorder else LightBorder

    val borderSubtle: Color
        @Composable get() = if (LocalThemeState.current.isDark) DarkBorderSubtle else LightBorderSubtle

    val textPrimary: Color
        @Composable get() = if (LocalThemeState.current.isDark) TextPrimaryDark else TextPrimaryLight

    val textSecondary: Color
        @Composable get() = if (LocalThemeState.current.isDark) TextSecondaryDark else TextSecondaryLight

    val textMuted: Color
        @Composable get() = if (LocalThemeState.current.isDark) TextMutedDark else TextMutedLight

    val emerald: Color
        @Composable get() = if (LocalThemeState.current.isDark) EmeraldAccent else EmeraldLight

    val emeraldBg: Color
        @Composable get() = if (LocalThemeState.current.isDark) EmeraldBgDark else EmeraldBgLight

    val coral: Color
        @Composable get() = if (LocalThemeState.current.isDark) CoralExpense else CoralLight

    val coralBg: Color
        @Composable get() = if (LocalThemeState.current.isDark) CoralBgDark else CoralBgLight

    val azure: Color
        @Composable get() = if (LocalThemeState.current.isDark) AzureAccent else AzureLight

    val azureBg: Color
        @Composable get() = if (LocalThemeState.current.isDark) AzureBgDark else AzureBgLight

    val violet: Color
        @Composable get() = Color(0xFF8B5CF6)

    val violetBg: Color
        @Composable get() = if (LocalThemeState.current.isDark) Color(0x268B5CF6) else Color(0x1F8B5CF6)
}
