package io.iskopasi.galleryview

import android.app.Application
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.FileObserver
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
    var mediaFiles by mutableStateOf(listOf<GalleryData>())
    private var onRefresh: ( () -> Unit)? = null
    private var onDelete: ( () -> Unit)? = null
    var onClick: ( (file: File) -> Unit)? = null
    private val retriever = MediaMetadataRetriever()
    private val observer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) object : FileObserver(
        File(saveDirectory.absolutePath + "/"),
        CLOSE_WRITE
    ) {
        override fun onEvent(event: Int, path: String?) {
            checkEvent(event, path)
        }
    }
    else object : FileObserver(
        saveDirectory.absolutePath + "/",
        CLOSE_WRITE
    ) {
        override fun onEvent(event: Int, path: String?) {
            checkEvent(event, path)
        }
    }

    fun start() {
        // Initial refresh
        refresh()

        observer.startWatching()
    }

    // Respond only for create and remove events
    private fun checkEvent(event: Int, path: String?) {
        refresh()

        viewModelScope.launch(Dispatchers.Main) { onRefresh?.invoke() }
    }

    fun refresh() = viewModelScope.launch(Dispatchers.IO) {
        mediaFiles = getFiles() ?: listOf()
    }

    private fun getFiles(): List<GalleryData>? = saveDirectory.listFiles { file ->
        file.isVisualMedia
    }?.sortedBy { it.lastModified() }?.reversed()?.toList()?.map {
        GalleryData(it, it.getDuration()) }

    fun remove(item: File) {
        if(item.exists()) {
            item.delete()
            refresh()

            onDelete?.invoke()
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

    private val File.isVisualMedia
        get() =
            extension == "webm" ||
                    extension == "avi" ||
                    extension == "mp4" ||
                    extension == "jpg" ||
                    extension == "jpeg" ||
                    extension == "png"

    private fun File.getDuration(): Long {
        retriever.setDataSource(getApplication(), Uri.fromFile(this))
        return retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: -1
    }

    fun clear() {
        saveDirectory.deleteRecursively()
    }
}


data class GalleryData(val file: File, val length: Long)