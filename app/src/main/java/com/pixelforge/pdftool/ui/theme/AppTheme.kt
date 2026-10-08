package com.pixelforge.pdftool.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val BgDeep = Color(0xFF07070F)
val BgSoft = Color(0xFF101020)
val AccentViolet = Color(0xFF7C4DFF)
val AccentPink = Color(0xFFFF4D9D)
val AccentCyan = Color(0xFF00E5FF)
val AccentGold = Color(0xFFFFC24D)
val TextPrimary = Color(0xFFF5F6FA)
val TextSecondary = Color(0xA6FFFFFF)
val GlassStroke = Color(0x26FFFFFF)

val PrimaryGradient = listOf(AccentViolet, AccentPink)
val CyanGradient = listOf(AccentCyan, AccentViolet)

private val DarkColors = darkColorScheme(
    primary = AccentViolet,
    secondary = AccentCyan,
    tertiary = AccentPink,
    background = BgDeep,
    surface = BgSoft,
    onPrimary = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkColors, content = content)
}