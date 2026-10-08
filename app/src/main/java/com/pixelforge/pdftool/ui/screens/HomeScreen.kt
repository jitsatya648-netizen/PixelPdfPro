package com.pixelforge.pdftool.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PhotoSizeSelectLarge
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.pixelforge.pdftool.model.ImageItem
import com.pixelforge.pdftool.model.PageSizeOption
import com.pixelforge.pdftool.ui.components.AnimatedAppear
import com.pixelforge.pdftool.ui.components.AnimatedPremiumBackground
import com.pixelforge.pdftool.ui.components.ConvertingOverlay
import com.pixelforge.pdftool.ui.components.ErrorCard
import com.pixelforge.pdftool.ui.components.GlassCard
import com.pixelforge.pdftool.ui.components.GlossyButton
import com.pixelforge.pdftool.ui.components.SuccessOverlay
import com.pixelforge.pdftool.ui.theme.AccentCyan
import com.pixelforge.pdftool.ui.theme.AccentGold
import com.pixelforge.pdftool.ui.theme.AccentPink
import com.pixelforge.pdftool.ui.theme.BgDeep
import com.pixelforge.pdftool.ui.theme.CyanGradient
import com.pixelforge.pdftool.ui.theme.GlassStroke
import com.pixelforge.pdftool.ui.theme.PrimaryGradient
import com.pixelforge.pdftool.ui.theme.TextPrimary
import com.pixelforge.pdftool.ui.theme.TextSecondary
import com.pixelforge.pdftool.ui.vm.MainViewModel
import com.pixelforge.pdftool.util.FileUtils

@Composable
fun HomeScreen(viewModel: MainViewModel) {
    val state by viewModel.state.collectAsState()
    val haptics = LocalHapticFeedback.current

    val pickImages = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        if (uris.isNotEmpty()) viewModel.addImages(uris)
    }

    val createDocument = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        if (uri != null) viewModel.startConversion(uri)
    }

    val hasImages = state.images.isNotEmpty()

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedPremiumBackground()

        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(Modifier.statusBarsPadding())
            HeaderBar(imageCount = state.images.size)

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 6.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item(key = "add") {
                    AnimatedAppear {
                        AddImagesCard(count = state.images.size) { pickImages.launch("image/*") }
                    }
                }

                if (hasImages) {
                    item(key = "pageSize") {
                        AnimatedAppear(delayMs = 60) {
                            PageSizeSelector(selected = state.pageSize, onSelect = viewModel::setPageSize)
                        }
                    }
                    item(key = "fileName") {
                        AnimatedAppear(delayMs = 120) {
                            FileNameField(value = state.fileName, onChange = viewModel::setFileName)
                        }
                    }
                    item(key = "sectionLabel") {
                        SectionHeader(count = state.images.size, onClear = viewModel::clearAll)
                    }
                    itemsIndexed(state.images, key = { _, item -> item.id }) { index, image ->
                        ImageCard(
                            item = image,
                            index = index,
                            total = state.images.size,
                            onMoveUp = { viewModel.moveImage(image.id, true) },
                            onMoveDown = { viewModel.moveImage(image.id, false) },
                            onRemove = { viewModel.removeImage(image.id) }
                        )
                    }
                    item(key = "bottomSpacer") { Spacer(Modifier.height(140.dp)) }
                } else {
                    item(key = "empty") { EmptyState() }
                }
            }
        }

        // Bottom convert button
        AnimatedVisibility(
            visible = hasImages && !state.isConverting && state.success == null,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(tween(400)) { it } + fadeIn(tween(400)),
            exit = slideOutVertically(tween(280)) { it } + fadeOut(tween(280))
        ) {
            ConvertBar(count = state.images.size) {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                val name = FileUtils.sanitizeFileName(state.fileName)
                createDocument.launch("$name.pdf")
            }
        }

        // Converting overlay
        AnimatedVisibility(
            visible = state.isConverting,
            enter = fadeIn(tween(250)),
            exit = fadeOut(tween(250))
        ) {
            ConvertingOverlay(
                progress = state.progress,
                status = state.statusText,
                onCancel = viewModel::cancelConversion
            )
        }

        // Success overlay
        AnimatedVisibility(
            visible = state.success != null,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(200))
        ) {
            state.success?.let { info ->
                SuccessOverlay(info = info, onDone = viewModel::dismissSuccess)
            }
        }

        // Error card
        AnimatedVisibility(
            visible = state.error != null,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 110.dp, start = 20.dp, end = 20.dp),
            enter = slideInVertically { it } + fadeIn(),
            exit = fadeOut()
        ) {
            ErrorCard(message = state.error.orEmpty(), onDismiss = viewModel::dismissError)
        }
    }
}

/* ---------- Header ---------- */

@Composable
private fun HeaderBar(imageCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Brush.linearGradient(PrimaryGradient)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.PictureAsPdf, contentDescription = null, modifier = Modifier.size(22.dp), tint = Color.White)
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text("PixelPDF Pro", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text("Image → PDF Converter", fontSize = 11.sp, color = TextSecondary)
        }
        Spacer(Modifier.weight(1f))
        AnimatedVisibility(
            visible = imageCount > 0,
            enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
            Box(
                Modifier
                    .clip(CircleShape)
                    .background(Brush.linearGradient(CyanGradient))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("$imageCount", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

/* ---------- Add Images Card ---------- */

@Composable
private fun AddImagesCard(count: Int, onClick: () -> Unit) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val pulse by transition.animateFloat(
        1f, 1.08f,
        infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )

    GlassCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(56.dp)
                    .graphicsLayer { scaleX = pulse; scaleY = pulse }
                    .clip(CircleShape)
                    .background(Brush.linearGradient(PrimaryGradient)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(28.dp), tint = Color.White)
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    if (count == 0) "Add Images" else "Add More Images",
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text("JPG • PNG • WEBP • BMP • HEIC • Any size", color = TextSecondary, fontSize = 12.sp)
            }
        }
    }
}

