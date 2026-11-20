package com.gonzalowebdev.goodhabits

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

object Palette {
    val habit: List<Color> = listOf(
        Color(0xFFF77070), Color(0xFFFA903B), Color(0xFFFBBD23), Color(0xFFFACB14), Color(0xFFA2E434), Color(0xFF49DC80), Color(0xFF33D299),
        Color(0xFF2BD2BE), Color(0xFF20D1ED), Color(0xFF37BCF7), Color(0xFF5FA4F8), Color(0xFF808AF8), Color(0xFFA689F9), Color(0xFFC082FC),
        Color(0xFFE777F8), Color(0xFFF271B5), Color(0xFFFA7084), Color(0xFF93A0B7), Color(0xFF99A1AD), Color(0xFFA3A3A3), Color(0xFFA8A19E)
    )
    fun color(i: Int): Color = habit[i.coerceIn(0, habit.lastIndex)]
}

@Immutable
data class GhColors(
    val dark: Boolean,
    val bg: Color,
    val surface: Color,
    val surface2: Color,
    val chip: Color,
    val line: Color,
    val text: Color,
    val text2: Color,
    val text3: Color,
    val primary: Color,
    val onPrimary: Color
)

val GhDark = GhColors(
    dark = true, bg = Color(0xFF100F12), surface = Color(0xFF1A191C), surface2 = Color(0xFF151417),
    chip = Color(0xFF2B2A31), line = Color(0x1AFFFFFF), text = Color(0xFFF2F2F5), text2 = Color(0xFFA09FA6),
    text3 = Color(0xFF6E6D74), primary = Color(0xFF8988F6), onPrimary = Color.White
)

val GhLight = GhColors(
    dark = false, bg = Color(0xFFF5F4F8), surface = Color(0xFFFFFFFF), surface2 = Color(0xFFEDECF1),
    chip = Color(0xFFE3E2E9), line = Color(0x1A000000), text = Color(0xFF16151A), text2 = Color(0xFF5E5D66),
    text3 = Color(0xFF8E8D96), primary = Color(0xFF6E6DE8), onPrimary = Color.White
)

val LocalGh = staticCompositionLocalOf { GhDark }

val gh: GhColors
    @Composable @ReadOnlyComposable get() = LocalGh.current

@Composable
fun GhTheme(mode: Int, content: @Composable () -> Unit) {
    val dark = when (mode) { 1 -> false; 2 -> true; else -> isSystemInDarkTheme() }
    val c = if (dark) GhDark else GhLight
    val scheme = if (dark) darkColorScheme(
        primary = c.primary, onPrimary = c.onPrimary, background = c.bg, onBackground = c.text,
        surface = c.bg, onSurface = c.text, surfaceVariant = c.surface, onSurfaceVariant = c.text2,
        surfaceContainerHigh = c.surface, surfaceContainerHighest = c.chip, secondaryContainer = c.chip
    ) else lightColorScheme(
        primary = c.primary, onPrimary = c.onPrimary, background = c.bg, onBackground = c.text,
        surface = c.bg, onSurface = c.text, surfaceVariant = c.surface, onSurfaceVariant = c.text2,
        surfaceContainerHigh = c.surface, surfaceContainerHighest = c.chip, secondaryContainer = c.chip
    )
    MaterialTheme(colorScheme = scheme) {
        CompositionLocalProvider(LocalGh provides c, content = content)
    }
}
