package com.kratiuk.flashmark.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.automirrored.filled.ShortText
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kratiuk.flashmark.data.NotificationPrefs
import com.kratiuk.flashmark.transcription.TranscriptionPrefs
import androidx.compose.ui.platform.LocalContext
import com.kratiuk.flashmark.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel(),
) {
    val settings by viewModel.settings.collectAsState()
    var activeDialog by remember { mutableStateOf<DialogType?>(null) }
    var tempText by remember { mutableStateOf("") }
    var tempIconKey by remember { mutableStateOf(settings.iconKey) }
    val context = LocalContext.current
    val transcriptionPrefs = remember { TranscriptionPrefs(context) }
    var transcriptionLang by remember { mutableStateOf(transcriptionPrefs.getLanguage()) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row {
                        Icon(
                            Icons.Filled.Settings,
                            contentDescription = null,
                            tint = Color(0xFFFDD835),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.settings_title))
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.settings_section_notification),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
            )

            SettingItem(
                title = stringResource(R.string.settings_title_label),
                value = settings.title,
                icon = {
                    Icon(
                        Icons.Filled.Title,
                        contentDescription = null,
                        modifier = Modifier.size(30.dp),
                    )
                },
                onClick = {
                    tempText = settings.title
                    activeDialog = DialogType.Title
                },
            )
            SettingItem(
                title = stringResource(R.string.settings_idle_text_label),
                value = settings.idleText,
                icon = {
                    Icon(
                        Icons.AutoMirrored.Filled.ShortText,
                        contentDescription = null,
                        modifier = Modifier.size(30.dp),
                    )
                },
                onClick = {
                    tempText = settings.idleText
                    activeDialog = DialogType.IdleText
                },
            )
            SettingItem(
                title = stringResource(R.string.settings_capturing_text_label),
                value = settings.recordingText,
                icon = {
                    Icon(
                        Icons.Filled.Mic,
                        contentDescription = null,
                        modifier = Modifier.size(30.dp),
                    )
                },
                onClick = {
                    tempText = settings.recordingText
                    activeDialog = DialogType.RecordingText
                },
            )
            SettingItem(
                title = stringResource(R.string.settings_capture_button_label),
                value = settings.recordLabel,
                icon = {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(30.dp),
                    )
                },
                onClick = {
                    tempText = settings.recordLabel
                    activeDialog = DialogType.RecordLabel
                },
            )
            SettingItem(
                title = stringResource(R.string.settings_stop_button_label),
                value = settings.stopLabel,
                icon = {
                    Icon(
                        Icons.Filled.Stop,
                        contentDescription = null,
                        modifier = Modifier.size(30.dp),
                    )
                },
                onClick = {
                    tempText = settings.stopLabel
                    activeDialog = DialogType.StopLabel
                },
            )
            SettingItem(
                title = stringResource(R.string.settings_notification_icon_label),
                value = stringResource(NotificationPrefs.getIconLabelRes(settings.iconKey)),
                icon = {
                    Icon(
                        Icons.Filled.Image,
                        contentDescription = null,
                        modifier = Modifier.size(30.dp),
                    )
                },
                onClick = {
                    tempIconKey = settings.iconKey
                    activeDialog = DialogType.Icon
                },
            )
            ToggleSettingItem(
                title = stringResource(R.string.settings_show_incomplete_count_label),
                value = stringResource(
                    if (settings.showIncompleteCount) {
                        R.string.settings_toggle_enabled
                    } else {
                        R.string.settings_toggle_disabled
                    },
                ),
                checked = settings.showIncompleteCount,
                onCheckedChange = {
                    viewModel.updateSettings(settings.copy(showIncompleteCount = it))
                    viewModel.save()
                },
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.settings_section_whisper),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
            )
            SettingItem(
                title = stringResource(R.string.settings_transcription_language_label),
                value = TranscriptionPrefs.SUPPORTED_LANGUAGES.firstOrNull { it.first == transcriptionLang }
                    ?.let { stringResource(it.second) }
                    ?: transcriptionLang,
                icon = {
                    Icon(
                        Icons.Filled.Mic,
                        contentDescription = null,
                        modifier = Modifier.size(30.dp),
                    )
                },
                onClick = { activeDialog = DialogType.Language },
            )
        }
    }

    when (activeDialog) {
        DialogType.Title -> TextSettingDialog(
            title = stringResource(R.string.dialog_title_title),
            value = tempText,
            onValueChange = { tempText = it },
            onConfirm = {
                viewModel.updateSettings(settings.copy(title = tempText))
                viewModel.save()
                activeDialog = null
            },
            onDismiss = { activeDialog = null },
        )
        DialogType.IdleText -> TextSettingDialog(
            title = stringResource(R.string.dialog_title_idle),
            value = tempText,
            onValueChange = { tempText = it },
            onConfirm = {
                viewModel.updateSettings(settings.copy(idleText = tempText))
                viewModel.save()
                activeDialog = null
            },
            onDismiss = { activeDialog = null },
        )
        DialogType.RecordingText -> TextSettingDialog(
            title = stringResource(R.string.dialog_title_capturing),
            value = tempText,
            onValueChange = { tempText = it },
            onConfirm = {
                viewModel.updateSettings(settings.copy(recordingText = tempText))
                viewModel.save()
                activeDialog = null
            },
            onDismiss = { activeDialog = null },
        )
        DialogType.RecordLabel -> TextSettingDialog(
            title = stringResource(R.string.dialog_title_capture_button),
            value = tempText,
            onValueChange = { tempText = it },
            onConfirm = {
                viewModel.updateSettings(settings.copy(recordLabel = tempText))
                viewModel.save()
                activeDialog = null
            },
            onDismiss = { activeDialog = null },
        )
        DialogType.StopLabel -> TextSettingDialog(
            title = stringResource(R.string.dialog_title_stop_button),
            value = tempText,
            onValueChange = { tempText = it },
            onConfirm = {
                viewModel.updateSettings(settings.copy(stopLabel = tempText))
                viewModel.save()
                activeDialog = null
            },
            onDismiss = { activeDialog = null },
        )
        DialogType.Icon -> IconSettingDialog(
            selectedKey = tempIconKey,
            onSelect = { tempIconKey = it },
            onConfirm = {
                viewModel.updateSettings(settings.copy(iconKey = tempIconKey))
                viewModel.save()
                activeDialog = null
            },
            onDismiss = { activeDialog = null },
        )
        DialogType.Language -> LanguageDialog(
            selected = transcriptionLang,
            onSelect = { transcriptionLang = it },
            onConfirm = {
                transcriptionPrefs.setLanguage(transcriptionLang)
                activeDialog = null
            },
            onDismiss = { activeDialog = null },
        )
        null -> Unit
    }
}

