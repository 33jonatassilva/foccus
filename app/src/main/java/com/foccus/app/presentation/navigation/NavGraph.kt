package com.foccus.app.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.foccus.app.data.local.preferences.UserPreferences
import com.foccus.app.presentation.ui.blocklist.BlockListScreen
import com.foccus.app.presentation.ui.focus.FocusScreen
import com.foccus.app.presentation.ui.home.HomeScreen
import com.foccus.app.presentation.ui.onboarding.OnboardingScreen
import com.foccus.app.presentation.ui.permissions.PermissionsScreen
import com.foccus.app.presentation.ui.settings.SettingsScreen
import com.foccus.app.presentation.ui.stats.StatsScreen

@Composable
fun FoccusNavGraph(
    navController: NavHostController = rememberNavController(),
    userPreferences: UserPreferences = hiltViewModel<NavViewModel>().preferences
) {
    val onboardingCompleted by userPreferences.onboardingCompleted.collectAsState(initial = false)
    val startDestination = if (onboardingCompleted) Screen.Home.route else Screen.Onboarding.route

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            fadeIn(animationSpec = tween(300)) +
                    slideInHorizontally(animationSpec = tween(300)) { it / 4 }
        },
        exitTransition = {
            fadeOut(animationSpec = tween(300)) +
                    slideOutHorizontally(animationSpec = tween(300)) { -it / 4 }
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(300)) +
                    slideInHorizontally(animationSpec = tween(300)) { -it / 4 }
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(300)) +
                    slideOutHorizontally(animationSpec = tween(300)) { it / 4 }
        }
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onComplete = {
                    navController.navigate(Screen.Permissions.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Permissions.route) {
            PermissionsScreen(
                onContinue = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Permissions.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToFocus = { navController.navigate(Screen.Focus.route) },
                onNavigateToBlockList = { navController.navigate(Screen.BlockList.route) },
                onNavigateToStats = { navController.navigate(Screen.Stats.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.Focus.route) {
            FocusScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.BlockList.route) {
            BlockListScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Stats.route) {
            StatsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
