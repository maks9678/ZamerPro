package com.example.zamerpro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.zamerpro.home.HOUSE_SCREEN_ROUTE
import com.example.zamerpro.home.HouseScreen
import com.example.zamerpro.homes.HOUSES_LIST_SCREEN_ROUTE
import com.example.zamerpro.room.ROOM_INPUT_ROUTE
import com.example.zamerpro.room.RoomInputScreen
import com.example.zamerpro.ui.theme.ZamerProTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ZamerProTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = HOUSES_LIST_SCREEN_ROUTE) {
                        composable(HOUSES_LIST_SCREEN_ROUTE) {
                            HouseScreen(navController = navController, houseId = "")
                        }
                        composable(
                            route = "$HOUSE_SCREEN_ROUTE/{houseId}", // Маршрут с аргументом
                            arguments = listOf(navArgument("houseId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val houseId = backStackEntry.arguments?.getString("houseId")
                            // Передаем houseId в HouseScreen (он будет его передавать в ViewModel)
                            if (houseId != null) {
                                HouseScreen(navController = navController, houseId = houseId)
                            } else {
                                // Обработка случая, если houseId не передан (например, вернуться назад)
                                navController.popBackStack()
                            }
                        }
                        composable(route = "$ROOM_INPUT_ROUTE/{houseId}", // Добавляем houseId
                            arguments = listOf(navArgument("houseId") { type =
                                NavType.StringType })
                        ) { backStackEntry ->
                            val houseId = backStackEntry.arguments?.getString("houseId")
                            if (houseId != null) {
                                RoomInputScreen(navController = navController, houseId = houseId)
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