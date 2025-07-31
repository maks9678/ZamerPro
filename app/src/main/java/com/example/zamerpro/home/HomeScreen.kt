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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.zamerpro.room.NEW_ROOM_RESULT_KEY
import com.example.zamerpro.room.ROOM_INPUT_ROUTE

const val HOUSE_SCREEN_ROUTE = "houseScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HouseScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: HouseViewModel = viewModel(), // Получаем экземпляр ViewModel
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
    val newRoomResult = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getLiveData<SimpleRoom>(NEW_ROOM_RESULT_KEY)?.observeAsState()

    LaunchedEffect(newRoomResult?.value) {
        newRoomResult?.value?.let { room ->
            viewModel.addRoom(room)
            // Очищаем результат, чтобы он не добавлялся повторно при recomposition или возврате на экран
            navController.currentBackStackEntry?.savedStateHandle?.remove<SimpleRoom>(NEW_ROOM_RESULT_KEY)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Мой Дом") })
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Button(
                    onClick = {
                        navController.navigate(ROOM_INPUT_ROUTE) // Навигация на RoomInputScreen
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
}
@Preview(showBackground = true)
@Composable
fun HouseScreenPreview() {
    MaterialTheme {
        HouseScreen(navController = rememberNavController(), viewModel = viewModel())
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