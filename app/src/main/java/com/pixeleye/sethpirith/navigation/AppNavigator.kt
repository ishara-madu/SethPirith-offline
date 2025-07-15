package com.pixeleye.sethpirith.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.*
import androidx.navigation.compose.NavHost
import com.pixeleye.sethpirith.navigation.Screen
import com.pixeleye.sethpirith.ui.screens.ListScreen
import com.pixeleye.sethpirith.ui.screens.PlayerScreen
import com.pixeleye.sethpirith.ui.screens.SettingsScreen
import com.pixeleye.sethpirith.ui.screens.SplashScreen
import com.pixeleye.sethpirith.ui.util.PirithPrefs

@Composable
fun AppNavigator() {
    val context = LocalContext.current
    val playingId = remember { PirithPrefs.getLastAudioId(context) }
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(onSplashComplete = {
                if (playingId != -1)
                    navController.navigate(Screen.Player.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                else
                    navController.navigate(Screen.List.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
            })
        }

        composable(Screen.List.route) {
            ListScreen(
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToPlayer = {
                    navController.navigate(Screen.Player.route)
                }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = {
                navController.popBackStack()
            },
                onNavigateToList = {
                    navController.navigate(Screen.List.route)
                }
            )
        }
        composable(Screen.Player.route) {
            PlayerScreen(
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToList = {
                    navController.navigate(Screen.List.route)
                }
            )
        }
    }
}
