package com.juguito.juguitoreader.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.juguito.juguitoreader.ui.book.add.AddBookScreen
import com.juguito.juguitoreader.ui.book.detail.BookDetailScreen
import com.juguito.juguitoreader.ui.folder.add.AddFolderScreen
import com.juguito.juguitoreader.ui.home.HomeScreen
import com.juguito.juguitoreader.ui.registry.RegistryScreen
import com.juguito.juguitoreader.ui.theme.AppTheme
import com.juguito.juguitoreader.ui.theme.ThemeViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JuguitoApp(
    themeViewModel: ThemeViewModel = hiltViewModel()
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    val currentTheme by themeViewModel.currentTheme.collectAsState()

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.fillMaxWidth(0.75f),
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                drawerShape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
            ) {
                DrawerHeader()
                
                Spacer(modifier = Modifier.height(12.dp))

                DrawerItem(
                    label = "Inicio",
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
                    label = "Registro",
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
                    label = "Biblioteca",
                    icon = Icons.Default.AutoStories,
                    onClick = { scope.launch { drawerState.close() } }
                )

                DrawerItem(
                    label = "Estadísticas",
                    icon = Icons.Default.BarChart,
                    onClick = { scope.launch { drawerState.close() } }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp))

                Text(
                    text = "Gestión",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 28.dp, bottom = 8.dp)
                )

                DrawerItem(
                    label = "Añadir Carpeta",
                    icon = Icons.Default.CreateNewFolder,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            navController.navigate("add_folder")
                        }
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp))

                Text(
                    text = "Apariencia",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 28.dp, bottom = 8.dp)
                )

                Row(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemeOption(
                        label = "Juguito",
                        selected = currentTheme == AppTheme.JUGUITO,
                        onClick = { themeViewModel.setTheme(AppTheme.JUGUITO) },
                        modifier = Modifier.weight(1f)
                    )
                    ThemeOption(
                        label = "Neón",
                        selected = currentTheme == AppTheme.NEON,
                        onClick = { themeViewModel.setTheme(AppTheme.NEON) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                DrawerItem(
                    label = "Ajustes",
                    icon = Icons.Default.Settings,
                    onClick = { scope.launch { drawerState.close() } }
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
            ) {
                composable(route = "home") {
                    HomeScreen(
                        onOpenDrawer = { scope.launch { drawerState.open() } },
                        onNavigateToAddBook = {
                            navController.navigate("add_book")
                        },
                        onNavigateToBookDetail = { bookId ->
                            navController.navigate("book_detail/$bookId")
                        }
                    )
                }

                composable(route = "add_book") {
                    AddBookScreen(
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }

                composable(route = "add_folder") {
                    AddFolderScreen(
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }

                composable(
                    route = "book_detail/{bookId}",
                    arguments = listOf(navArgument("bookId") { type = NavType.IntType })
                ) {
                    BookDetailScreen(
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }

                composable (route = "registry") {
                    RegistryScreen(
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onNavigateToAddBook = {
                            navController.navigate("add_book")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ThemeOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    InputChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = InputChipDefaults.inputChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
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
                text = "JuguitoReader",
                style = MaterialTheme.typography.headlineMedium,
                color = colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Tu biblioteca personal",
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
