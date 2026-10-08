package com.pixelforge.pdftool.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import com.pixelforge.pdftool.model.PageSizeOption
import kotlinx.coroutines.CancellationException
import java.io.IOException
import java.io.OutputStream
import kotlin.math.max
import kotlin.math.min

data class PdfResult(val pages: Int, val bytes: Long)

class PdfEngine(private val context: Context) {

    companion object {
        // A4 @ 300dpi quality cap — quality ভালো থাকবে, আবার মেমোরিও বাঁচবে
        private const val MAX_DIMENSION = 2480
        private const val A4_W = 595f
        private const val A4_H = 842f
        private const val LETTER_W = 612f
        private const val LETTER_H = 792f
        private const val MARGIN_PT = 36f

        fun decodeBitmap(context: Context, uri: Uri): Bitmap? {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, bounds)
            }
            val width = bounds.outWidth
            val height = bounds.outHeight
            if (width <= 0 || height <= 0) return null

            var sample = 1
            while (max(width / sample, height / sample) > MAX_DIMENSION) sample *= 2

            val opts = BitmapFactory.Options().apply {
                inSampleSize = sample
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val bitmap = context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, opts)
            } ?: return null

            val rotation = readExifRotation(context, uri)
            return if (rotation != 0) {
                val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
                val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                if (rotated !== bitmap) bitmap.recycle()
                rotated
            } else bitmap
        }

        private fun readExifRotation(context: Context, uri: Uri): Int = try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val exif = ExifInterface(stream)
                when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    else -> 0
                }
            } ?: 0
        } catch (_: Exception) {
            0
        }
    }

    fun createPdf(
        images: List<Uri>,
        pageSize: PageSizeOption,
        output: OutputStream,
        onProgress: (done: Int, total: Int) -> Unit,
        isCancelled: () -> Boolean
    ): PdfResult {
        val total = images.size
        val document = PdfDocument()
        val counting = CountingOutputStream(output)
        var pagesWritten = 0

        try {
            images.forEachIndexed { index, uri ->
                if (isCancelled()) throw CancellationException("PDF conversion cancelled")

                // একটা সময়ে একটাই বিটম্যাপ মেমোরিতে থাকে → কখনো OOM হবে না
                val bitmap = decodeBitmap(context, uri)
                if (bitmap == null) {
                    onProgress(index + 1, total)
                    return@forEachIndexed
                }

                val pageWidth: Float
                val pageHeight: Float
                when (pageSize) {
                    PageSizeOption.A4 -> { pageWidth = A4_W; pageHeight = A4_H }
                    PageSizeOption.LETTER -> { pageWidth = LETTER_W; pageHeight = LETTER_H }
                    PageSizeOption.FIT_IMAGE -> {
                        val scale = 1000f / max(bitmap.width, bitmap.height).toFloat()
                        pageWidth = bitmap.width * scale
                        pageHeight = bitmap.height * scale
                    }
                }

                val info = PdfDocument.PageInfo.Builder(pageWidth.toInt(), pageHeight.toInt(), index + 1).create()
                val page = document.startPage(info)
                val canvas = page.canvas
                canvas.drawColor(Color.WHITE)

                val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
                if (pageSize == PageSizeOption.FIT_IMAGE) {
                    canvas.drawBitmap(bitmap, Rect(0, 0, bitmap.width, bitmap.height), RectF(0f, 0f, pageWidth, pageHeight), paint)
                } else {
                    val availW = pageWidth - MARGIN_PT * 2
                    val availH = pageHeight - MARGIN_PT * 2
                    val scale = min(availW / bitmap.width, availH / bitmap.height)
                    val drawW = bitmap.width * scale
                    val drawH = bitmap.height * scale
                    val left = MARGIN_PT + (availW - drawW) / 2f
                    val top = MARGIN_PT + (availH - drawH) / 2f
                    canvas.drawBitmap(bitmap, null, RectF(left, top, left + drawW, top + drawH), paint)
                }

                document.finishPage(page)
                bitmap.recycle()
                pagesWritten++
                onProgress(index + 1, total)
            }

            if (pagesWritten == 0) throw IOException("No images could be processed")

            document.writeTo(counting)
            counting.flush()
            return PdfResult(pagesWritten, counting.bytesWritten)
        } finally {
            document.close()
            runCatching { output.close() }
        }
    }

    private class CountingOutputStream(private val delegate: OutputStream) : OutputStream() {
        var bytesWritten: Long = 0L
            private set

        override fun write(b: Int) {
            delegate.write(b)
            bytesWritten++
        }

        override fun write(b: ByteArray, off: Int, len: Int) {
            delegate.write(b, off, len)
            bytesWritten += len
        }

        override fun flush() = delegate.flush()
        override fun close() = delegate.close()
    }
}