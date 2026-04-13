package com.kratiuk.flashmark.transcription

import android.content.Context
import com.kratiuk.flashmark.R

class TranscriptionPrefs(context: Context) {
    private val prefs = context.getSharedPreferences("transcription_prefs", Context.MODE_PRIVATE)

    fun getLanguage(): String = prefs.getString("language", "en") ?: "en"

    fun setLanguage(lang: String) {
        prefs.edit().putString("language", lang).apply()
    }

    companion object {
        val SUPPORTED_LANGUAGES = listOf(
            "en" to R.string.lang_en,
            "uk" to R.string.lang_uk,
            "de" to R.string.lang_de,
        )
    }
}
