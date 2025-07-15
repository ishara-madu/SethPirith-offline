// ui/screens/list/ListScreen.kt
package com.pixeleye.sethpirith.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.pixeleye.sethpirith.data.PirithDataProvider
import com.pixeleye.sethpirith.ui.components.BackgroundWrapper
import com.pixeleye.sethpirith.ui.components.PirithItem

@Composable
fun ListScreen(onNavigateToSettings: () -> Unit,onNavigateToPlayer: () -> Unit,) {
    val context = LocalContext.current
    // Intercept back press
    BackHandler {
        // Exit the app when back pressed on ListScreen
        (context as? android.app.Activity)?.finish()
    }
    BackgroundWrapper {
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.safeDrawing.asPaddingValues())
        ) {
            // 🔹 Top-right icon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {

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

            LazyColumn(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 50.dp, start = 16.dp, end = 16.dp) // Leave space below icon
            ) {
                items(PirithDataProvider.pirithList) { pirith ->
                    PirithItem(pirith = pirith,onNavigateToPlayer = onNavigateToPlayer)
                }
            }
        }
    }
}

