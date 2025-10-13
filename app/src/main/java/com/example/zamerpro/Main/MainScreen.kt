package com.example.zamerpro.Main

import android.annotation.SuppressLint
import android.app.Application
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.zamerpro.BottomNavItem
import com.example.zamerpro.homes.AppViewModelProvider
import com.example.zamerpro.homes.HousesListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainScreen(
    navController: NavController,
    content: @Composable (modifier: Modifier) -> Unit
) {
    val housesViewModel: HousesListViewModel = viewModel(factory = AppViewModelProvider(LocalContext.current.applicationContext as Application))

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        topBar = {
            // Показываем TopAppBar в зависимости от текущего экрана
            if (currentDestination?.route == BottomNavItem.Houses.route) {
                TopAppBar(
                    title = { Text("Мои Объекты") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    )
                )
            }
            // Здесь можно добавить TopAppBar для других экранов
            // else if (currentDestination?.route == BottomNavItem.Materials.route) { ... }
        },
        bottomBar = { BottomNavigationBar(navController = navController) },
        floatingActionButton = {
            // Показываем FAB только на экране "Объекты"
            if (currentDestination?.route == BottomNavItem.Houses.route) {
                FloatingActionButton(onClick = {
                    // Вызываем метод из ViewModel для показа диалога
                    housesViewModel.onShowDialogChange(true)
                }) {
                    Icon(Icons.Filled.Add, contentDescription = "Создать новый объект")
                }
            }
        }
    ) { innerPadding ->
        // Отображаем переданный контент с правильными отступами
        content(Modifier.padding(innerPadding))
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem.Houses,
        BottomNavItem.Materials
    )

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        items.forEach { screen ->
            NavigationBarItem(
                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(screen.icon, contentDescription = screen.title) },
                label = { Text(screen.title) }
            )
        }
    }
}