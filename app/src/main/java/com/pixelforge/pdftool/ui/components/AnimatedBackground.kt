package com.pixelforge.pdftool.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.pixelforge.pdftool.ui.theme.AccentCyan
import com.pixelforge.pdftool.ui.theme.AccentPink
import com.pixelforge.pdftool.ui.theme.AccentViolet
import com.pixelforge.pdftool.ui.theme.BgDeep

@Composable
fun AnimatedPremiumBackground(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "bgTransition")
    val p1 by transition.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(11000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "p1"
    )
    val p2 by transition.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(13000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "p2"
    )
    val p3 by transition.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(9000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "p3"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(brush = Brush.verticalGradient(listOf(Color(0xFF0C0C1A), BgDeep)))
        val w = size.width
        val h = size.height

        val c1 = Offset(w * (0.15f + 0.25f * p1), h * (0.12f + 0.08f * p1))
        drawCircle(
            brush = Brush.radialGradient(listOf(AccentViolet.copy(alpha = 0.35f), Color.Transparent), center = c1, radius = w * 0.8f),
            radius = w * 0.8f,
            center = c1
        )

        val c2 = Offset(w * (0.95f - 0.2f * p2), h * (0.9f - 0.1f * p2))
        drawCircle(
            brush = Brush.radialGradient(listOf(AccentCyan.copy(alpha = 0.20f), Color.Transparent), center = c2, radius = w * 0.9f),
            radius = w * 0.9f,
            center = c2
        )

        val c3 = Offset(w * (0.75f - 0.3f * p3), h * (0.4f + 0.15f * p3))
        drawCircle(
            brush = Brush.radialGradient(listOf(AccentPink.copy(alpha = 0.14f), Color.Transparent), center = c3, radius = w * 0.7f),
            radius = w * 0.7f,
            center = c3
        )
    }
}