package com.kratiuk.flashmark.transcription

import android.content.Context
import java.io.File

class TranscriptionManager(private val context: Context) {

    private val modelAssetName = "ggml-base-q5_1.bin"

    fun transcribe(audioPath: String, language: String): String {
        val modelPath = ensureModel()
        val audioFile = File(audioPath)
        if (!audioFile.exists()) {
            throw IllegalStateException("Audio file not found: $audioPath")
        }
        if (audioFile.length() <= 44) {
            throw IllegalStateException("Audio file too small: ${audioFile.length()} bytes")
        }
        if (!WhisperBridge.isLoaded) {
            throw IllegalStateException("Native whisper libraries not loaded")
        }
        return WhisperBridge.transcribe(modelPath, audioPath, language)
    }

    private fun ensureModel(): String {
        val modelFile = File(context.filesDir, modelAssetName)
        if (!modelFile.exists()) {
            context.assets.open(modelAssetName).use { input ->
                modelFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }
        return modelFile.absolutePath
    }
}
