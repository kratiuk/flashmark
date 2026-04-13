package com.kratiuk.flashmark.data

data class Recording(
    val fileName: String,
    val filePath: String,
    val createdAt: Long,
    val durationMs: Long,
    val isCompleted: Boolean = false,
    val transcript: String? = null,
    val transcriptStatus: String = "pending",
    val transcriptLog: String? = null,
)
