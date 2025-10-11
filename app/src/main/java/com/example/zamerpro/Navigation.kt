package com.example.zamerpro

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Запечатанный класс для описания элементов нижней навигации.
 * @param route Уникальный маршрут для NavController.
 * @param title Название для отображения на панели.
 * @param icon Иконка для отображения на панели.
 */
sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Houses : BottomNavItem("houses_list", "Объекты", Icons.Default.Home)
    object Materials : BottomNavItem("materials_list", "Материалы",
        Icons.Default.List)
}