package com.hananelsabag.volunteerhours.ui

import android.app.Application
import android.content.Intent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.hananelsabag.volunteerhours.data.SettingsRepository
import com.hananelsabag.volunteerhours.viewmodel.VolunteerViewModel

sealed class Screen(
    val route: String,
    val label: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector
) {
    object Home      : Screen("home",       "בית",     Icons.Filled.Home,     Icons.Outlined.Home)
    object Volunteers: Screen("volunteers", "מתנדבים", Icons.Filled.Group,    Icons.Outlined.Group)
    object Settings  : Screen("settings",  "הגדרות",  Icons.Filled.Settings, Icons.Outlined.Settings)
}

private val bottomScreens = listOf(Screen.Home, Screen.Volunteers, Screen.Settings)

@Composable
fun MainScreen(settingsRepo: SettingsRepository) {
    val context = LocalContext.current
    val factory = remember {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                VolunteerViewModel(context.applicationContext as Application) as T
        }
    }
    val vm: VolunteerViewModel = viewModel(factory = factory)

    val navController       = rememberNavController()
    val navBackStack        by navController.currentBackStackEntryAsState()
    val currentRoute        = navBackStack?.destination?.route
    val isTopLevel          = bottomScreens.any { it.route == currentRoute }
    val snackbarHost        = remember { SnackbarHostState() }
    val errorMsg            by vm.error.collectAsState(initial = null)
    var isExporting         by remember { mutableStateOf(false) }
    val hasSeenOnboarding   by settingsRepo.hasSeenOnboarding.collectAsState(initial = true)
    val scope               = rememberCoroutineScope()

    // Show DB errors as snackbar
    LaunchedEffect(errorMsg) {
        errorMsg?.let { snackbarHost.showSnackbar(it) }
    }

    val navItems = remember {
        bottomScreens.map { NavItem(it.route, it.label, it.selectedIcon, it.unselectedIcon) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHost) },
        bottomBar = {
            if (isTopLevel) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    GlassNavBar(
                        items        = navItems,
                        currentRoute = currentRoute,
                        onNavigate   = { route ->
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState    = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController     = navController,
            startDestination  = Screen.Home.route,
            modifier          = Modifier.padding(innerPadding),
            enterTransition   = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(250)) },
            exitTransition    = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(250)) },
            popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(250)) },
            popExitTransition  = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(250)) }
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    vm               = vm,
                    showOnboarding   = !hasSeenOnboarding,
                    onOnboardingSeen = { scope.launch { settingsRepo.setOnboardingSeen() } }
                )
            }
            composable(Screen.Volunteers.route) {
                VolunteersScreen(vm = vm, onVolunteerClick = { navController.navigate("volunteer/$it") })
            }
            composable(
                route     = "volunteer/{id}",
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { back ->
                val id = back.arguments?.getLong("id") ?: return@composable
                VolunteerDetailScreen(volunteerId = id, vm = vm, onBack = { navController.popBackStack() })
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    settingsRepo  = settingsRepo,
                    isExporting   = isExporting,
                    onExportClick = {
                        isExporting = true
                        vm.exportToCsv { uri ->
                            isExporting = false
                            uri?.let {
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/csv"
                                    putExtra(Intent.EXTRA_STREAM, it)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(intent, "שתף ייצוא שעות"))
                            }
                        }
                    }
                )
            }
        }
    }
}
