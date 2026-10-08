package com.pixelforge.pdftool.ui.vm

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pixelforge.pdftool.model.ImageItem
import com.pixelforge.pdftool.model.PageSizeOption
import com.pixelforge.pdftool.model.SuccessInfo
import com.pixelforge.pdftool.pdf.PdfEngine
import com.pixelforge.pdftool.util.FileUtils
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Collections
import java.util.UUID

data class UiState(
    val images: List<ImageItem> = emptyList(),
    val pageSize: PageSizeOption = PageSizeOption.A4,
    val fileName: String = "MyDocument",
    val isConverting: Boolean = false,
    val progress: Float = 0f,
    val statusText: String = "",
    val success: SuccessInfo? = null,
    val error: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    private val engine = PdfEngine(application)
    private var convertJob: Job? = null

    fun addImages(uris: List<Uri>) {
        if (uris.isEmpty()) return
        val newItems = uris.map { uri ->
            ImageItem(id = UUID.randomUUID().toString(), uriString = uri.toString())
        }
        _state.update { it.copy(images = it.images + newItems) }

        // Background-এ নাম ও সাইজ লোড হয় — UI কখনো ব্লক হবে না
        viewModelScope.launch(Dispatchers.IO) {
            newItems.forEach { item ->
                val (name, size) = FileUtils.queryNameAndSize(getApplication(), Uri.parse(item.uriString))
                _state.update { current ->
                    current.copy(
                        images = current.images.map {
                            if (it.id == item.id) it.copy(name = name, sizeBytes = size) else it
                        }
                    )
                }
            }
        }
    }

    fun removeImage(id: String) = _state.update { s -> s.copy(images = s.images.filterNot { it.id == id }) }

    fun clearAll() = _state.update { it.copy(images = emptyList()) }

    fun moveImage(id: String, up: Boolean) {
        _state.update { s ->
            val list = s.images.toMutableList()
            val from = list.indexOfFirst { it.id == id }
            val to = if (up) from - 1 else from + 1
            if (from >= 0 && to in list.indices) Collections.swap(list, from, to)
            s.copy(images = list)
        }
    }

    fun setPageSize(option: PageSizeOption) = _state.update { it.copy(pageSize = option) }

    fun setFileName(name: String) = _state.update { it.copy(fileName = name.take(60)) }

    fun dismissError() = _state.update { it.copy(error = null) }

    fun dismissSuccess() = _state.update { it.copy(success = null) }

    fun startConversion(saveUri: Uri) {
        val current = _state.value
        if (current.images.isEmpty() || current.isConverting) return

        _state.update { it.copy(isConverting = true, progress = 0f, statusText = "Starting…") }

        convertJob = viewModelScope.launch {
            try {
                val uris = current.images.map { it.uri }
                val pageSize = current.pageSize

                // ভারী কাজ সম্পূর্ণ ব্যাকগ্রাউন্ড থ্রেডে
                val result = withContext(Dispatchers.IO) {
                    val output = getApplication<Application>().contentResolver.openOutputStream(saveUri)
                        ?: throw IOException("Storage access failed")
                    engine.createPdf(
                        images = uris,
                        pageSize = pageSize,
                        output = output,
                        onProgress = { done, total ->
                            _state.update {
                                it.copy(
                                    progress = done.toFloat() / total.coerceAtLeast(1),
                                    statusText = "Processing image $done of $total"
                                )
                            }
                        },
                        isCancelled = { !isActive }
                    )
                }

                val finalName = FileUtils.sanitizeFileName(current.fileName) + ".pdf"
                _state.update {
                    it.copy(
                        isConverting = false,
                        progress = 1f,
                        success = SuccessInfo(finalName, result.bytes, result.pages)
                    )
                }
            } catch (e: CancellationException) {
                runCatching { getApplication<Application>().contentResolver.delete(saveUri, null, null) }
                _state.update { it.copy(isConverting = false, progress = 0f, statusText = "") }
            } catch (e: Exception) {
                runCatching { getApplication<Application>().contentResolver.delete(saveUri, null, null) }
                _state.update {
                    it.copy(isConverting = false, error = e.message ?: "Something went wrong. Try again.")
                }
            }
        }
    }

    fun cancelConversion() {
        convertJob?.cancel()
    }
}