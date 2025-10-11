package com.example.zamerpro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.zamerpro.MaterialsList.MATERIALS_LIST_SCREEN_ROUTE
import com.example.zamerpro.MaterialsList.MaterialsListScreen
import com.example.zamerpro.home.HOUSE_SCREEN_ROUTE
import com.example.zamerpro.home.HouseScreen
import com.example.zamerpro.homes.HOUSES_LIST_SCREEN_ROUTE
import com.example.zamerpro.homes.HousesListScreen
import com.example.zamerpro.materials.MaterialsScreen
import com.example.zamerpro.room.ROOM_INPUT_ROUTE
import com.example.zamerpro.room.RoomInputScreen
import com.example.zamerpro.ui.theme.ZamerProTheme
const val MAIN_SCREEN_ROUTE = "main_screen"


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ZamerProTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Главный NavController для всего приложения
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = BottomNavItem.Houses.route // Новая стартовая точка
                    ) {
                        // 1. Главный экран с нижней панелью
                        composable(BottomNavItem.Houses.route) {
                            // Оборачиваем HousesListScreen в MainScreen
                            MainScreen(navController = navController) { modifier ->
                                HousesListScreen(
                                    navController = navController,
                                    modifier = modifier // Передаем отступы
                                )
                            }
                        }

                        composable(BottomNavItem.Materials.route) {
                            // Оборачиваем MaterialsListScreen в MainScreen
                            MainScreen(navController = navController) { modifier ->
                                MaterialsListScreen(
                                    navController = navController,
                                    modifier = modifier // Передаем отступы
                                )
                            }
                        }


                        // 2. Экран одного дома (куда мы переходим из списка домов)
                        composable(
                            route = "$HOUSE_SCREEN_ROUTE/{houseId}",
                            arguments = listOf(navArgument("houseId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val houseId = backStackEntry.arguments?.getString("houseId")
                            if (houseId != null) {
                                HouseScreen(navController = navController, houseId = houseId)
                            } else {
                                // Если ID дома не передан, безопасно возвращаемся назад
                                navController.popBackStack()
                            }
                        }

                        // 3. Экран для создания/редактирования комнаты
                        composable(
                            // Используем один маршрут для создания и редактирования.
                            // roomId - необязательный параметр.
                            route = "$ROOM_INPUT_ROUTE/{houseId}?roomId={roomId}",
                            arguments = listOf(
                                navArgument("houseId") { type = NavType.StringType },
                                navArgument("roomId") {
                                    type =
                                        NavType.StringType // ID комнат тоже могут быть String (UUID)
                                    nullable =
                                        true          // Указываем, что он может отсутствовать
                                    defaultValue = null
                                }
                            )
                        ) { backStackEntry ->
                            val houseId = backStackEntry.arguments?.getString("houseId")
                            val roomId = backStackEntry.arguments?.getString("roomId")

                            if (houseId != null) {
                                RoomInputScreen(
                                    navController = navController,
                                    houseId = houseId,
                                    roomId = roomId?.toIntOrNull(),
                                )
                            } else {
                                navController.popBackStack()
                            }
                        }

                        // 4. Экран со списком материалов для ОДНОГО дома (заменил ваш HOUSES_LIST_SCREEN_ROUTE)
                        // У него должен быть свой уникальный маршрут
                        composable(
                            route = "$MATERIALS_LIST_SCREEN_ROUTE/{houseId}",
                            arguments = listOf(navArgument("houseId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val houseId = backStackEntry.arguments?.getString("houseId")
                            if (houseId != null) {
                                // ЭТОТ ЭКРАН ВЫ НАЗВАЛИ MaterialsListScreen, НО, СКОРЕЕ ВСЕГО, ОН - MaterialsScreen.
                                // Если у вас действительно два экрана для материалов, убедитесь, что вызываете нужный.
                                // Судя по названию, он показывает материалы для конкретного дома.
                                MaterialsScreen(
                                    navController = navController,
                                    houseId = houseId
                                )
                            } else {
                                navController.popBackStack()
                            }
                        }
                    }
                }
            }
        }
    }
}

