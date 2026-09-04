package com.juguito.juguitoreader.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AppRegistration
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryAdd
import androidx.compose.material.icons.filled.NewLabel
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.ui.book.add.AddBookScreen
import com.juguito.juguitoreader.ui.book.detail.BookDetailScreen
import com.juguito.juguitoreader.ui.folder.FolderScreen
import com.juguito.juguitoreader.ui.genre.AddGenreDialog
import com.juguito.juguitoreader.ui.home.HomeScreen
import com.juguito.juguitoreader.ui.library.LibraryScreen
import com.juguito.juguitoreader.ui.management.ManagementScreen
import com.juguito.juguitoreader.ui.reader.ReaderScreen
import com.juguito.juguitoreader.ui.registry.RegistryScreen
import com.juguito.juguitoreader.ui.settings.SettingsScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JuguitoApp() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    var showAddGenreDialog by remember { mutableStateOf(false) }

    if (showAddGenreDialog) {
        AddGenreDialog(
            onDismissRequest = {showAddGenreDialog = false},
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = currentRoute != "reader/{bookId}",
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.fillMaxWidth(0.75f),
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                drawerShape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
            ) {
                DrawerHeader()
                
                Spacer(modifier = Modifier.height(12.dp))

                DrawerItem(
                    label = stringResource(R.string.home_label),
                    icon = Icons.Default.Home,
                    selected = currentRoute == "home",
                    onClick = { 
                        scope.launch { 
                            drawerState.close()
                            navController.navigate("home") {
                                popUpTo("home") { inclusive = true }
                            }
                        } 
                    }
                )

                DrawerItem(
                    label = stringResource(R.string.registry_label),
                    icon = Icons.Default.AppRegistration,
                    selected = currentRoute == "registry",
                    onClick = { 
                        scope.launch { 
                            drawerState.close()
                            navController.navigate("registry")
                        } 
                    }
                )

                DrawerItem(
                    label = stringResource(R.string.library_label),
                    icon = Icons.Default.AutoStories,
                    selected = currentRoute == "library",
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            navController.navigate("library")
                        }
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))

                Text(
                    text = stringResource(R.string.management_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 28.dp, bottom = 8.dp)
                )

                DrawerItem(
                    label = stringResource(R.string.content_manager),
                    icon = Icons.Default.Widgets,
                    selected = currentRoute == "management",
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            navController.navigate("management")
                        }
                    }
                )

                DrawerItem(
                    label = stringResource(R.string.create_book),
                    icon = Icons.Default.LibraryAdd,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            navController.navigate("add_book")
                        }
                    }
                )

                DrawerItem(
                    label = stringResource(R.string.new_folder),
                    icon = Icons.Default.CreateNewFolder,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            navController.navigate("add_folder")
                        }
                    }
                )

                DrawerItem(
                    label = stringResource(R.string.create_genre),
                    icon = Icons.Default.NewLabel,
                    onClick = {
                        scope.launch { drawerState.close() }
                        showAddGenreDialog = true
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))

                Spacer(modifier = Modifier.weight(1f))

                DrawerItem(
                    label = stringResource(R.string.settings),
                    icon = Icons.Default.Settings,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("settings")
                    }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            NavHost(
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
                        onOpenDrawer = { scope.launch { drawerState.open() } },
                        onNavigateToAddBook = {
                            navController.navigate("add_book")
                        },
                        onNavigateToBookDetail = { bookId ->
                            navController.navigate("book_detail/$bookId")
                        },
                        onNavigateToReadBook = { bookId ->
                            navController.navigate("reader/$bookId")
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
                        managementMessage = snackbarMessage,
                        onClearManagementMessage = {
                            savedStateHandle.remove<Int>("snackbar_result")
                        },
                        onNavigateBack = { navController.popBackStack() },
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
                        onNavigateToAddFolder = { navController.navigate("add_folder")}
                    )
                }

                composable (route = "registry") {
                    RegistryScreen(
                        onOpenDrawer = { scope.launch { drawerState.open() } },
                        onNavigateToAddBook = {
                            navController.navigate("add_book")
                        },
                        onNavigateToBookDetail = { bookId ->
                            navController.navigate("book_detail/$bookId")
                        }
                    )
                }

                composable (route = "library") {
                    LibraryScreen(
                        onOpenDrawer = { scope.launch { drawerState.open() } },
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

                composable (
                    route = "reader/{bookId}",
                    arguments = listOf(navArgument("bookId") { type = NavType.IntType })
                ) {
                    ReaderScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                
                composable (
                    route = "settings"
                ) {
                    SettingsScreen(
                        onNavigateBack = { navController.popBackStack() },
                    )
                }
            }
        }
    }
}

@Composable
fun DrawerHeader() {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(colorScheme.primary, colorScheme.secondary)
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.BottomStart
    ) {
        Column {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                contentDescription = null,
                tint = colorScheme.onPrimary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                color = colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.your_personal_library),
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onPrimary.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun DrawerItem(
    label: String,
    icon: ImageVector,
    selected: Boolean = false,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        label = { 
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            ) 
        },
        selected = selected,
        onClick = onClick,
        icon = { Icon(icon, contentDescription = null) },
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurface
        ),
        shape = RoundedCornerShape(12.dp)
    )
}