@Composable
private fun SettingItem(
    title: String,
    value: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
) {
    ListItem(
        headlineContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
            )
        },
        supportingContent = {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        leadingContent = icon,
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    )
}

@Composable
private fun ToggleSettingItem(
    title: String,
    value: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    ListItem(
        headlineContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
            )
        },
        supportingContent = {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) },
    )
}

@Composable
private fun TextSettingDialog(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(stringResource(R.string.dialog_ok)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.dialog_cancel)) }
        },
    )
}

@Composable
private fun IconSettingDialog(
    selectedKey: String,
    onSelect: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_title_notification_icon)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                NotificationPrefs.ICON_OPTIONS.forEach { (key, resId) ->
                    val selected = key == selectedKey
                    ListItem(
                        headlineContent = { Text(stringResource(NotificationPrefs.getIconLabelRes(key))) },
                        leadingContent = {
                            Icon(
                                painter = painterResource(resId),
                                contentDescription = key,
                                modifier = Modifier.size(24.dp),
                            )
                        },
                        trailingContent = {
                            if (selected) {
                                Icon(
                                    Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                        },
                        modifier = Modifier.clickable { onSelect(key) },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(stringResource(R.string.dialog_ok)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.dialog_cancel)) }
        },
    )
}

@Composable
private fun LanguageDialog(
    selected: String,
    onSelect: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_title_transcription_language)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TranscriptionPrefs.SUPPORTED_LANGUAGES.forEach { (code, labelRes) ->
                    ListItem(
                        headlineContent = { Text(stringResource(labelRes)) },
                        supportingContent = { Text(code) },
                        trailingContent = {
                            if (code == selected) {
                                Icon(
                                    Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier.clickable { onSelect(code) },
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = onConfirm) { Text(stringResource(R.string.dialog_ok)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.dialog_cancel)) } },
    )
}

private enum class DialogType {
    Title,
    IdleText,
    RecordingText,
    RecordLabel,
    StopLabel,
    Icon,
    Language,
}
