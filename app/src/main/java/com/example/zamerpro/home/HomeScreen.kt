package com.example.zamerpro.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

import androidx.lifecycle.viewmodel.compose.viewModel


@Preview(showBackground = true)
@Composable
fun HouseScreenPreview() {
    MaterialTheme {
        HouseScreen(viewModel = viewModel()) // Используем viewModel() для превью
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HouseScreen(
    modifier: Modifier = Modifier,
    viewModel: HouseViewModel = viewModel() // Получаем экземпляр ViewModel
) {
    // Собираем состояния из ViewModel
    // Для лучшей практики с жизненным циклом используйте collectAsStateWithLifecycle
    // import androidx.lifecycle.compose.collectAsStateWithLifecycle
    // val newRoomName by viewModel.newRoomName.collectAsStateWithLifecycle()
    // val newRoomAreaString by viewModel.newRoomAreaString.collectAsStateWithLifecycle()
    // val roomsInHouse by viewModel.roomsInHouse.collectAsStateWithLifecycle()
    // val totalArea by viewModel.totalArea.collectAsStateWithLifecycle()

    val roomsInHouse by viewModel.roomsInHouse.collectAsState()
    val totalArea by viewModel.totalArea.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Секция для добавления новой комнаты
        item {
            Button(
                onClick = {
                    // Здесь должна быть навигация на RoomInputScreen
                    // navController.navigate("route_to_room_input_screen")
                    // Например: navController.navigate("roomInput")
                },
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .padding(vertical = 8.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Добавить новую комнату")
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Добавить новую комнату")
            }
        }

        // Секция для отображения общей площади (без изменений)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(0.95f),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Общая площадь дома:",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "${String.format("%.2f", totalArea)} м²",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        if (roomsInHouse.isNotEmpty()) {
            item {
                Text(
                    text = "Список комнат:",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }
        }

        items(roomsInHouse, key = { room -> room.id }) { room ->
            RoomInHouseItem(
                room = room,
                onRemoveClick = { viewModel.removeRoom(room) }
            )
        }
    }
}

@Composable
fun RoomInHouseItem(
    room: SimpleRoom,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth(0.95f)
            .padding(vertical = 4.dp), // Небольшой отступ между карточками комнат
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = room.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "Площадь: ${String.format("%.2f", room.area)} м²",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray // MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onRemoveClick) {
                Icon(Icons.Filled.Delete, contentDescription = "Удалить комнату", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}