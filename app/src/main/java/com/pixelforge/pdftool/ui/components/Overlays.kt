package com.pixelforge.pdftool.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Path
import androidx.compose.ui.geometry.PathMeasure
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pixelforge.pdftool.model.SuccessInfo
import com.pixelforge.pdftool.ui.theme.AccentCyan
import com.pixelforge.pdftool.ui.theme.AccentGold
import com.pixelforge.pdftool.ui.theme.AccentPink
import com.pixelforge.pdftool.ui.theme.AccentViolet
import com.pixelforge.pdftool.ui.theme.CyanGradient
import com.pixelforge.pdftool.ui.theme.PrimaryGradient
import com.pixelforge.pdftool.ui.theme.TextPrimary
import com.pixelforge.pdftool.ui.theme.TextSecondary
import com.pixelforge.pdftool.util.FileUtils
import kotlinx.coroutines.delay
import kotlin.math.min

/* ---------- Converting / Loading Overlay ---------- */

@Composable
fun ConvertingOverlay(progress: Float, status: String, onCancel: () -> Unit) {
    val animatedProgress by animateFloatAsState(targetValue = progress, animationSpec = tween(350), label = "progressAnim")
    val percent = (animatedProgress * 100).toInt().coerceIn(0, 100)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.78f))
            .clickable(enabled = true, indication = null, interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }) { },
        contentAlignment = Alignment.Center
    ) {
        GlassCard(modifier = Modifier.width(300.dp), cornerRadius = 28.dp) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(contentAlignment = Alignment.Center) {
                    GradientRing(modifier = Modifier.size(92.dp), progress = animatedProgress)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$percent%", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("Converting", color = TextSecondary, fontSize = 10.sp)
                    }
                }
                Spacer(Modifier.height(18.dp))
                Text(
                    status.ifEmpty { "Preparing…" },
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(14.dp))
                ShimmerProgressBar(progress = animatedProgress)
                Spacer(Modifier.height(6.dp))
                TextButton(onClick = onCancel) {
                    Text("Cancel", color = AccentPink, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun GradientRing(modifier: Modifier = Modifier, progress: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "ringGlow")
    val rotation by infiniteTransition.animateFloat(
        0f, 360f,
        infiniteRepeatable(tween(1600, easing = LinearEasing), RepeatMode.Restart),
        label = "ringRotation"
    )

    Canvas(modifier = modifier) {
        val stroke = 9.dp.toPx()
        val inset = stroke / 2f
        val arcSize = size.min() - stroke
        val topLeft = Offset(inset, inset)

        drawArc(
            color = Color.White.copy(alpha = 0.08f),
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = androidx.compose.ui.geometry.Size(arcSize, arcSize),
            style = Stroke(stroke)
        )

        val sweep = 360f * min(progress, 1f).coerceAtLeast(0.02f)
        drawArc(
            brush = Brush.sweepGradient(listOf(AccentCyan, AccentViolet, AccentPink, AccentCyan)),
            startAngle = -90f + rotation * 0f,
            sweepAngle = sweep,
            useCenter = false,
            topLeft = topLeft,
            size = androidx.compose.ui.geometry.Size(arcSize, arcSize),
            style = Stroke(stroke, cap = StrokeCap.Round)
        )
    }
}

@Composable
private fun ShimmerProgressBar(progress: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmerBar")
    val shift by infiniteTransition.animateFloat(
        -1f, 2f,
        infiniteRepeatable(tween(1200, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmerShift"
    )

    Column(Modifier.fillMaxWidth()) {
        Box(Modifier.fillMaxWidth().height(8.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.08f))) {
            Box(
                Modifier
                    .fillMaxWidth(fraction = min(progress, 1f))
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            0f to AccentViolet,
                            (shift * 0.5f).coerceIn(0f, 1f) to AccentCyan,
                            1f to AccentPink
                        )
                    )
            )
        }
    }
}

/* ---------- Success Overlay (Checkmark Draw Animation + Sparkle) ---------- */

