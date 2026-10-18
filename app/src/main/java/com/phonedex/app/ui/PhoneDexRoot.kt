package com.phonedex.app.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.phonedex.app.domain.AchievementDef
import com.phonedex.app.ui.achievements.AchievementsScreen
import com.phonedex.app.ui.achievements.UnlockDialog
import com.phonedex.app.ui.collection.BrandScreen
import com.phonedex.app.ui.collection.ModelScreen
import com.phonedex.app.ui.collection.RouteKey
import com.phonedex.app.ui.detail.DetailScreen
import com.phonedex.app.ui.edit.EditScreen
import com.phonedex.app.ui.history.HistoryScreen
import com.phonedex.app.ui.home.HomeScreen
import com.phonedex.app.ui.map.MapScreen
import com.phonedex.app.ui.stats.StatsScreen

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab("home", "Home", Icons.Rounded.Home),
    Tab("history", "History", Icons.Rounded.History),
    Tab("map", "Map", Icons.Rounded.Map),
    Tab("stats", "Stats", Icons.Rounded.BarChart)
)

@Composable
fun PhoneDexRoot(vm: MainViewModel) {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    val sightings by vm.sightings.collectAsStateWithLifecycle()
    val unlocked by vm.unlocked.collectAsStateWithLifecycle()
    var celebrate by remember { mutableStateOf<List<AchievementDef>>(emptyList()) }
    LaunchedEffect(Unit) { vm.unlockEvents.collect { celebrate = celebrate + it } }

    fun goTab(r: String) = nav.navigate(r) {
        popUpTo("home") { saveState = true }
        launchSingleTop = true
        restoreState = true
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            AnimatedVisibility(
                visible = tabs.any { it.route == route },
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                NavigationBar {
                    tabs.forEach { t ->
                        NavigationBarItem(
                            selected = route == t.route,
                            onClick = { goTab(t.route) },
                            icon = { Icon(t.icon, t.label) },
                            label = { Text(t.label) }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = route == "home" || route == "history",
                enter = scaleIn() + fadeIn(), exit = scaleOut() + fadeOut()
            ) {
                FloatingActionButton(
                    onClick = { nav.navigate("edit/0") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) { Icon(Icons.Rounded.Add, "Add sighting") }
            }
        }
    ) { pad ->
        NavHost(
            navController = nav,
            startDestination = "home",
            modifier = Modifier.padding(bottom = pad.calculateBottomPadding()),
            enterTransition = { fadeIn(tween(220)) + slideInVertically(tween(260)) { it / 16 } },
            exitTransition = { fadeOut(tween(160)) },
            popEnterTransition = { fadeIn(tween(220)) },
            popExitTransition = { fadeOut(tween(180)) + slideOutVertically(tween(220)) { it / 16 } }
        ) {
            composable("home") {
                HomeScreen(
                    sightings,
                    onOpenBrand = { nav.navigate("brand/${RouteKey.encode(it)}") },
                    onOpenStats = { goTab("stats") },
                    onOpenAchievements = { nav.navigate("achievements") { launchSingleTop = true } }
                )
            }
            composable("history") {
                HistoryScreen(sightings, onOpen = { nav.navigate("detail/$it") })
            }
            composable("map") {
                MapScreen(sightings, onOpenDetail = { nav.navigate("detail/$it") }, onOpenHistory = { goTab("history") })
            }
            composable("stats") { StatsScreen(sightings) }
            composable("achievements") {
                AchievementsScreen(sightings, unlocked, onBack = { nav.popBackStack() })
            }
            composable("brand/{key}", arguments = listOf(navArgument("key") { type = NavType.StringType })) { e ->
                val key = RouteKey.decode(e.arguments?.getString("key").orEmpty())
                BrandScreen(
                    brandKey = key, sightings = sightings,
                    onBack = { nav.popBackStack() },
                    onOpenModel = { nav.navigate("model/${RouteKey.encode(it)}") }
                )
            }
            composable("model/{key}", arguments = listOf(navArgument("key") { type = NavType.StringType })) { e ->
                val key = RouteKey.decode(e.arguments?.getString("key").orEmpty())
                ModelScreen(
                    modelKey = key, sightings = sightings,
                    onBack = { nav.popBackStack() },
                    onOpenSighting = { nav.navigate("detail/$it") }
                )
            }
            composable("detail/{id}", arguments = listOf(navArgument("id") { type = NavType.LongType })) { e ->
                val id = e.arguments?.getLong("id") ?: 0L
                val s by remember(id) { vm.observe(id) }.collectAsStateWithLifecycle(initialValue = null)
                DetailScreen(
                    sighting = s,
                    onBack = { nav.popBackStack() },
                    onEdit = { nav.navigate("edit/$id") },
                    onDelete = { vm.delete(id); nav.popBackStack() }
                )
            }
            composable("edit/{id}", arguments = listOf(navArgument("id") { type = NavType.LongType })) { e ->
                val id = e.arguments?.getLong("id") ?: 0L
                EditScreen(id = id, vm = vm, onClose = { nav.popBackStack() })
            }
        }
    }

    if (celebrate.isNotEmpty()) {
        UnlockDialog(
            unlocked = celebrate,
            onDismiss = { celebrate = emptyList() },
            onViewAll = {
                celebrate = emptyList()
                nav.navigate("achievements") { launchSingleTop = true }
            }
        )
    }
}
