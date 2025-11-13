package com.pranshulgg.recordmaster.utils

import android.media.MediaPlayer
import androidx.compose.runtime.MutableState
import java.io.File

fun computeDirKey(dir: File): Long {
    val children = dir.listFiles() ?: return 0L
    var total = 0L
    for (child in children) {
        total += child.name.hashCode().toLong()
        total += child.lastModified()
        if (child.isDirectory) {
            total += computeDirKey(child)
        }
    }
    return total
}


 fun moveFileToDir(file: File, targetDir: File) {
    try {
        val target = File(targetDir, file.name)
        if (!file.renameTo(target)) {
            file.copyTo(target, overwrite = true)
            file.delete()
        }
    } catch (_: Exception) { }
}

 fun stopIfPlayingAndCleanup(file: File, mediaPlayerState: MutableState<MediaPlayer?>, playingPath: String?) {
    val mp = mediaPlayerState.value
    if (playingPath == file.absolutePath) {
        try { mp?.stop() } catch (_: Exception) {}
        try { mp?.release() } catch (_: Exception) {}
        mediaPlayerState.value = null
    }
}
