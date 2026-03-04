package com.kratiuk.flashmark.transcription

import android.util.Log

object WhisperBridge {
    private const val TAG = "WhisperBridge"

    val isLoaded: Boolean = try {
        System.loadLibrary("omp")
        System.loadLibrary("ggml-base")
        System.loadLibrary("ggml-cpu")
        System.loadLibrary("ggml")
        System.loadLibrary("whisper")
        System.loadLibrary("whisper_jni")
        true
    } catch (e: UnsatisfiedLinkError) {
        Log.e(TAG, "Native libs not loaded", e)
        false
    }

    external fun transcribe(
        modelPath: String,
        audioPath: String,
        language: String,
    ): String
}
