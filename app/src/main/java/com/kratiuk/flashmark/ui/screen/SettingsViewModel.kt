package com.kratiuk.flashmark.ui.screen

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import com.kratiuk.flashmark.data.NotificationPrefs
import com.kratiuk.flashmark.data.NotificationSettings
import com.kratiuk.flashmark.service.RecordingService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = NotificationPrefs(application)

    private val _settings = MutableStateFlow(prefs.get())
    val settings: StateFlow<NotificationSettings> = _settings

    fun updateSettings(settings: NotificationSettings) {
        _settings.value = settings
    }

    fun save() {
        prefs.save(_settings.value)
        restartService()
    }

    private fun restartService() {
        val app = getApplication<Application>()
        app.stopService(Intent(app, RecordingService::class.java))
        RecordingService.start(app)
    }
}
