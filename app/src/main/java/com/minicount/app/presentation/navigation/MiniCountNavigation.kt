package com.minicount.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.minicount.app.domain.export.BackupManager
import com.minicount.app.presentation.screens.addedit.AddEditEventScreen
import com.minicount.app.presentation.screens.home.HomeScreen
import com.minicount.app.presentation.screens.premium.PremiumScreen
import com.minicount.app.presentation.screens.settings.SettingsScreen
import com.minicount.app.presentation.screens.statistics.StatisticsScreen
import com.minicount.app.presentation.screens.onboarding.OnboardingScreen
import com.minicount.app.presentation.screens.calendar.CalendarScreen
import com.minicount.app.presentation.screens.history.EventHistoryScreen
import com.minicount.app.presentation.screens.widget.WidgetPreviewScreen

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object AddEvent : Screen("add_event")
    object EditEvent : Screen("edit_event/{eventId}") {
        fun createRoute(eventId: Long) = "edit_event/$eventId"
    }
    object Premium : Screen("premium")
    object Settings : Screen("settings")
    object Statistics : Screen("statistics")
    object Calendar : Screen("calendar")
    object EventHistory : Screen("event_history")
    object EventHistoryForEvent : Screen("event_history/{eventId}") {
        fun createRoute(eventId: Long) = "event_history/$eventId"
    }
    object WidgetPreview : Screen("widget_preview/{eventId}") {
        fun createRoute(eventId: Long) = "widget_preview/$eventId"
    }
}

@Composable
fun MiniCountNavigation(
    startWithOnboarding: Boolean = false,
    backupManager: BackupManager
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = if (startWithOnboarding) Screen.Onboarding.route else Screen.Home.route
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onComplete = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onAddEventClick = { navController.navigate(Screen.AddEvent.route) },
                onEditEventClick = { eventId ->
                    navController.navigate(Screen.EditEvent.createRoute(eventId))
                },
                onPremiumClick = { navController.navigate(Screen.Premium.route) },
                onSettingsClick = { navController.navigate(Screen.Settings.route) },
                onStatisticsClick = { navController.navigate(Screen.Statistics.route) },
                onCalendarClick = { navController.navigate(Screen.Calendar.route) },
                onHistoryClick = { navController.navigate(Screen.EventHistory.route) }
            )
        }

        composable(Screen.AddEvent.route) {
            AddEditEventScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.EditEvent.route,
            arguments = listOf(navArgument("eventId") { type = NavType.LongType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getLong("eventId") ?: return@composable
            AddEditEventScreen(
                eventId = eventId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Premium.route) {
            PremiumScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onPremiumClick = { navController.navigate(Screen.Premium.route) },
                backupManager = backupManager
            )
        }

        composable(Screen.Statistics.route) {
            StatisticsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Calendar.route) {
            CalendarScreen(
                onNavigateBack = { navController.popBackStack() },
                onEventClick = { eventId ->
                    navController.navigate(Screen.EditEvent.createRoute(eventId))
                }
            )
        }

        composable(Screen.EventHistory.route) {
            EventHistoryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.EventHistoryForEvent.route,
            arguments = listOf(navArgument("eventId") { type = NavType.LongType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getLong("eventId") ?: return@composable
            EventHistoryScreen(
                onNavigateBack = { navController.popBackStack() },
                eventId = eventId
            )
        }

        composable(
            route = Screen.WidgetPreview.route,
            arguments = listOf(navArgument("eventId") { type = NavType.LongType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getLong("eventId") ?: return@composable
            WidgetPreviewScreen(
                onNavigateBack = { navController.popBackStack() },
                eventId = eventId
            )
        }
    }
}
