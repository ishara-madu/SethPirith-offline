package com.pixeleye.sethpirith.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pixeleye.sethpirith.R
import com.pixeleye.sethpirith.data.PirithDataProvider.pirithList
import com.pixeleye.sethpirith.ui.components.BackgroundWrapper
import com.pixeleye.sethpirith.ui.components.Player
import com.pixeleye.sethpirith.ui.theme.Ternary
import com.pixeleye.sethpirith.ui.util.LimitCharacters
import com.pixeleye.sethpirith.ui.util.PirithPrefs


@Composable
fun PlayerScreen(onNavigateToSettings: () -> Unit, onNavigateToList: () -> Unit) {
    val context = LocalContext.current
    val playingId by PirithPrefs.playingIdFlow(context)
        .collectAsState(initial = PirithPrefs.getLastAudioId(context))
    val playingTitle = stringResource(id = pirithList[playingId].titleResId)
    val playingLyrics = stringResource(id = pirithList[playingId].lyricsResId)

    val IskoolaPota = FontFamily(Font(R.font.iskoola_pota))

    BackgroundWrapper {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(WindowInsets.safeDrawing.asPaddingValues())
        ) {
            // Top Row (Navigation Bar)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Back Icon
                Box(
                    modifier = Modifier.clickable { onNavigateToList() }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }

                // Title
                Text(
                    text = LimitCharacters(playingTitle, 19),
                    color = Ternary,
                    style = MaterialTheme.typography.titleSmall,
                )

                // Settings Icon
                Box(
                    modifier = Modifier.clickable { onNavigateToSettings() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            // Lyrics Section
            val scrollState = rememberScrollState()
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        16.dp,50.dp,16.dp,220.dp
                    )  // Add padding to avoid overlap with top and bottom
                    .verticalScroll(scrollState),
            ) {

                Text(
                    text = playingLyrics,
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    fontFamily = IskoolaPota
                )

            }

            // Player Section

            Player()

        }
    }
}