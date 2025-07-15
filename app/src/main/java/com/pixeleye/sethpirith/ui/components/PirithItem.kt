package com.pixeleye.sethpirith.ui.components

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.pixeleye.sethpirith.R
import com.pixeleye.sethpirith.data.Pirith
import com.pixeleye.sethpirith.ui.theme.Primary
import com.pixeleye.sethpirith.ui.theme.Secondary
import com.pixeleye.sethpirith.ui.util.PirithPrefs

@Composable
fun PirithItem(
    pirith: Pirith,
    modifier: Modifier = Modifier,
    onNavigateToPlayer: () -> Unit,
) {
    val context = LocalContext.current
    val title = stringResource(id = pirith.titleResId)


    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val backgroundColor = if (isPressed) Primary else Secondary
    val iconTint = if (isPressed) Color.Black else Color.White
    val playingId by PirithPrefs.playingIdFlow(context).collectAsState(initial = PirithPrefs.getLastAudioId(context))
    val isPlaying by PirithPrefs.isPlayingFlow(context).collectAsState(initial = PirithPrefs.isPlaying(context))


    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {


                // Save state to SharedPreferences
                PirithPrefs.saveAudioState(context, pirith.id, true)

                // Navigate
                onNavigateToPlayer()
            },
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                if (pirith.id == playingId) {
                    if (isPlaying) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_play_arrow_24),
                            contentDescription = "Play",
                            tint = iconTint,
                            modifier = Modifier.size(30.dp)
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_pause_24),
                            contentDescription = "Pause",
                            tint = iconTint,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            }

            //   Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
