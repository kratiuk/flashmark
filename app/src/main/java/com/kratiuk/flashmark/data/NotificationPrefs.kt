package com.kratiuk.flashmark.data

import android.content.Context
import com.kratiuk.flashmark.R

data class NotificationSettings(
    val title: String,
    val idleText: String,
    val recordingText: String,
    val recordLabel: String,
    val stopLabel: String,
    val iconKey: String,
    val showIncompleteCount: Boolean,
)

class NotificationPrefs(private val context: Context) {

    private val prefs = context.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)

    fun get(): NotificationSettings = NotificationSettings(
        title = prefs.getString("title", context.getString(R.string.app_name))
            ?: context.getString(R.string.app_name),
        idleText = prefs.getString("idle_text", context.getString(R.string.notif_default_idle))
            ?: context.getString(R.string.notif_default_idle),
        recordingText = prefs.getString("recording_text", context.getString(R.string.notif_default_recording_text))
            ?: context.getString(R.string.notif_default_recording_text),
        recordLabel = prefs.getString("record_label", context.getString(R.string.notif_default_record_label))
            ?: context.getString(R.string.notif_default_record_label),
        stopLabel = prefs.getString("stop_label", context.getString(R.string.notif_default_stop_label))
            ?: context.getString(R.string.notif_default_stop_label),
        iconKey = prefs.getString("icon_key", "default") ?: "default",
        showIncompleteCount = prefs.getBoolean("show_incomplete_count", false),
    )

    fun save(settings: NotificationSettings) {
        prefs.edit()
            .putString("title", settings.title)
            .putString("idle_text", settings.idleText)
            .putString("recording_text", settings.recordingText)
            .putString("record_label", settings.recordLabel)
            .putString("stop_label", settings.stopLabel)
            .putString("icon_key", settings.iconKey)
            .putBoolean("show_incomplete_count", settings.showIncompleteCount)
            .apply()
    }

    companion object {
        val ICON_OPTIONS = listOf(
            "default" to R.drawable.bolt_24,
            "stacks" to R.drawable.stacks_24,
            "favorite" to R.drawable.favorite_24,
            "android" to R.drawable.android_24,
            "sunny" to R.drawable.sunny_24,
            "photo" to R.drawable.photo_24,
        )

        fun getIconRes(key: String): Int {
            return ICON_OPTIONS.firstOrNull { it.first == key }?.second
                ?: R.drawable.bolt_24
        }

        fun getIconLabelRes(key: String): Int {
            return when (key) {
                "default" -> R.string.icon_default
                "stacks" -> R.string.icon_stacks
                "favorite" -> R.string.icon_favorite
                "android" -> R.string.icon_android
                "sunny" -> R.string.icon_sunny
                "photo" -> R.string.icon_photo
                else -> R.string.icon_default
            }
        }
    }
}
