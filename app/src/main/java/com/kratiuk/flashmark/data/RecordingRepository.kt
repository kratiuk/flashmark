package com.kratiuk.flashmark.data

import android.content.Context
import android.media.MediaMetadataRetriever
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecordingRepository(private val context: Context) {

    private val recordingsDir: File
        get() = File(context.filesDir, "recordings").also { it.mkdirs() }

    fun generateFilePath(): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        return File(recordingsDir, "rec_$timestamp.wav").absolutePath
    }

    fun getRecordings(): List<Recording> {
        val dir = recordingsDir
        if (!dir.exists()) return emptyList()
        return dir.listFiles { f -> f.extension == "wav" }
            ?.map { file ->
                val transcriptFile = File(file.parentFile, file.nameWithoutExtension + ".txt")
                val statusFile = File(file.parentFile, file.nameWithoutExtension + ".stt")
                val logFile = File(file.parentFile, file.nameWithoutExtension + ".sttlog")
                val doneFile = File(file.parentFile, file.nameWithoutExtension + ".done")
                val status = statusFile.takeIf { it.exists() }?.readText()?.trim().orEmpty()
                Recording(
                    fileName = file.name,
                    filePath = file.absolutePath,
                    createdAt = file.lastModified(),
                    durationMs = extractDuration(file),
                    isCompleted = doneFile.exists(),
                    transcript = transcriptFile.takeIf { it.exists() }?.readText(),
                    transcriptStatus = if (status.isNotEmpty()) status else "pending",
                    transcriptLog = logFile.takeIf { it.exists() }?.readText(),
                )
            }
            ?.sortedByDescending { it.createdAt }
            ?: emptyList()
    }

    fun deleteRecording(recording: Recording) {
        File(recording.filePath).delete()
        val base = File(recording.filePath).nameWithoutExtension
        File(File(recording.filePath).parentFile, "$base.txt").delete()
        File(File(recording.filePath).parentFile, "$base.stt").delete()
        File(File(recording.filePath).parentFile, "$base.sttlog").delete()
        File(File(recording.filePath).parentFile, "$base.done").delete()
    }

    fun toggleCompleted(recording: Recording) {
        val base = File(recording.filePath).nameWithoutExtension
        val doneFile = File(File(recording.filePath).parentFile, "$base.done")
        if (doneFile.exists()) {
            doneFile.delete()
        } else {
            doneFile.writeText("done")
        }
    }

    private fun extractDuration(file: File): Long {
        return try {
            MediaMetadataRetriever().use { retriever ->
                retriever.setDataSource(file.absolutePath)
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    ?.toLongOrNull() ?: 0L
            }
        } catch (_: Exception) {
            0L
        }
    }
}
