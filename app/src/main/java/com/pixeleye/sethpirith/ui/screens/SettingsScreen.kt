package com.pixeleye.sethpirith.ui.screens

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.pixeleye.sethpirith.MainActivity
import com.pixeleye.sethpirith.R
import com.pixeleye.sethpirith.service.MediaPlayerService
import com.pixeleye.sethpirith.ui.components.BackgroundWrapper
import com.pixeleye.sethpirith.ui.components.CustomLanguageDropdown
import com.pixeleye.sethpirith.ui.components.CustomSleepTimeDropdown
import com.pixeleye.sethpirith.ui.theme.Primary
import com.pixeleye.sethpirith.ui.util.PirithPrefs
import com.pixeleye.sethpirith.ui.util.WebUtils

@Composable
fun SettingsScreen(onBack: () -> Unit, onNavigateToList: () -> Unit) {
    val context = LocalContext.current
    var mediaPlayerService by remember { mutableStateOf<MediaPlayerService?>(null) }
    val showDialog = remember { mutableStateOf(false) }

    // Service connection
    val serviceConnection = remember {
        object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder = service as MediaPlayerService.MediaPlayerBinder
                mediaPlayerService = binder.getService()
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                mediaPlayerService = null
            }
        }
    }

    // Bind to service
    LaunchedEffect(Unit) {
        val intent = Intent(context, MediaPlayerService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    // Clean up
    DisposableEffect(Unit) {
        onDispose {
            context.unbindService(serviceConnection)
        }
    }

    fun reset() {
        // Clear all app data (SharedPreferences and files)
        mediaPlayerService?.onDestroy()
        PirithPrefs.clear(context)
        onNavigateToList()
    }

    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            title = { Text(stringResource(R.string.reset_title)) },
            text = { Text(stringResource(R.string.reset_description)) },
            confirmButton = {
                TextButton(onClick = {
                    reset()
                    showDialog.value = false
                }) {
                    Text(
                        text = stringResource(R.string.label_confirm),
                        color = Color.Red
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog.value = false }) {
                    Text(
                        text = stringResource(R.string.label_cancel),
                    )
                }
            }
        )
    }

    BackgroundWrapper {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(WindowInsets.safeDrawing.asPaddingValues())
        ) {
            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // 🔹 Top Row with Back Icon
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier.clickable { onBack() }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }

                // 🔹 Scrollable Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(scrollState)
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = stringResource(R.string.label_language),
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    CustomLanguageDropdown()
                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = stringResource(R.string.label_sleep_timer),
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    CustomSleepTimeDropdown()
                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = stringResource(R.string.button_reset_all),
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                onClick = { showDialog.value = true },
                                onClickLabel = "Show reset all dialog"
                            )
                            .background(Color.Red.copy(0.8f), shape = RoundedCornerShape(8.dp))
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.button_reset),
                            color = Color.White,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = stringResource(R.string.label_other_links),
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                WebUtils.openWebsite(
                                    context,
                                    "market://search?q=pub:com.pixeleye.lteonly"
                                )
                            }
                            .background(Primary, shape = RoundedCornerShape(8.dp))
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.button_rate_us),
                            color = Color.White,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                WebUtils.openWebsite(
                                    context,
                                    "https://ishara-madu.github.io/"
                                )
                            }
                            .background(Primary, shape = RoundedCornerShape(8.dp))
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.label_about_me),
                            color = Color.White,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        text = stringResource(R.string.text_support_message),
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                WebUtils.openWebsite(context, "https://coff.ee/ishara.madu")
                            }
                            .background(Primary, shape = RoundedCornerShape(8.dp))
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.button_support),
                            color = Color.White,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp)) // Final spacing
                }
            }
        }
    }
}