package io.iskopasi.galleryview

import android.os.Build
import android.os.FileObserver
import java.io.File

class GalleryObserver(saveDirectory: File) {
    private val listeners = mutableMapOf<String, (() -> Unit)>()
    private val observer =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) object : FileObserver(
            File(saveDirectory.absolutePath + "/"),
            CLOSE_WRITE or DELETE
        ) {
            override fun onEvent(event: Int, path: String?) {
                if (path?.isVisualMedia == true) {
                    listeners.values.forEach {
                        it()
                    }
                }
            }
        }
        else object : FileObserver(
            saveDirectory.absolutePath + "/",
            CLOSE_WRITE or DELETE
        ) {
            override fun onEvent(event: Int, path: String?) {
                if (path?.isVisualMedia == true) {
                    listeners.values.forEach {
                        it()
                    }
                }
            }
        }

    fun addListener(key: String, onEvent: () -> Unit) {
        if (!listeners.containsKey(key)) {
            listeners[key] = onEvent
        }
    }

    fun startWatching() {
        observer.startWatching()
    }

    fun removeListener(key: String) {
        listeners.remove(key)
    }
}