package com.pixelforge.pdftool.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns

object FileUtils {

    fun queryNameAndSize(context: Context, uri: Uri): Pair<String, Long> {
        return try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                var name = "Image"
                var size = 0L
                if (cursor.moveToFirst()) {
                    if (nameIndex >= 0) name = cursor.getString(nameIndex) ?: "Image"
                    if (sizeIndex >= 0) size = cursor.getLong(sizeIndex)
                }
                Pair(name, size)
            } ?: Pair("Image", 0L)
        } catch (_: Exception) {
            Pair("Image", 0L)
        }
    }

    fun formatBytes(bytes: Long): String = when {
        bytes <= 0 -> ""
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> String.format("%.1f KB", bytes / 1024f)
        else -> String.format("%.2f MB", bytes / (1024f * 1024f))
    }

    fun sanitizeFileName(name: String): String {
        val cleaned = name.trim().replace(Regex("[\\\\/:*?\"<>|]"), "_").take(60).trim()
        return cleaned.ifBlank { "MyDocument" }
    }
}