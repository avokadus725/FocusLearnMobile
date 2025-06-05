// Оновіть app/src/main/java/com/example/focuslearnmobile/ui/navigation/Navigation.kt

package com.example.focuslearnmobile.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.focuslearnmobile.R
import com.example.focuslearnmobile.ui.components.LanguageSwitcherButton
import com.example.focuslearnmobile.ui.statistics.StatisticsScreen
import com.example.focuslearnmobile.ui.timer.TimerScreen
import com.example.focuslearnmobile.ui.profile.ProfileScreen

sealed class NavigationItem(
    val route: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val titleRes: Int
) {
    object Timer : NavigationItem("timer", Icons.Default.Schedule, R.string.timer)
    object Profile : NavigationItem("profile", Icons.Default.Person, R.string.profile)
    object Statistics : NavigationItem("statistics", Icons.Default.BarChart, R.string.stats)
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigationScreen(
    onLogout: () -> Unit,
    onLanguageChanged: (() -> Unit)? = null
) {
    val navController = rememberNavController()
    val navigationItems = listOf(
        NavigationItem.Timer,
        NavigationItem.Profile,
        NavigationItem.Statistics
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    // Кнопка переключення мови
                    LanguageSwitcherButton(onLanguageChanged = onLanguageChanged)

                    // Кнопка виходу
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = stringResource(R.string.logout)
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                navigationItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = null) },
                        label = { Text(stringResource(item.titleRes)) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = NavigationItem.Timer.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(NavigationItem.Timer.route) {
                TimerScreen()
            }

            composable(NavigationItem.Profile.route) {
                ProfileScreen()
            }

            composable(NavigationItem.Statistics.route) {
                StatisticsScreen()
            }
        }
    }
}