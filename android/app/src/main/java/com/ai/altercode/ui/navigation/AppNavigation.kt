package com.ai.altercode.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ai.altercode.ServiceLocator
import com.ai.altercode.ui.screens.HomeScreen
import com.ai.altercode.ui.screens.SavedScreen
import com.ai.altercode.ui.screens.SettingsScreen
import com.ai.altercode.ui.screens.SnippetDetailScreen
import com.ai.altercode.ui.screens.WelcomeScreen

object Routes {
    const val WELCOME = "welcome"
    const val HOME = "home"
    const val SAVED = "saved"
    const val SETTINGS = "settings"
    const val SNIPPET = "snippet"
    const val SNIPPET_ARG = "snippetId"
    const val SNIPPET_PATTERN = "$SNIPPET/{$SNIPPET_ARG}"

    fun snippet(id: Long): String = "$SNIPPET/$id"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val settings = ServiceLocator.settings
    val startDestination = if (settings.hasOnboarded) Routes.HOME else Routes.WELCOME

    fun switchTab(route: String) {
        navController.navigate(route) {
            popUpTo(Routes.HOME) { inclusive = route == Routes.HOME }
            launchSingleTop = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { fadeIn(animationSpec = tween(220)) },
        exitTransition = { fadeOut(animationSpec = tween(180)) }
    ) {
        composable(Routes.WELCOME) {
            WelcomeScreen(
                onGetStarted = {
                    settings.markOnboarded()
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.WELCOME) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                onOpenSnippet = { id -> navController.navigate(Routes.snippet(id)) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                onNavigateTab = ::switchTab
            )
        }

        composable(Routes.SAVED) {
            SavedScreen(
                onOpenSnippet = { id -> navController.navigate(Routes.snippet(id)) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                onNavigateTab = ::switchTab
            )
        }

        composable(
            route = Routes.SNIPPET_PATTERN,
            arguments = listOf(navArgument(Routes.SNIPPET_ARG) { type = NavType.LongType }),
            enterTransition = {
                slideInHorizontally(animationSpec = tween(260)) { it / 6 } + fadeIn(tween(260))
            },
            exitTransition = {
                slideOutHorizontally(animationSpec = tween(220)) { it / 6 } + fadeOut(tween(220))
            }
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong(Routes.SNIPPET_ARG) ?: -1L
            SnippetDetailScreen(
                snippetId = id,
                onBack = { navController.popBackStack() },
                onRunAgain = {
                    // Return to the EXISTING Home back stack entry so its
                    // view model stays alive and completes the pending
                    // EditorHandoff run. Rebuilding Home here would cancel
                    // the run started by "Run Again".
                    navController.popBackStack(Routes.HOME, inclusive = false)
                }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
