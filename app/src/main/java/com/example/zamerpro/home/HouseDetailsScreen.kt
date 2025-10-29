package com.example.zamerpro.home

import android.app.Application
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.zamerpro.Price.PRICE_SCREEN_ROUTE
import com.example.zamerpro.Price.Price
import com.example.zamerpro.Price.PriceScreen
import com.example.zamerpro.materials.MATERIAL_SCREEN_ROUTE
import com.example.zamerpro.materials.MaterialsScreen
import com.example.zamerpro.room.ROOM_INPUT_ROUTE

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HouseDetailsScreen(
    rootNavController: NavController, // Контроллер верхнего уровня для возврата назад
    houseId: String
) {
    // ViewModel, которая будет общей для всех экранов внутри этого дома
    // `key` гарантирует, что ViewModel будет жить, пока мы на экране этого дома
    val houseViewModel: HouseViewModel = viewModel(
        key = "house_$houseId",
        factory = HouseViewModel.HouseViewModelFactory(
            application = LocalContext.current.applicationContext as Application,
            houseId = houseId
        )
    )
    val currentHouse by houseViewModel.currentHouse.collectAsState()

    // Создаем НОВЫЙ NavController для вложенной навигации
    val detailsNavController = rememberNavController()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentHouse?.name ?: "Загрузка...") },
                modifier = Modifier.fillMaxWidth(),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                // Добавляем кнопку "Назад"
                navigationIcon = {
                    IconButton(onClick = { rootNavController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        },
        // Используем нашу новую контекстную нижнюю панель
        bottomBar = { HouseDetailsBottomBar(navController = detailsNavController) }
    ) { innerPadding ->
        // Вложенный NavHost для переключения между "Комнатами" и "Материалами"
        NavHost(
            navController = detailsNavController,
            // Стартовый экран - список комнат
            startDestination = HouseDetailsNavItem.Rooms.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Экран со списком комнат (ваш бывший HouseScreen)
            composable(HouseDetailsNavItem.Rooms.route) {
                // Передаем rootNavController для перехода на экран создания комнаты
                HouseScreen(
                    navController = rootNavController,
                    houseId = houseId,
                )
            }

            // Экран со списком материалов для этого дома
            composable(HouseDetailsNavItem.Materials.route) {
                // Передаем rootNavController для возможных переходов
                MaterialsScreen(
                    navController = rootNavController,
                    houseId = houseId
                )
            }
            composable(HouseDetailsNavItem.Price.route) {
                PriceScreen(
                    rootNavController,
                    houseId
                )
            }
        }
    }
}

@Composable
fun HouseDetailsBottomBar(navController: NavController) {
    val items = listOf(
        HouseDetailsNavItem.Rooms,
        HouseDetailsNavItem.Materials,
        HouseDetailsNavItem.Price,
    )

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        items.forEach { screen ->
            NavigationBarItem(
                // Проверяем, активен ли текущий маршрут
                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                onClick = {
                    navController.navigate(screen.route) {
                        // Эта логика гарантирует, что у нас не будет большой стопки экранов в нижней навигации
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

sealed class HouseDetailsNavItem(val route: String, val title: String, val icon: ImageVector) {
    // Маршрут для списка комнат конкретного дома
    // Мы добавим houseId к этому маршруту при навигации
    object Rooms : HouseDetailsNavItem(
        route = ROOM_INPUT_ROUTE,
        title = "Комнаты",
        icon = Icons.Default.Architecture
    )

    // Маршрут для списка материалов конкретного дома
    object Materials : HouseDetailsNavItem(
        route = MATERIAL_SCREEN_ROUTE,
        title = "Материалы",
        icon = Icons.Default.Construction
    )

    object Price : HouseDetailsNavItem(
        route = PRICE_SCREEN_ROUTE,
        title = "Стоимость работ",
        icon = Icons.Default.AttachMoney
    )
}