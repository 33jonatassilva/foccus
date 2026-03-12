package com.foccus.app.presentation.navigation

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Permissions : Screen("permissions")
    object Home : Screen("home")
    object Focus : Screen("focus")
    object BlockList : Screen("block_list")
    object Stats : Screen("stats")
    object Settings : Screen("settings")
}
