package com.pixeleye.sethpirith.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object List : Screen("list")
    data object Settings : Screen("settings")
    data object Player : Screen("player")
}