@Composable
fun SuccessOverlay(info: SuccessInfo, onDone: () -> Unit) {
    val alpha = remember { Animatable(0f) }
    val pop = remember { Animatable(0.5f) }
    val check = remember { Animatable(0f) }
    var showSparkle by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        alpha.animateTo(1f, tween(250))
        pop.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
        check.animateTo(1f, tween(600, easing = FastOutSlowInEasing))
        showSparkle = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f * alpha.value))
            .clickable(enabled = true, indication = null, interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }) { },
        contentAlignment = Alignment.Center
    ) {
        GlassCard(
            modifier = Modifier
                .width(320.dp)
                .graphicsLayer {
                    scaleX = pop.value
                    scaleY = pop.value
                    this.alpha = alpha.value
                },
            cornerRadius = 28.dp,
            padding = 26.dp
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(contentAlignment = Alignment.Center) {
                    AnimatedCheckCircle(progress = check.value, showSparkle = showSparkle)
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(0.dp)
                    )
                }
                Spacer(Modifier.height(20.dp))
                Text("PDF Created!", color = TextPrimary, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text(
                    info.fileName,
                    color = AccentCyan,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "${info.pages} page${if (info.pages > 1) "s" else ""} • ${FileUtils.formatBytes(info.sizeBytes)} • Saved offline",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Spacer(Modifier.height(22.dp))
                GlossyButton(
                    text = "Done",
                    onClick = onDone,
                    gradient = CyanGradient,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun AnimatedCheckCircle(progress: Float, showSparkle: Boolean) {
    val sparkle = remember { Animatable(0f) }
    LaunchedEffect(showSparkle) {
        if (showSparkle) sparkle.animateTo(1f, tween(700, easing = FastOutSlowInEasing))
    }

    Canvas(modifier = Modifier.size(130.dp)) {
        val c = Offset(size.width / 2f, size.height / 2f)
        val r = size.minDimension() * 0.30f

        // Outer expanding ring
        if (progress > 0.1f) {
            drawCircle(color = AccentCyan.copy(alpha = (1f - progress) * 0.6f), radius = r * (1f + progress * 0.55f), center = c)
        }

        // Gradient circle
        drawCircle(
            brush = Brush.linearGradient(listOf(AccentViolet, AccentPink)),
            radius = r * progress.coerceAtLeast(0.001f),
            center = c
        )

        // Glossy top highlight
        drawCircle(
            brush = Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.35f), Color.Transparent), endY = size.height * 0.55f),
            radius = r * progress.coerceAtLeast(0.001f),
            center = c
        )

        // Check mark (path trim animation)
        if (progress > 0.5f) {
            val checkProgress = ((progress - 0.5f) / 0.5f).coerceIn(0f, 1f)
            val path = Path().apply {
                moveTo(c.x - r * 0.45f, c.y + r * 0.02f)
                lineTo(c.x - r * 0.12f, c.y + r * 0.36f)
                lineTo(c.x + r * 0.5f, c.y - r * 0.34f)
            }
            val measure = PathMeasure()
            measure.setPath(path, false)
            val segment = Path()
            measure.getSegment(0f, measure.length * checkProgress, segment, true)
            drawPath(
                path = segment,
                color = Color.White,
                style = Stroke(width = r * 0.18f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }

        // Sparkle burst
        if (showSparkle) {
            val s = sparkle.value
            if (s < 1f) {
                val sparkColors = listOf(AccentCyan, AccentPink, AccentGold, AccentViolet)
                for (i in 0 until 12) {
                    val angle = Math.toRadians(i * 30.0)
                    val dist = r * (1.15f + 0.75f * s)
                    val sx = c.x + dist * kotlin.math.cos(angle).toFloat()
                    val sy = c.y + dist * kotlin.math.sin(angle).toFloat()
                    val dotRadius = (r * 0.08f) * (1f - s)
                    if (dotRadius > 0.5f) {
                        drawCircle(color = sparkColors[i % sparkColors.size].copy(alpha = 1f - s), radius = dotRadius, center = Offset(sx, sy))
                    }
                }
            }
        }
    }
}

/* ---------- Error Card ---------- */

@Composable
fun ErrorCard(message: String, onDismiss: () -> Unit) {
    LaunchedEffect(message) {
        delay(4000)
        onDismiss()
    }
    GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 18.dp, padding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.ErrorOutline, contentDescription = null, modifier = Modifier.size(22.dp), tint = AccentPink)
            Spacer(Modifier.width(10.dp))
            Text(message, color = TextPrimary, fontSize = 13.sp, modifier = Modifier.weight(1f))
        }
    }
}