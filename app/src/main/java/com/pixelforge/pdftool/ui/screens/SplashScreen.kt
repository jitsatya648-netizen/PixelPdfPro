package com.pixelforge.pdftool.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pixelforge.pdftool.ui.components.AnimatedPremiumBackground
import com.pixelforge.pdftool.ui.theme.AccentCyan
import com.pixelforge.pdftool.ui.theme.AccentPink
import com.pixelforge.pdftool.ui.theme.AccentViolet
import com.pixelforge.pdftool.ui.theme.CyanGradient
import com.pixelforge.pdftool.ui.theme.PrimaryGradient
import com.pixelforge.pdftool.ui.theme.TextSecondary

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    var appeared by remember { mutableStateOf(false) }

    val logoScale by animateFloatAsState(
        targetValue = if (appeared) 1f else 0.3f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "logoScale"
    )
    val logoAlpha by animateFloatAsState(
        targetValue = if (appeared) 1f else 0f,
        animationSpec = tween(700),
        label = "logoAlpha"
    )
    val textAlpha by animateFloatAsState(
        targetValue = if (appeared) 1f else 0f,
        animationSpec = tween(900, delayMillis = 350),
        label = "textAlpha"
    )

    val shimmer = rememberInfiniteTransition(label = "shimmer")
    val shimmerOffset by shimmer.animateFloat(
        0f, 2000f,
        infiniteRepeatable(tween(2600, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmerOffset"
    )

    val loading = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        appeared = true
        loading.animateTo(1f, tween(2100, easing = FastOutSlowInEasing))
        onFinished()
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        AnimatedPremiumBackground()

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .graphicsLayer {
                        scaleX = logoScale
                        scaleY = logoScale
                        alpha = logoAlpha
                    }
                    .shadow(28.dp, CircleShape, ambientColor = AccentViolet, spotColor = AccentPink)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(PrimaryGradient)),
                contentAlignment = Alignment.Center
            ) {
                Box(Modifier.matchParentSize().background(Brush.verticalGradient(listOf(Color(0x55FFFFFF), Color.Transparent))))
                Icon(Icons.Filled.PictureAsPdf, contentDescription = null, modifier = Modifier.size(56.dp), tint = Color.White)
            }

            Spacer(Modifier.height(28.dp))

            Text(
                "PixelPDF Pro",
                modifier = Modifier.graphicsLayer { alpha = textAlpha },
                style = TextStyle(
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                    brush = Brush.linearGradient(
                        colors = listOf(AccentViolet, AccentCyan, AccentPink, AccentViolet),
                        start = Offset(shimmerOffset - 1000f, 0f),
                        end = Offset(shimmerOffset, 200f)
                    )
                )
            )

            Spacer(Modifier.height(6.dp))

            Text(
                "Image → PDF • 100% Offline • Premium",
                color = TextSecondary,
                fontSize = 13.sp,
                modifier = Modifier.graphicsLayer { alpha = textAlpha }
            )

            Spacer(Modifier.height(48.dp))

            Box(Modifier.width(180.dp).height(4.dp).clip(CircleShape).background(Color(0x1AFFFFFF))) {
                Box(
                    Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction = loading.value)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(CyanGradient))
                )
            }
        }
    }
}