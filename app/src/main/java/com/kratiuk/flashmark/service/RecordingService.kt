package com.kratiuk.flashmark.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.kratiuk.flashmark.R
import com.kratiuk.flashmark.data.NotificationPrefs
import com.kratiuk.flashmark.data.RecordingRepository
import com.kratiuk.flashmark.transcription.TranscriptionManager
import com.kratiuk.flashmark.transcription.TranscriptionPrefs
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.io.File

class RecordingService : Service() {

    companion object {
        const val CHANNEL_ID = "recording_channel"
        const val NOTIFICATION_ID = 1
        const val ACTION_START_RECORDING = "com.kratiuk.flashmark.START_RECORDING"
        const val ACTION_STOP_RECORDING = "com.kratiuk.flashmark.STOP_RECORDING"
        const val ACTION_RECORDING_SAVED = "com.kratiuk.flashmark.RECORDING_SAVED"
        const val ACTION_TRANSCRIPT_READY = "com.kratiuk.flashmark.TRANSCRIPT_READY"
        const val ACTION_TRANSCRIPT_STATUS = "com.kratiuk.flashmark.TRANSCRIPT_STATUS"

        fun start(context: Context) {
            context.startForegroundService(Intent(context, RecordingService::class.java))
        }

        fun showIdleNotification(context: Context) {
            val nm = context.getSystemService(NotificationManager::class.java)
            nm.notify(NOTIFICATION_ID, buildNotification(context, recording = false))
        }

        private fun buildNotification(context: Context, recording: Boolean): Notification {
            val settings = NotificationPrefs(context).get()
            val incompleteCount = RecordingRepository(context).getIncompleteCount()

            val openAppIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            val openAppPi = PendingIntent.getActivity(
                context,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

            val actionIntent = Intent(context, RecordingService::class.java).apply {
                action = if (recording) ACTION_STOP_RECORDING else ACTION_START_RECORDING
            }
            val actionPi = if (recording) {
                PendingIntent.getService(
                    context,
                    1,
                    actionIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
            } else {
                PendingIntent.getForegroundService(
                    context,
                    1,
                    actionIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
            }

            val actionIcon = if (recording) {
                android.R.drawable.ic_media_pause
            } else {
                NotificationPrefs.getIconRes(settings.iconKey)
            }
            val actionTitle = if (recording) settings.stopLabel else settings.recordLabel
            val contentText = if (recording) {
                settings.recordingText
            } else if (settings.showIncompleteCount) {
                context.getString(
                    R.string.notif_idle_with_incomplete_count,
                    settings.idleText,
                    incompleteCount,
                )
            } else {
                settings.idleText
            }

            return NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(settings.title)
                .setContentText(contentText)
                .setSmallIcon(NotificationPrefs.getIconRes(settings.iconKey))
                .setContentIntent(openAppPi)
                .setOngoing(true)
                .setShowWhen(false)
                .setWhen(0L)
                .addAction(actionIcon, actionTitle, actionPi)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build()
        }
    }

    private var mediaRecorder: MediaRecorder? = null
    private var audioRecord: AudioRecord? = null
    private var recordingThread: Thread? = null
    private var isRecording = false
    private var currentFilePath: String? = null
    private lateinit var repository: RecordingRepository
    private lateinit var notificationPrefs: NotificationPrefs
    private lateinit var transcriptionPrefs: TranscriptionPrefs
    private lateinit var transcriptionManager: TranscriptionManager

    override fun onCreate() {
        super.onCreate()
        repository = RecordingRepository(this)
        notificationPrefs = NotificationPrefs(this)
        transcriptionPrefs = TranscriptionPrefs(this)
        transcriptionManager = TranscriptionManager(this)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_RECORDING -> startRecording()
            ACTION_STOP_RECORDING -> stopRecording()
            else -> startForegroundIdle(buildNotification(recording = false))
        }
        return START_STICKY
    }

    private fun startForegroundIdle(notification: Notification) {
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun startForegroundForRecording(notification: Notification) {
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun startRecording() {
        val filePath = repository.generateFilePath()
        currentFilePath = filePath

        startForegroundForRecording(buildNotification(recording = true))
        startPcmRecording(filePath)
        isRecording = true
        updateNotification(recording = true)
    }

    private fun stopRecording() {
        val lastFilePath = currentFilePath
        stopPcmRecording()
        isRecording = false
        currentFilePath = null
        updateNotification(recording = false)
        sendBroadcast(Intent(ACTION_RECORDING_SAVED).setPackage(packageName))
        if (lastFilePath != null) {
            Thread {
                val language = transcriptionPrefs.getLanguage()
                writeTranscriptStatus(lastFilePath, "processing")
                clearTranscriptLog(lastFilePath)
                sendBroadcast(Intent(ACTION_TRANSCRIPT_STATUS).setPackage(packageName))
                try {
                    val audioSize = File(lastFilePath).length()
                    writeTranscriptLog(lastFilePath, "Starting transcription. lang=$language, audioBytes=$audioSize")
                    val text = transcriptionManager.transcribe(lastFilePath, language)
                    if (text.isNotBlank()) {
                        writeTranscript(lastFilePath, text)
                        writeTranscriptStatus(lastFilePath, "done")
                        writeTranscriptLog(lastFilePath, "OK")
                        sendBroadcast(Intent(ACTION_TRANSCRIPT_READY).setPackage(packageName))
                    } else {
                        writeTranscriptStatus(lastFilePath, "failed")
                        writeTranscriptLog(lastFilePath, "Empty transcript result. audioBytes=$audioSize")
                        sendBroadcast(Intent(ACTION_TRANSCRIPT_STATUS).setPackage(packageName))
                    }
                } catch (e: Exception) {
                    writeTranscriptStatus(lastFilePath, "failed")
                    writeTranscriptLog(lastFilePath, e.stackTraceToString())
                    sendBroadcast(Intent(ACTION_TRANSCRIPT_STATUS).setPackage(packageName))
                }
            }.start()
        }
    }

    private fun writeTranscript(audioPath: String, text: String) {
        val file = File(audioPath)
        val transcript = File(file.parentFile, file.nameWithoutExtension + ".txt")
        transcript.writeText(text)
    }

    private fun writeTranscriptStatus(audioPath: String, status: String) {
        val file = File(audioPath)
        val statusFile = File(file.parentFile, file.nameWithoutExtension + ".stt")
        statusFile.writeText(status)
    }

    private fun writeTranscriptLog(audioPath: String, text: String) {
        val file = File(audioPath)
        val logFile = File(file.parentFile, file.nameWithoutExtension + ".sttlog")
        logFile.appendText(text + "\n")
    }

    private fun clearTranscriptLog(audioPath: String) {
        val file = File(audioPath)
        val logFile = File(file.parentFile, file.nameWithoutExtension + ".sttlog")
        logFile.writeText("")
    }

    private fun updateNotification(recording: Boolean) {
        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(NOTIFICATION_ID, buildNotification(recording))
    }

    @Suppress("DEPRECATION")
    private fun createMediaRecorder(): MediaRecorder {
        return if (Build.VERSION.SDK_INT >= 31) MediaRecorder(this) else MediaRecorder()
    }

    private fun startPcmRecording(filePath: String) {
        val sampleRate = 16_000
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        val minBuffer = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            channelConfig,
            audioFormat,
            minBuffer,
        )

        val output = FileOutputStream(filePath)
        // Write placeholder WAV header
        writeWavHeader(output, sampleRate, 1, 16, 0)

        audioRecord?.startRecording()
        recordingThread = Thread {
            val buffer = ByteArray(minBuffer)
            var totalBytes = 0L
            while (audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                if (read > 0) {
                    output.write(buffer, 0, read)
                    totalBytes += read
                }
            }
            output.flush()
            output.close()
            updateWavHeader(filePath, totalBytes)
        }.apply { start() }
    }

    private fun stopPcmRecording() {
        audioRecord?.apply {
            stop()
            release()
        }
        audioRecord = null
        recordingThread?.join()
        recordingThread = null
    }

    private fun writeWavHeader(
        out: FileOutputStream,
        sampleRate: Int,
        channels: Int,
        bitsPerSample: Int,
        dataSize: Long,
    ) {
        val byteRate = sampleRate * channels * bitsPerSample / 8
        val blockAlign = channels * bitsPerSample / 8
        val totalDataLen = dataSize + 36
        val header = ByteArray(44)
        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        writeIntLE(header, 4, totalDataLen.toInt())
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        writeIntLE(header, 16, 16)
        writeShortLE(header, 20, 1.toShort())
        writeShortLE(header, 22, channels.toShort())
        writeIntLE(header, 24, sampleRate)
        writeIntLE(header, 28, byteRate)
        writeShortLE(header, 32, blockAlign.toShort())
        writeShortLE(header, 34, bitsPerSample.toShort())
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        writeIntLE(header, 40, dataSize.toInt())
        out.write(header, 0, 44)
    }

    private fun updateWavHeader(filePath: String, dataSize: Long) {
        val raf = RandomAccessFile(filePath, "rw")
        val totalDataLen = dataSize + 36
        raf.seek(4)
        raf.write(intToLe(totalDataLen.toInt()))
        raf.seek(40)
        raf.write(intToLe(dataSize.toInt()))
        raf.close()
    }

    private fun writeIntLE(buf: ByteArray, offset: Int, value: Int) {
        buf[offset] = (value and 0xff).toByte()
        buf[offset + 1] = ((value shr 8) and 0xff).toByte()
        buf[offset + 2] = ((value shr 16) and 0xff).toByte()
        buf[offset + 3] = ((value shr 24) and 0xff).toByte()
    }

    private fun writeShortLE(buf: ByteArray, offset: Int, value: Short) {
        buf[offset] = (value.toInt() and 0xff).toByte()
        buf[offset + 1] = ((value.toInt() shr 8) and 0xff).toByte()
    }

    private fun intToLe(value: Int): ByteArray {
        return byteArrayOf(
            (value and 0xff).toByte(),
            ((value shr 8) and 0xff).toByte(),
            ((value shr 16) and 0xff).toByte(),
            ((value shr 24) and 0xff).toByte(),
        )
    }

    private fun buildNotification(recording: Boolean): Notification {
        return buildNotification(this, recording)
    }

    override fun onDestroy() {
        if (isRecording) {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false
        }
        super.onDestroy()
    }
}
