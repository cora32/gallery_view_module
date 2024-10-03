package io.iskopasi.galleryview

import android.app.Application
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File


class GalleryModelFactory(private val context: Application, private val saveDirectory: File) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return GalleryModel(context, saveDirectory) as T
    }
}

class GalleryModel(context: Application, private val saveDirectory: File) :
    AndroidViewModel(context) {
    companion object {
        private var observer: GalleryObserver? = null

        fun initObserver(saveDirectory: File): GalleryObserver {
            if (observer == null) {
                observer = GalleryObserver(saveDirectory)
            }

            return observer!!
        }
    }

    var mediaFiles by mutableStateOf(listOf<GalleryData>())
    var onClick: ((file: File) -> Unit)? = null
    private var onRefresh: (() -> Unit)? = null
    private var onDelete: (() -> Unit)? = null
    private val retriever = MediaMetadataRetriever()

    // Respond only for create and remove events
    private fun onNewVideoFileEvent() {
        refresh().invokeOnCompletion {
            viewModelScope.launch(Dispatchers.Main) { onRefresh?.invoke() }
        }
    }

    fun start() {
        // Initial refresh
        refresh().invokeOnCompletion {
            initObserver(saveDirectory)
                .apply {
                    addListener(this@GalleryModel.hashCode().toString(), ::onNewVideoFileEvent)
                    startWatching()
                }
        }
    }

    private fun refresh() = viewModelScope.launch(Dispatchers.IO) {
        mediaFiles = getFiles() ?: listOf()
    }

    private fun getFiles(): List<GalleryData>? = saveDirectory.listFiles { file ->
        file.isVisualMedia
    }?.sortedBy { it.lastModified() }?.reversed()?.toList()?.map {
        GalleryData(it, it.getDuration())
    }

    fun remove(item: File) {
        if (item.exists()) {
            item.delete()
            refresh().invokeOnCompletion {
                onDelete?.invoke()
            }
        }
    }

    fun onRefresh(function: () -> Unit) {
        onRefresh = function
    }

    fun onDelete(function: () -> Unit) {
        onDelete = function
    }

    fun onClick(function: (file: File) -> Unit) {
        onClick = function
    }

    private fun File.getDuration(): Long {
        try {
            retriever.setDataSource(getApplication(), Uri.fromFile(this))
            return retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong()
                ?: -1
        } catch (ex: Exception) {
            return -1
        }
    }

    fun clear() = viewModelScope.launch(Dispatchers.IO) {
        if (saveDirectory.exists()) {
            for (child in saveDirectory.listFiles()!!)
                child.delete()
        }

        refresh()
    }

    fun onDestroy() {
        observer?.removeListener(this.hashCode().toString())
    }
}


data class GalleryData(val file: File, val length: Long)