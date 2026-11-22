package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.GridOn
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.GridOn
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.repository.ThemeMode
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PlayScreen
import com.example.ui.screens.RecordsScreen
import com.example.ui.theme.LexiGridTheme
import com.example.ui.viewmodel.GameViewModel

const val ROUTE_HOME = "home"
const val ROUTE_PLAY = "play"
const val ROUTE_RECORDS = "records"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LexiGridApp()
        }
    }
}

@Composable
fun LexiGridApp(
    gameViewModel: GameViewModel = viewModel()
) {
    val uiState by gameViewModel.uiState.collectAsStateWithLifecycle()
    val recordsUiState by gameViewModel.recordsUiState.collectAsStateWithLifecycle()

    val isDarkTheme = when (uiState.themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> gameViewModel.setAppForeground(true)
                Lifecycle.Event.ON_STOP -> gameViewModel.setAppForeground(false)
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: ROUTE_HOME

    LaunchedEffect(currentRoute) {
        gameViewModel.setPlayScreenActive(currentRoute == ROUTE_PLAY)
    }

    fun navigateToTab(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    val recordsByLevelMap = recordsUiState.levelRecords.associateBy { it.level }

    LexiGridTheme(darkTheme = isDarkTheme) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useRail = maxWidth >= 640.dp

            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                contentWindowInsets = WindowInsets.safeDrawing,
                bottomBar = {
                    if (!useRail) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 0.dp
                        ) {
                            NavigationBarItem(
                                selected = currentRoute == ROUTE_HOME,
                                onClick = { navigateToTab(ROUTE_HOME) },
                                icon = {
                                    Icon(
                                        imageVector = if (currentRoute == ROUTE_HOME) {
                                            Icons.Rounded.Home
                                        } else {
                                            Icons.Outlined.Home
                                        },
                                        contentDescription = stringResource(R.string.nav_home)
                                    )
                                },
                                label = { Text(stringResource(R.string.nav_home)) },
                                modifier = Modifier.testTag("nav_home_tab")
                            )
                            NavigationBarItem(
                                selected = currentRoute == ROUTE_PLAY,
                                onClick = { navigateToTab(ROUTE_PLAY) },
                                icon = {
                                    Icon(
                                        imageVector = if (currentRoute == ROUTE_PLAY) {
                                            Icons.Rounded.GridOn
                                        } else {
                                            Icons.Outlined.GridOn
                                        },
                                        contentDescription = stringResource(R.string.nav_play)
                                    )
                                },
                                label = { Text(stringResource(R.string.nav_play)) },
                                modifier = Modifier.testTag("nav_play_tab")
                            )
                            NavigationBarItem(
                                selected = currentRoute == ROUTE_RECORDS,
                                onClick = { navigateToTab(ROUTE_RECORDS) },
                                icon = {
                                    Icon(
                                        imageVector = if (currentRoute == ROUTE_RECORDS) {
                                            Icons.Rounded.EmojiEvents
                                        } else {
                                            Icons.Outlined.EmojiEvents
                                        },
                                        contentDescription = stringResource(R.string.nav_records)
                                    )
                                },
                                label = { Text(stringResource(R.string.nav_records)) },
                                modifier = Modifier.testTag("nav_records_tab")
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (useRail) {
                        NavigationRail(
                            containerColor = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            Spacer(modifier = Modifier.weight(1f))
                            NavigationRailItem(
                                selected = currentRoute == ROUTE_HOME,
                                onClick = { navigateToTab(ROUTE_HOME) },
                                icon = {
                                    Icon(
                                        imageVector = if (currentRoute == ROUTE_HOME) {
                                            Icons.Rounded.Home
                                        } else {
                                            Icons.Outlined.Home
                                        },
                                        contentDescription = stringResource(R.string.nav_home)
                                    )
                                },
                                label = { Text(stringResource(R.string.nav_home)) },
                                modifier = Modifier.testTag("nav_home_rail")
                            )
                            NavigationRailItem(
                                selected = currentRoute == ROUTE_PLAY,
                                onClick = { navigateToTab(ROUTE_PLAY) },
                                icon = {
                                    Icon(
                                        imageVector = if (currentRoute == ROUTE_PLAY) {
                                            Icons.Rounded.GridOn
                                        } else {
                                            Icons.Outlined.GridOn
                                        },
                                        contentDescription = stringResource(R.string.nav_play)
                                    )
                                },
                                label = { Text(stringResource(R.string.nav_play)) },
                                modifier = Modifier.testTag("nav_play_rail")
                            )
                            NavigationRailItem(
                                selected = currentRoute == ROUTE_RECORDS,
                                onClick = { navigateToTab(ROUTE_RECORDS) },
                                icon = {
                                    Icon(
                                        imageVector = if (currentRoute == ROUTE_RECORDS) {
                                            Icons.Rounded.EmojiEvents
                                        } else {
                                            Icons.Outlined.EmojiEvents
                                        },
                                        contentDescription = stringResource(R.string.nav_records)
                                    )
                                },
                                label = { Text(stringResource(R.string.nav_records)) },
                                modifier = Modifier.testTag("nav_records_rail")
                            )
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }

                    NavHost(
                        navController = navController,
                        startDestination = ROUTE_HOME,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                    ) {
                        composable(ROUTE_HOME) {
                            HomeScreen(
                                uiState = uiState,
                                stats = recordsUiState.stats,
                                recordsByLevel = recordsByLevelMap,
                                onContinueCurrent = { navigateToTab(ROUTE_PLAY) },
                                onStartLevel = { level ->
                                    gameViewModel.goToLevel(level)
                                    navigateToTab(ROUTE_PLAY)
                                },
                                onCycleTheme = gameViewModel::cycleThemeMode,
                                onToggleSound = gameViewModel::toggleSound
                            )
                        }
                        composable(ROUTE_PLAY) {
                            PlayScreen(
                                uiState = uiState,
                                recordsByLevel = recordsByLevelMap,
                                isDarkTheme = isDarkTheme,
                                onTogglePause = gameViewModel::togglePause,
                                onSelectionStep = gameViewModel::onGridSelectionStep,
                                onArmedCellChanged = gameViewModel::setArmedStartCell,
                                onSubmitSelection = gameViewModel::submitSelection,
                                onUseHint = gameViewModel::useHint,
                                onNewBoard = gameViewModel::generateFreshBoardForCurrentLevel,
                                onNextLevel = gameViewModel::nextLevel,
                                onReplayLevel = gameViewModel::replayCurrentLevel,
                                onSelectLevel = gameViewModel::goToLevel,
                                onCycleTheme = gameViewModel::cycleThemeMode,
                                onToggleSound = gameViewModel::toggleSound
                            )
                        }
                        composable(ROUTE_RECORDS) {
                            RecordsScreen(
                                recordsUiState = recordsUiState,
                                onReplayLevel = { level ->
                                    gameViewModel.goToLevel(level)
                                    navigateToTab(ROUTE_PLAY)
                                },
                                onClearAllRecords = gameViewModel::clearAllRecords
                            )
                        }
                    }
                }
            }
        }
    }
}
