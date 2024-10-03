package io.iskopasi.galleryview

import android.annotation.SuppressLint
import android.text.format.DateUtils
import java.io.File

@SuppressLint("DefaultLocale")
fun Long.toElapsed(): String {
    val elapsedMillisec = (System.currentTimeMillis() - this)

    return when {
        elapsedMillisec > DateUtils.DAY_IN_MILLIS -> {
            val days = elapsedMillisec / DateUtils.DAY_IN_MILLIS
            return days.toString() + days.toDaysPlural()
        }

        elapsedMillisec > DateUtils.HOUR_IN_MILLIS -> {
            val hours = elapsedMillisec / DateUtils.HOUR_IN_MILLIS
            val mins =
                (elapsedMillisec - (hours * DateUtils.HOUR_IN_MILLIS)) / DateUtils.MINUTE_IN_MILLIS
            return "${String.format("%02d", hours)}h ${String.format("%02d", mins)}m"
        }

        else -> DateUtils.formatElapsedTime(elapsedMillisec / DateUtils.SECOND_IN_MILLIS)

    }
}

private fun Long.toDaysPlural(): String {
    return when {
        this == 1L -> " day"
        else -> " days"
    }
}


val File.isVisualMedia
    get() =
        extension.isVisualMedia

val String.isVisualMedia
    get() =
        endsWith("webm") ||
                endsWith("avi") ||
                endsWith("mp4") ||
                endsWith("jpg") ||
                endsWith("jpeg") ||
                endsWith("png")