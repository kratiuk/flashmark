package com.kratiuk.flashmark

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kratiuk.flashmark.service.RecordingService

/**
 * Handles the boot-completed system event
 * Runs when the phone finishes starting and Android sends BOOT_COMPLETED
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            // Show main notification
            RecordingService.showIdleNotification(context)
        }
    }
}
