package com.example.zamerpro.Main

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
import com.example.zamerpro.home.HOUSE_SCREEN_ROUTE
import com.example.zamerpro.home.HouseDetailsScreen
import com.example.zamerpro.homes.HousesListScreen
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
                    val rootNavController = rememberNavController()

                    NavHost(
                        navController = rootNavController,
                        startDestination = "houses_list" // Стартуем со списка всех объектов
                    ) {
                        // 1. Экран со списком ВСЕХ домов (без нижней панели)
                        composable(route = "houses_list") {
                            HousesListScreen(
                                navController = rootNavController,
                                // modifier теперь не нужен, так как нет Scaffold'а-обертки
                            )
                        }

                        // 2. Экран КОНКРЕТНОГО дома (который содержит вложенную навигацию)
                        composable(
                            route = "$HOUSE_SCREEN_ROUTE/{houseId}",
                            arguments = listOf(navArgument("houseId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val houseId = backStackEntry.arguments?.getString("houseId")
                            if (houseId != null) {
                                // Этот экран теперь является контейнером с собственной нижней панелью
                                HouseDetailsScreen(
                                    rootNavController = rootNavController, // Для возврата назад или перехода на другие глобальные экраны
                                    houseId = houseId
                                )
                            }
                        }

                        // 3. Экран для создания/редактирования комнаты (без нижней панели)
                        composable(
                            route = "$ROOM_INPUT_ROUTE/{houseId}?roomId={roomId}",
                            arguments = listOf(
                                navArgument("houseId") { type = NavType.StringType },
                                navArgument("roomId") {
                                    type = NavType.IntType
                                    defaultValue = -1
                                }
                            )
                        ) { backStackEntry ->
                            val houseId = backStackEntry.arguments?.getString("houseId")
                            val roomId = backStackEntry.arguments?.getInt("roomId")

                            if (houseId != null) {
                                RoomInputScreen(
                                    navController = rootNavController, // Используем главный контроллер
                                    houseId = houseId,
                                    roomId = if (roomId == -1) null else roomId,
                                )
                            }
                        }
                    }
                }
            }
        }

    }
}

