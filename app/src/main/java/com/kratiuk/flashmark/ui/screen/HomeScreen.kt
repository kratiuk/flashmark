package com.kratiuk.flashmark.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kratiuk.flashmark.data.Recording
import com.kratiuk.flashmark.R
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    refreshTick: Int = 0,
    viewModel: HomeViewModel = viewModel(),
) {
    val recordings by viewModel.recordings.collectAsState()
    val playingFilePath by viewModel.playingFilePath.collectAsState()
    var infoRecording by remember { mutableStateOf<Recording?>(null) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    val darkTheme = isSystemInDarkTheme()
    val emptyIconTint = if (darkTheme) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        lerp(MaterialTheme.colorScheme.secondaryContainer, Color.Black, 0.15f)
    }
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(refreshTick) {
        viewModel.loadRecordings()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadRecordings()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        modifier = Modifier.offset(x = (-18).dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            tint = androidx.compose.ui.graphics.Color(0xFFFDD835),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge,
                        )
                    }
                },
                actions = {},
            )
        },
    ) { innerPadding ->
        val zoneId = remember { ZoneId.systemDefault() }
        val filtered = remember(recordings, selectedDate) {
            recordings.filter { recording ->
                val date = Instant.ofEpochMilli(recording.createdAt)
                    .atZone(zoneId)
                    .toLocalDate()
                date == selectedDate
            }
        }
        val today = LocalDate.now()
        val dateFormatter = remember {
            DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH)
        }
        if (filtered.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                DateHeader(
                    selectedDate = selectedDate,
                    today = today,
                    onSelectDate = { selectedDate = it },
                    onOpenCustom = { showDatePicker = true },
                    dateFormatter = dateFormatter,
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = emptyIconTint,
                        )
                        if (selectedDate == today) {
                            Text(
                                text = stringResource(R.string.home_no_captures_today_title),
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center,
                            )
                            Text(
                                text = stringResource(R.string.home_no_captures_today_subtitle),
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.home_no_captures_date_title),
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center,
                            )
                            Text(
                                text = stringResource(R.string.home_blank_line),
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    DateHeader(
                        selectedDate = selectedDate,
                        today = today,
                        onSelectDate = { selectedDate = it },
                        onOpenCustom = { showDatePicker = true },
                        dateFormatter = dateFormatter,
                    )
                }
                items(filtered, key = { it.fileName }) { recording ->
                    RecordingItem(
                        recording = recording,
                        isPlaying = playingFilePath == recording.filePath,
                        onPlay = { viewModel.play(recording) },
                        onStop = { viewModel.stopPlayback() },
                        onInfo = { infoRecording = recording },
                        onToggleCompleted = { viewModel.toggleCompleted(recording) },
                        onDelete = { viewModel.deleteRecording(recording) },
                    )
                }
            }
        }
    }

    if (infoRecording != null) {
        val rec = infoRecording!!
        val logText = rec.transcriptLog?.ifBlank { stringResource(R.string.home_log_empty) }
            ?: stringResource(R.string.home_log_empty)
        AlertDialog(
            onDismissRequest = { infoRecording = null },
            title = { Text(stringResource(R.string.home_transcription_log_title)) },
            text = {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = logText,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                            .verticalScroll(rememberScrollState()),
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { infoRecording = null }) {
                    Text(stringResource(R.string.home_ok))
                }
            },
        )
    }

    if (showDatePicker) {
        val initialMillis = selectedDate
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialMillis,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val date = Instant.ofEpochMilli(utcTimeMillis)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                    return !date.isAfter(LocalDate.now())
                }
            },
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = datePickerState.selectedDateMillis
                    if (millis != null) {
                        selectedDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                    }
                    showDatePicker = false
                }) { Text(stringResource(R.string.home_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.home_cancel)) }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun RecordingItem(
    recording: Recording,
    isPlaying: Boolean,
    onPlay: () -> Unit,
    onStop: () -> Unit,
    onInfo: () -> Unit,
    onToggleCompleted: () -> Unit,
    onDelete: () -> Unit,
) {
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val timeString = timeFormat.format(Date(recording.createdAt))
    val durationString = formatDuration(recording.durationMs)
    val textDecoration = if (recording.isCompleted) TextDecoration.LineThrough else null
    val completedAlpha = if (recording.isCompleted) 0.6f else 1f

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = timeString,
                        style = MaterialTheme.typography.bodyLarge,
                        textDecoration = textDecoration,
                        modifier = Modifier.alpha(completedAlpha),
                    )
                    Text(
                        text = durationString,
                        style = MaterialTheme.typography.bodySmall,
                        textDecoration = textDecoration,
                        modifier = Modifier.alpha(completedAlpha),
                    )
                }
                IconButton(onClick = if (isPlaying) onStop else onPlay) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) {
                            stringResource(R.string.home_stop_cd)
                        } else {
                            stringResource(R.string.home_play_cd)
                        },
                    )
                }
                IconButton(onClick = onToggleCompleted) {
                    Icon(Icons.Default.CheckCircle, contentDescription = stringResource(R.string.home_mark_done_cd))
                }
                IconButton(onClick = onInfo) {
                    Icon(Icons.Default.Terminal, contentDescription = stringResource(R.string.home_transcription_log_cd))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.home_delete_cd))
                }
            }
            if (!recording.transcript.isNullOrBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceDim,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                ) {
                    Text(
                        text = recording.transcript,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                            .alpha(completedAlpha),
                        style = MaterialTheme.typography.bodyMedium,
                        textDecoration = textDecoration,
                    )
                }
            }
        }
    }
}

