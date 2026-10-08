package com.pixelforge.pdftool.model

import android.net.Uri

data class ImageItem(
    val id: String,
    val uriString: String,
    val name: String = "Image",
    val sizeBytes: Long = 0L
) {
    val uri: Uri get() = Uri.parse(uriString)
}

enum class PageSizeOption(val label: String) {
    A4("A4"),
    LETTER("Letter"),
    FIT_IMAGE("Fit to Image")
}

data class SuccessInfo(
    val fileName: String,
    val sizeBytes: Long,
    val pages: Int
)