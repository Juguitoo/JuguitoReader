package com.juguito.juguitoreader.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AppRegistration
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.enums.BookStatus
import com.juguito.juguitoreader.ui.about.AboutScreen
import com.juguito.juguitoreader.ui.book.add.AddBookScreen
import com.juguito.juguitoreader.ui.book.detail.BookDetailScreen
import com.juguito.juguitoreader.ui.changelog.ChangelogScreen
import com.juguito.juguitoreader.ui.changelog.WhatsNewDialog
import com.juguito.juguitoreader.ui.changelog.WhatsNewEvent
import com.juguito.juguitoreader.ui.changelog.WhatsNewUiState
import com.juguito.juguitoreader.ui.changelog.WhatsNewViewModel
import com.juguito.juguitoreader.ui.folder.FolderScreen
import com.juguito.juguitoreader.ui.home.HomeScreen
import com.juguito.juguitoreader.ui.library.LibraryScreen
import com.juguito.juguitoreader.ui.management.ManagementScreen
import com.juguito.juguitoreader.ui.onboarding.OnboardingEvent
import com.juguito.juguitoreader.ui.onboarding.OnboardingScreen
import com.juguito.juguitoreader.ui.onboarding.OnboardingUiState
import com.juguito.juguitoreader.ui.onboarding.OnboardingViewModel
import com.juguito.juguitoreader.ui.reader.ReaderScreen
import com.juguito.juguitoreader.ui.registry.RegistryScreen
import com.juguito.juguitoreader.ui.settings.SettingsScreen
import com.juguito.juguitoreader.ui.stats.StatsScreen
import com.juguito.juguitoreader.ui.theme.chromeNavInactiveColor
import kotlinx.coroutines.launch

@Composable
fun JuguitoApp(
    whatsNewViewModel: WhatsNewViewModel = hiltViewModel(),
    onboardingViewModel: OnboardingViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val whatsNewState by whatsNewViewModel.uiState.collectAsState()
    val onboardingState by onboardingViewModel.uiState.collectAsState()
    val isOnboardingVisible = onboardingState is OnboardingUiState.Visible
    val isWhatsNewVisible = whatsNewState is WhatsNewUiState.Visible && !isOnboardingVisible

    if (isWhatsNewVisible) {
        WhatsNewDialog(
            state = whatsNewState,
            onDismissRequest = { whatsNewViewModel.onEvent(WhatsNewEvent.OnDismiss) }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar =
            {
                val density = LocalDensity.current
                val isImeVisible = WindowInsets.ime.getBottom(density) > 0

                if (shouldShowNavigationBar(currentRoute) && !isImeVisible) {
                    JuguitoNavigationBar(
                        currentRoute = currentRoute,
                        onHomeClick = {
                            scope.launch {
                                navController.navigateToTopLevel("home")
                            }
                        },
                        onLibraryClick = {
                            scope.launch {
                                navController.navigateToTopLevel("library")
                            }
                        },
                        onRegistryClick = {
                            scope.launch {
                                navController.navigateToTopLevel("registry")
                            }
                        },
                        onStatsClick = {
                            scope.launch {
                                navController.navigateToTopLevel("statistics")
                            }
                        }
                    )
                }
            }
    ) { paddingValues ->
        NavHost(
            modifier = Modifier.padding(bottom = paddingValues.calculateBottomPadding()),
            navController = navController,
            startDestination = "home",
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            composable(route = "home") { backStackEntry ->
                val savedStateHandle = backStackEntry.savedStateHandle
                val snackbarMessage by savedStateHandle
                    .getStateFlow<Int?>("snackbar_result", null)
                    .collectAsState()

                HomeScreen(
                    snackbarMessage = snackbarMessage,
                    onClearSnackbarMessage = {
                        savedStateHandle.remove<Int>("snackbar_result")
                    },
                    onNavigateToAddBook = {
                        navController.navigate("add_book")
                    },
                    onNavigateToBookDetail = { bookId ->
                        navController.navigate("book_detail/$bookId")
                    },
                    onNavigateToReadBook = { bookId ->
                        navController.navigate("reader/$bookId")
                    },
                    onNavigateToSettings = {
                        navController.navigate("settings")
                    },
                    onViewActiveReadings = {
                        navController.navigate("library?status=${BookStatus.READING.name}")
                    },
                    onViewFavourites = {
                        navController.navigate("library?favourites=true")
                    },
                    onViewPending = {
                        navController.navigate("library?status=${BookStatus.PENDING.name}")
                    }
                )
            }

            composable(route = "add_book") {
                AddBookScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onBookSavedSuccessfully = {
                        navController.popBackStack()
                    },
                    onNavigateToAddFolder = { navController.navigate("add_folder") }
                )
            }

            composable(route = "add_folder") {
                FolderScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onFolderSavedSuccessfully = {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("snackbar_result", R.string.folder_created)
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = "edit_folder/{folderId}",
                arguments = listOf(navArgument("folderId") { type = NavType.IntType })
            ) {
                FolderScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onFolderSavedSuccessfully = {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("snackbar_result", R.string.folder_updated)
                        navController.popBackStack()
                    }
                )
            }

            composable(route = "management") { backStackEntry ->
                val savedStateHandle = backStackEntry.savedStateHandle
                val snackbarMessage by savedStateHandle
                    .getStateFlow<Int?>("snackbar_result", null)
                    .collectAsState()

                ManagementScreen(
                    onNavigateBack = { navController.popBackStack() },
                    managementMessage = snackbarMessage,
                    onClearManagementMessage = {
                        savedStateHandle.remove<Int>("snackbar_result")
                    },
                    onNavigateToEditFolder = { folderId ->
                        navController.navigate("edit_folder/$folderId")
                    },
                    onNavigateToAddFolder = { navController.navigate("add_folder") }
                )
            }

            composable(
                route = "book_detail/{bookId}",
                arguments = listOf(navArgument("bookId") { type = NavType.IntType })
            ) {
                BookDetailScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToAddFolder = { navController.navigate("add_folder") },
                    onNavigateToReadBook = { bookId ->
                        navController.navigate("reader/$bookId")
                    }
                )
            }

            composable(route = "registry") {
                RegistryScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToAddBook = {
                        navController.navigate("add_book")
                    },
                    onNavigateToBookDetail = { bookId ->
                        navController.navigate("book_detail/$bookId")
                    },
                    onNavigateToContentManager = {
                        navController.navigate("management")
                    }
                )
            }

            composable(route = "statistics") {
                StatsScreen(
                    onNavigateToBookDetail = { bookId ->
                        navController.navigate("book_detail/$bookId")
                    },
                    onViewFinishedBooks = {
                        navController.navigate("library?status=${BookStatus.FINISHED.name}")
                    },
                )
            }

            composable(
                route = "library?favourites={favourites}&status={status}",
                arguments = listOf(
                    navArgument("favourites") {
                        type = NavType.BoolType
                        defaultValue = false
                    },
                    navArgument("status") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) {
                LibraryScreen(
                    onNavigateToContentManager = {
                        navController.navigate("management")
                    },
                    onNavigateToAddBook = {
                        navController.navigate("add_book")
                    },
                    onNavigateToReadBook = { bookId ->
                        navController.navigate("reader/$bookId")
                    },
                    onNavigateToBookDetail = { bookId ->
                        navController.navigate("book_detail/$bookId")
                    }
                )
            }

            composable(
                route = "reader/{bookId}",
                arguments = listOf(navArgument("bookId") { type = NavType.IntType })
            ) {
                ReaderScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = "settings"
            ) {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToAbout = { navController.navigate("about") }
                )
            }

            composable(route = "about") {
                AboutScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onOpenChangelog = { navController.navigate("changelog") }
                )
            }

            composable(route = "changelog") {
                ChangelogScreen(
                    onNavigateBack = { navController.popBackStack() },
                )
            }
        }
    }

    if (isOnboardingVisible) {
        OnboardingScreen(
            onFinished = { onboardingViewModel.onEvent(OnboardingEvent.OnFinished) }
        )
    }
}

