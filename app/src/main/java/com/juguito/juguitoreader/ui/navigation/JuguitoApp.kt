package com.juguito.juguitoreader.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.juguito.juguitoreader.ui.book.add.AddBookScreen
import com.juguito.juguitoreader.ui.folder.add.AddFolderScreen
import com.juguito.juguitoreader.ui.home.HomeScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JuguitoApp() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.fillMaxWidth(0.7f)
            ) {
                Text(
                    text = "JuguitoReader",
                    modifier = Modifier.padding(16.dp),
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                HorizontalDivider()

                NavigationDrawerItem(
                    label = { Text("Inicio") },
                    selected = true,
                    onClick = { scope.launch { drawerState.close() } },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )

                NavigationDrawerItem(
                    label = { Text("Biblioteca") },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() } },
                    icon = { Icon(Icons.Default.Book, contentDescription = null) },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )

                HorizontalDivider()

                NavigationDrawerItem(
                    label = { Text("+ Añadir Carpeta") },
                    selected = false,
                    onClick = { scope.launch {
                        drawerState.close()
                        navController.navigate("add_folder")
                    } },
                    icon = { Icon(Icons.Default.Folder, contentDescription = null) },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
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
            }

        }
    }
}