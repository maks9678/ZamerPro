package com.example.zamerpro.home

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.zamerpro.Class.House
import com.example.zamerpro.Class.Room
import com.example.zamerpro.room.NEW_ROOM_RESULT_KEY
import com.example.zamerpro.room.ROOM_INPUT_ROUTE
import com.example.zamerpro.room.UPDATED_ROOM_RESULT_KEY

const val HOUSE_SCREEN_ROUTE = "houseScreen"

val previewsRoom = listOf(Room(
        name = "Test",
width = 4.0,
length = 5.0,
height = 2.5,
floorArea = 20.0,
wallArea = 45.0,
windowMetre = 0.0,
houseId = "preview_house"
) )
val previewHouse = House(id = "preview_house_id_123", name = "Дом для Превью")

@Preview(showBackground = true, name = "HouseScreen с данными")
@Composable
fun HouseScreenWithDataPreview() {
    MaterialTheme {
        // Упрощенная версия HouseScreen или версия, принимающая данные напрямую
        // Вместо того чтобы создавать полноценный ViewModel, который лезет в БД,
        // мы можем передать данные напрямую в Composable для превью.
        // Для этого нужно будет немного изменить HouseScreen, чтобы он мог принимать
        // список комнат и дом как параметры (с дефолтными значениями, получаемыми из ViewModel).

        // Вариант А: Модифицировать HouseScreen (показан ниже)
        HouseScreenInternal( // Назовем внутренний Composable иначе
            navController = rememberNavController(),
            currentHouse = previewHouse,
            roomsInHouse = previewsRoom,
            totalArea = previewsRoom.sumOf { it.wallArea }.toInt(),
            totalMetre = previewsRoom.sumOf { it.windowMetre}.toInt(),
            onAddRoomClicked = {},
            onRemoveRoomClicked = {},
            onEditRoomClicked = {}
        )
    }
}


@Composable
fun HouseScreen(
    navController: NavController,
    houseId: String,
) {
    val application = LocalContext.current.applicationContext as Application
    val viewModel: HouseViewModel = viewModel(
        factory = HouseViewModel.HouseViewModelFactory(application, houseId)
    )

    val currentHouse by viewModel.currentHouse.collectAsState()
    val roomsInHouse by viewModel.roomsInHouse.collectAsState()


    val newRoomResult = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getLiveData<Room>(NEW_ROOM_RESULT_KEY)?.observeAsState()

    LaunchedEffect(newRoomResult?.value) {
        newRoomResult?.value?.let { room ->
            viewModel.addRoom(room) // room здесь без houseId
            navController.currentBackStackEntry?.savedStateHandle?.remove<Room>(
                NEW_ROOM_RESULT_KEY
            )
        }
    }
    val updatedRoomResult = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getLiveData<Room>(UPDATED_ROOM_RESULT_KEY)?.observeAsState() // Используем новый ключ

    LaunchedEffect(updatedRoomResult?.value) {
        updatedRoomResult?.value?.let { room ->
            viewModel.updateRoom(room) // Предполагаем, что в ViewModel будет такой метод
            navController.currentBackStackEntry?.savedStateHandle?.remove<Room>(UPDATED_ROOM_RESULT_KEY)
        }
    }

    HouseScreenInternal(
        navController = navController,
        currentHouse = currentHouse,
        roomsInHouse = roomsInHouse,
        totalArea = currentHouse?.totalWallArea ?: 0,
        totalMetre = currentHouse?.totalWindowMetre ?: 0,
        onAddRoomClicked = {
            navController.navigate("$ROOM_INPUT_ROUTE/$houseId") // Передаем houseId
        },
        onRemoveRoomClicked = { room ->
            viewModel.removeRoom(room)
        },
        onEditRoomClicked = { room ->
            navController.navigate("$ROOM_INPUT_ROUTE/${room.houseId}?roomId=${room.id}")
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HouseScreenInternal(
    navController: NavController, // NavController все еще может быть нужен для навигации с кнопок
    currentHouse: House?,
    roomsInHouse: List<Room>,
    totalArea: Int,
    totalMetre: Int,
    onAddRoomClicked: () -> Unit,
    onRemoveRoomClicked: (Room) -> Unit,
    onEditRoomClicked: (Room) -> Unit,
    modifier: Modifier = Modifier // Добавим modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentHouse?.name ?: "Мой Дом") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,

                    )
            )
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(0.95f),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Площадь стен:",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = "$totalArea м²",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Метраж откосов:",
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Text(
                                    text = "$totalMetre м",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
            // Кнопка "Добавить новую комнату"
            item {
                Button(
                    onClick = onAddRoomClicked, // Используем коллбэк
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .padding(vertical = 4.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Добавить новую комнату")
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Добавить новую комнату")
                }
            }
            if (roomsInHouse.isNotEmpty()) {
                item {
                    Text(
                        text = "Список комнат:",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                items(roomsInHouse, key = { room -> room.id }) { roomData ->
                    RoomInHouseItem(
                        room = roomData,
                        onRemoveClick = { onRemoveRoomClicked(roomData) },
                        onItemClick = { onEditRoomClicked(roomData) }
                    )
                }
            } else {
                item {
                    Text(
                        text = "Пока нет добавленных комнат.",
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun RoomInHouseItem(
    room: Room,
    onRemoveClick: () -> Unit,
    onItemClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth(0.95f)
            .padding(vertical = 4.dp)
            .clickable { onItemClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 11.dp, vertical = 12.dp), // Внутренние отступы в карточке
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween // Размещает имя/площадь слева, кнопку справа
        ) {
            Column(modifier = Modifier.weight(1f)) { // Позволяет тексту занимать доступное пространство
                Text(
                    text = room.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Площадь стен: ${room.wallArea} м²",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant // Используйте цвета из темы
                )
                Text(
                    text = "Метраж : ${room.windowMetre} м",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onRemoveClick) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Удалить комнату ${room.name}", // Более описательный contentDescription
                    tint = MaterialTheme.colorScheme.error // Цвет ошибки для кнопки удаления
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "HouseScreen пустой")
@Composable
fun HouseScreenEmptyPreview() {
    MaterialTheme {
        HouseScreenInternal(
            navController = rememberNavController(),
            currentHouse = previewHouse.copy(name = "Пустой Дом (Превью)"),
            roomsInHouse = emptyList(),
            totalArea = 0,
            totalMetre = 0,
            onAddRoomClicked = {},
            onRemoveRoomClicked = {},
            onEditRoomClicked = {}
        )
    }
}