private fun formatDuration(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

@Composable
private fun DateHeader(
    selectedDate: LocalDate,
    today: LocalDate,
    onSelectDate: (LocalDate) -> Unit,
    onOpenCustom: () -> Unit,
    dateFormatter: DateTimeFormatter,
) {
    val activeColor = MaterialTheme.colorScheme.secondaryContainer
    val inactiveColor = MaterialTheme.colorScheme.surfaceVariant
    val activeText = MaterialTheme.colorScheme.onSecondaryContainer
    val inactiveText = Color(0xFFB0B0B0)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val isToday = selectedDate == today
            ChipButton(
                text = stringResource(R.string.home_today),
                active = !isToday,
                activeColor = activeColor,
                inactiveColor = inactiveColor,
                inactiveText = inactiveText,
                activeText = activeText,
                onClick = { if (!isToday) onSelectDate(today) },
            )
            val prev = selectedDate.minusDays(1)
            IconChipButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.home_prev_day_cd),
                active = true,
                activeColor = activeColor,
                inactiveColor = inactiveColor,
                inactiveText = inactiveText,
                activeText = activeText,
                onClick = { onSelectDate(prev) },
            )
            val next = selectedDate.plusDays(1)
            val canGoNext = !next.isAfter(today)
            IconChipButton(
                icon = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = stringResource(R.string.home_next_day_cd),
                active = canGoNext,
                activeColor = activeColor,
                inactiveColor = inactiveColor,
                inactiveText = inactiveText,
                activeText = activeText,
                onClick = { if (canGoNext) onSelectDate(next) },
            )
            ChipButton(
                text = stringResource(R.string.home_custom_date),
                active = true,
                activeColor = activeColor,
                inactiveColor = inactiveColor,
                inactiveText = inactiveText,
                activeText = activeText,
                onClick = onOpenCustom,
            )
        }
        Text(
            text = selectedDate.format(dateFormatter),
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
private fun ChipButton(
    text: String,
    active: Boolean,
    activeColor: Color,
    inactiveColor: Color,
    inactiveText: Color,
    activeText: Color,
    onClick: () -> Unit,
) {
    val container = if (active) activeColor else inactiveColor
    val content = if (active) activeText else inactiveText
    Surface(
        color = container,
        shape = MaterialTheme.shapes.large,
        modifier = Modifier
            .height(36.dp)
            .clickable { onClick() },
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = text, color = content, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun IconChipButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    active: Boolean,
    activeColor: Color,
    inactiveColor: Color,
    inactiveText: Color,
    activeText: Color,
    onClick: () -> Unit,
) {
    val container = if (active) activeColor else inactiveColor
    val content = if (active) activeText else inactiveText
    Surface(
        color = container,
        shape = MaterialTheme.shapes.large,
        modifier = Modifier
            .height(36.dp)
            .clickable(enabled = active) { onClick() },
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = content,
            )
        }
    }
}