internal fun shouldShowNavigationBar(route: String?): Boolean {
    if (route == null) return false
    return route == "home" ||
            route == "registry" ||
            route == "statistics" ||
            route.matchesTopLevel("library")
}

private fun String?.matchesTopLevel(route: String): Boolean {
    if (this == null) return false
    return this == route || this.startsWith("$route?")
}

private fun NavHostController.navigateToTopLevel(route: String) {
    navigate(route) {
        popUpTo(graph.id) {
            inclusive = false
        }
        launchSingleTop = true
    }
}

@Composable
private fun JuguitoNavigationBar(
    currentRoute: String?,
    onHomeClick: () -> Unit,
    onLibraryClick: () -> Unit,
    onRegistryClick: () -> Unit,
    onStatsClick: () -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.background) {
        Column {
            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(NavigationBarDefaults.windowInsets),
                verticalAlignment = Alignment.CenterVertically
            ) {
                JuguitoNavigationBarItem(
                    selected = currentRoute == "home",
                    icon = Icons.Default.Home,
                    label = stringResource(R.string.home_label),
                    onClick = onHomeClick
                )
                JuguitoNavigationBarItem(
                    selected = currentRoute.matchesTopLevel("library"),
                    icon = Icons.Default.AutoStories,
                    label = stringResource(R.string.library_label),
                    onClick = onLibraryClick
                )
                JuguitoNavigationBarItem(
                    selected = currentRoute == "registry",
                    icon = Icons.Default.AppRegistration,
                    label = stringResource(R.string.registry_label),
                    onClick = onRegistryClick
                )
                JuguitoNavigationBarItem(
                    selected = currentRoute == "statistics",
                    icon = Icons.Default.BarChart,
                    label = stringResource(R.string.stats),
                    onClick = onStatsClick
                )
            }
        }
    }
}

@Composable
private fun RowScope.JuguitoNavigationBarItem(
    selected: Boolean,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    val color = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        chromeNavInactiveColor()
    }
    Column(
        modifier = Modifier
            .weight(1f)
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.Tab
            )
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            color = color,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