/* ---------- Page Size Selector ---------- */

@Composable
private fun PageSizeSelector(selected: PageSizeOption, onSelect: (PageSizeOption) -> Unit) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.PhotoSizeSelectLarge, contentDescription = null, modifier = Modifier.size(20.dp), tint = AccentCyan)
            Spacer(Modifier.width(10.dp))
            Text("Page Size", color = TextPrimary, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PageSizeOption.entries.forEach { option ->
                PageSizeChip(
                    option = option,
                    isSelected = selected == option,
                    modifier = Modifier.weight(1f),
                    onClick = { onSelect(option) }
                )
            }
        }
    }
}

@Composable
private fun PageSizeChip(
    option: PageSizeOption,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.96f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium),
        label = "chipScale"
    )

    Box(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (isSelected) Brush.linearGradient(PrimaryGradient)
                else Brush.verticalGradient(listOf(Color(0x14FFFFFF), Color(0x08FFFFFF)))
            )
            .border(if (isSelected) 0.dp else 1.dp, if (isSelected) Color.Transparent else GlassStroke, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 11.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            option.label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else TextSecondary
        )
    }
}

/* ---------- File Name Field ---------- */

@Composable
private fun FileNameField(value: String, onChange: (String) -> Unit) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.DriveFileRenameOutline, contentDescription = null, modifier = Modifier.size(20.dp), tint = AccentGold)
            Spacer(Modifier.width(10.dp))
            Text("PDF File Name", color = TextPrimary, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(4.dp))
        TextField(
            value = value,
            onValueChange = onChange,
            singleLine = true,
            label = { Text("MyDocument") },
            textStyle = LocalTextStyle.current.copy(color = TextPrimary, fontSize = 15.sp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = AccentCyan,
                unfocusedIndicatorColor = Color(0x2EFFFFFF),
                cursorColor = AccentCyan,
                focusedLabelColor = AccentCyan,
                unfocusedLabelColor = TextSecondary
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/* ---------- Section Header ---------- */

@Composable
private fun SectionHeader(count: Int, onClear: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
        Text("Selected Images ($count)", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Spacer(Modifier.weight(1f))
        Text(
            "Clear All",
            color = AccentPink,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .clip(CircleShape)
                .clickable(onClick = onClear)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

/* ---------- Image Card ---------- */

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ImageCard(
    item: ImageItem,
    index: Int,
    total: Int,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: () -> Unit
) {
    AnimatedAppear(
        modifier = Modifier.animateItemPlacement(),
        delayMs = if (index < 8) index * 45 else 0
    ) {
        GlassCard(modifier = Modifier.fillMaxWidth(), padding = 12.dp) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(60.dp).clip(RoundedCornerShape(14.dp)).background(Color(0x14FFFFFF))) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(item.uri)
                            .crossfade(250)
                            .build(),
                        contentDescription = item.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp))
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        item.name,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .clip(CircleShape)
                                .background(Color(0x1A7C4DFF))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text("${index + 1}", fontSize = 10.sp, color = AccentCyan, fontWeight = FontWeight.Bold)
                        }
                        if (item.sizeBytes > 0) {
                            Spacer(Modifier.width(8.dp))
                            Text(FileUtils.formatBytes(item.sizeBytes), fontSize = 11.sp, color = TextSecondary)
                        }
                    }
                }
                Spacer(Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CardIconButton(Icons.Filled.KeyboardArrowUp, enabled = index > 0, onClick = onMoveUp)
                    Spacer(Modifier.height(4.dp))
                    CardIconButton(Icons.Filled.KeyboardArrowDown, enabled = index < total - 1, onClick = onMoveDown)
                }
                Spacer(Modifier.width(6.dp))
                CardIconButton(Icons.Filled.DeleteOutline, tint = AccentPink, onClick = onRemove)
            }
        }
    }
}

@Composable
private fun CardIconButton(
    icon: ImageVector,
    enabled: Boolean = true,
    tint: Color = Color.White,
    onClick: () -> Unit
) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = if (enabled) tint.copy(alpha = 0.85f) else tint.copy(alpha = 0.18f),
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(Color(0x12FFFFFF))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(3.dp)
    )
}

/* ---------- Empty State ---------- */

@Composable
private fun EmptyState() {
    val transition = rememberInfiniteTransition(label = "float")
    val offset by transition.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(2400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "floatOffset"
    )

    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(Modifier.graphicsLayer { translationY = -offset * 14f }) {
            Box(
                Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color(0x0DFFFFFF))
                    .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(32.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.PhotoLibrary, contentDescription = null, modifier = Modifier.size(52.dp), tint = Color.White.copy(alpha = 0.5f))
            }
        }
        Spacer(Modifier.height(24.dp))
        Text("No images added yet", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Text(
            "Tap the card above to select photos\nand create a beautiful PDF — fully offline.",
            color = TextSecondary,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}

/* ---------- Bottom Convert Bar ---------- */

@Composable
private fun ConvertBar(count: Int, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color.Transparent, BgDeep.copy(alpha = 0.95f), BgDeep)))
            .navigationBarsPadding()
    ) {
        GlossyButton(
            text = "Convert to PDF",
            subtitle = "$count image${if (count > 1) "s" else ""} selected • Ready",
            icon = Icons.Filled.PictureAsPdf,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 32.dp, bottom = 12.dp),
            onClick = onClick
        )
    }
}