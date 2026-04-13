package com.kratiuk.flashmark

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.compose.ui.res.stringResource
import com.kratiuk.flashmark.service.RecordingService
import com.kratiuk.flashmark.ui.screen.AboutScreen
import com.kratiuk.flashmark.ui.screen.HomeScreen
import com.kratiuk.flashmark.ui.screen.SettingsScreen
import com.kratiuk.flashmark.ui.theme.FlashmarkTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import com.kratiuk.flashmark.R

class MainActivity : ComponentActivity() {

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (results.all { it.value }) {
            RecordingService.start(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestPermissionsAndStartService()
        setContent {
            FlashmarkTheme {
                var currentScreen by remember { mutableStateOf(Screen.Home) }
                var refreshTick by remember { mutableIntStateOf(0) }

                BackHandler(enabled = currentScreen == Screen.Settings) {
                    currentScreen = Screen.Home
                }

                DisposableEffect(Unit) {
                    val receiver = object : BroadcastReceiver() {
                        override fun onReceive(context: Context?, intent: Intent?) {
                            refreshTick += 1
                        }
                    }
                    val filter = IntentFilter(RecordingService.ACTION_RECORDING_SAVED)
                    registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
                    onDispose { unregisterReceiver(receiver) }
                }

                Surface(modifier = Modifier.fillMaxSize()) {
                    Scaffold(
                        contentWindowInsets = WindowInsets(0),
                        bottomBar = {
                            NavigationBar {
                                NavigationBarItem(
                                    selected = currentScreen == Screen.Home,
                                    onClick = { currentScreen = Screen.Home },
                                    icon = { Icon(Icons.Default.Home, contentDescription = stringResource(R.string.nav_home)) },
                                    label = { Text(stringResource(R.string.nav_home)) },
                                )
                                NavigationBarItem(
                                    selected = currentScreen == Screen.Settings,
                                    onClick = { currentScreen = Screen.Settings },
                                    icon = { Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.nav_settings)) },
                                    label = { Text(stringResource(R.string.nav_settings)) },
                                )
                                NavigationBarItem(
                                    selected = currentScreen == Screen.About,
                                    onClick = { currentScreen = Screen.About },
                                    icon = { Icon(Icons.Default.Info, contentDescription = stringResource(R.string.nav_about)) },
                                    label = { Text(stringResource(R.string.nav_about)) },
                                )
                            }
                        },
                    ) { innerPadding ->
                        AnimatedContent(
                            targetState = currentScreen,
                            modifier = Modifier.padding(innerPadding),
                            transitionSpec = {
                                val forward = targetState.ordinal > initialState.ordinal
                                if (forward) {
                                    (slideInHorizontally { it } + fadeIn())
                                        .togetherWith(slideOutHorizontally { -it / 3 } + fadeOut())
                                } else {
                                    (slideInHorizontally { -it } + fadeIn())
                                        .togetherWith(slideOutHorizontally { it / 3 } + fadeOut())
                                }
                            },
                            label = "screen_transition",
                        ) { screen ->
                            when (screen) {
                                Screen.Home -> HomeScreen(refreshTick = refreshTick)
                                Screen.Settings -> SettingsScreen(onBack = { currentScreen = Screen.Home })
                                Screen.About -> AboutScreen()
                            }
                        }
                    }
                }
            }
        }
    }

    private enum class Screen {
        Home,
        Settings,
        About,
    }

    private fun requestPermissionsAndStartService() {
        val needed = mutableListOf(Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= 33) {
            needed.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val notGranted = needed.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (notGranted.isEmpty()) {
            RecordingService.start(this)
        } else {
            permissionLauncher.launch(notGranted.toTypedArray())
        }
    }
}
