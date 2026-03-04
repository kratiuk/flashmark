package com.kratiuk.flashmark.ui.screen

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import androidx.lifecycle.AndroidViewModel
import androidx.core.content.ContextCompat
import com.kratiuk.flashmark.data.Recording
import com.kratiuk.flashmark.data.RecordingRepository
import com.kratiuk.flashmark.service.RecordingService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = RecordingRepository(application)

    private val _recordings = MutableStateFlow<List<Recording>>(emptyList())
    val recordings: StateFlow<List<Recording>> = _recordings

    private val _playingFilePath = MutableStateFlow<String?>(null)
    val playingFilePath: StateFlow<String?> = _playingFilePath

    private var mediaPlayer: MediaPlayer? = null

    private val recordingSavedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            loadRecordings()
        }
    }

    init {
        loadRecordings()
        val filter = IntentFilter(RecordingService.ACTION_RECORDING_SAVED).apply {
            addAction(RecordingService.ACTION_TRANSCRIPT_READY)
            addAction(RecordingService.ACTION_TRANSCRIPT_STATUS)
        }
        ContextCompat.registerReceiver(
            application,
            recordingSavedReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
    }

    fun loadRecordings() {
        _recordings.value = repository.getRecordings()
    }

    fun play(recording: Recording) {
        stopPlayback()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(recording.filePath)
            prepare()
            start()
            setOnCompletionListener {
                _playingFilePath.value = null
            }
        }
        _playingFilePath.value = recording.filePath
    }

    fun stopPlayback() {
        mediaPlayer?.apply {
            if (isPlaying) stop()
            release()
        }
        mediaPlayer = null
        _playingFilePath.value = null
    }

    fun deleteRecording(recording: Recording) {
        stopPlayback()
        repository.deleteRecording(recording)
        loadRecordings()
    }

    fun toggleCompleted(recording: Recording) {
        repository.toggleCompleted(recording)
        loadRecordings()
    }


    override fun onCleared() {
        super.onCleared()
        stopPlayback()
        getApplication<Application>().unregisterReceiver(recordingSavedReceiver)
    }
}
