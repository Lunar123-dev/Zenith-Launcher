package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.data.model.AccentColorOption
import com.example.data.model.ThemeStyle

fun buildLauncherColorScheme(themeStyle: ThemeStyle, accentOption: AccentColorOption): ColorScheme {
    val accent = Color(accentOption.hex)

    return when (themeStyle) {
        ThemeStyle.AMOLED_BLACK -> darkColorScheme(
            primary = accent,
            onPrimary = Color.Black,
            background = Color(0xFF000000),
            onBackground = Color(0xFFF1F5F9),
            surface = Color(0xFF08090C),
            onSurface = Color(0xFFF1F5F9),
            surfaceVariant = Color(0xFF14171E),
            onSurfaceVariant = Color(0xFF94A3B8),
            outline = Color(0xFF2E3440)
        )

        ThemeStyle.CHARCOAL_DARK -> darkColorScheme(
            primary = accent,
            onPrimary = Color.Black,
            background = Color(0xFF121418),
            onBackground = Color(0xFFF8FAFC),
            surface = Color(0xFF1A1D24),
            onSurface = Color(0xFFF8FAFC),
            surfaceVariant = Color(0xFF262A34),
            onSurfaceVariant = Color(0xFFA0AEC0),
            outline = Color(0xFF3B4252)
        )

        ThemeStyle.MIDNIGHT_NAVY -> darkColorScheme(
            primary = accent,
            onPrimary = Color.Black,
            background = Color(0xFF0A0F1D),
            onBackground = Color(0xFFE2E8F0),
            surface = Color(0xFF11182C),
            onSurface = Color(0xFFE2E8F0),
            surfaceVariant = Color(0xFF1E293B),
            onSurfaceVariant = Color(0xFF94A3B8),
            outline = Color(0xFF334155)
        )

        ThemeStyle.CLEAN_LIGHT -> lightColorScheme(
            primary = accent,
            onPrimary = Color.White,
            background = Color(0xFFF8FAFC),
            onBackground = Color(0xFF0F172A),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF0F172A),
            surfaceVariant = Color(0xFFE2E8F0),
            onSurfaceVariant = Color(0xFF475569),
            outline = Color(0xFFCBD5E1)
        )
    }
}

@Composable
fun ZenithLauncherTheme(
    themeStyle: ThemeStyle = ThemeStyle.AMOLED_BLACK,
    accentOption: AccentColorOption = AccentColorOption.CYAN_NEON,
    content: @Composable () -> Unit
) {
    val colorScheme = buildLauncherColorScheme(themeStyle, accentOption)
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
