package com.example.zamerpro.room

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.zamerpro.ItemDimension
import kotlin.collections.forEachIndexed

const val ROOM_INPUT_ROUTE = "roomInput"

// Ключ для возврата результата
const val NEW_ROOM_RESULT_KEY = "new_room_details"

@Preview(showBackground = true, name = "RoomInputScreen (Простое Превью)")
@Composable
fun RoomInputScreenSimplePreview() {
    MaterialTheme {


        val previewHouseId = "preview_house_123"
        val viewModelForPreview: RoomViewModel = viewModel(
            factory = RoomViewModelFactory(previewHouseId)
        )

        viewModelForPreview.updateRoomName("Гостиная (Превью)")
        viewModelForPreview.updateRoomHeight("2.75")
        viewModelForPreview.updateRoomWidth("4.8")
        viewModelForPreview.updateRoomLength("5.2")

        RoomInputScreen(
            houseId = previewHouseId, // Передаем houseId, как и в реальном вызове
            viewModel = viewModelForPreview, // Передаем настроенный ViewModel
            navController = rememberNavController() // Моковый NavController
        )
    }
}

@Preview(showBackground = true, name = "RoomInputScreen (Пустое Простое Превью)")
@Composable
fun RoomInputScreenEmptySimplePreview() {
    MaterialTheme {
        val previewHouseId = "preview_empty_house_123"
        val viewModelForPreview: RoomViewModel = viewModel(
            factory = RoomViewModelFactory(previewHouseId)
        )

        RoomInputScreen(
            houseId = previewHouseId,
            viewModel = viewModelForPreview,
            navController = rememberNavController()
        )
    }
}

class RoomViewModelFactory(private val houseId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RoomViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RoomViewModel(houseId) as T // Передаем houseId в конструктор
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomInputScreen(
    modifier: Modifier = Modifier,
    houseId: String,
    viewModel: RoomViewModel = viewModel(factory = RoomViewModelFactory(houseId)),
    navController: NavController,
) {
    val roomName by viewModel.roomName.collectAsState()
    val roomHeight by viewModel.roomHeight.collectAsState()
    val roomWidth by viewModel.roomWidth.collectAsState()
    val roomLength by viewModel.roomLength.collectAsState()
    val doors by viewModel.doors.collectAsState()
    val windows by viewModel.windows.collectAsState()
    val customWalls by viewModel.customWalls.collectAsState()

    Scaffold(
        bottomBar = {
            Spacer(modifier = Modifier.height(8.dp)) // Отступ перед кнопкой
            Button(
                onClick = {
                    val simpleRoomNoHouseId =
                        viewModel.calculateAndGetSimpleRoom() // Не содержит houseId
                    if (simpleRoomNoHouseId != null) {
                        // Передаем объект без houseId. HouseViewModel сам его подставит.
                        navController.previousBackStackEntry?.savedStateHandle?.set(
                            NEW_ROOM_RESULT_KEY,
                            simpleRoomNoHouseId
                        )
                        viewModel.resetAllFields()
                        navController.popBackStack()
                        println("LOG_ROOM_INPUT: Room to be saved (without houseId yet): $simpleRoomNoHouseId")
                    } else {
                        println("LOG_ROOM_INPUT: Validation failed for room save.")
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(start= 16.dp,bottom = 16.dp, end = 16.dp ),
            ) {
                Icon(Icons.Filled.Done, contentDescription = "Сохранить комнату")
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(
                    "Сохранить и добавить в дом"
                )
            }
        })
    { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues) // Применяем padding от Scaffold
                .background(color = MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                OutlinedTextField(
                    value = roomName,
                    onValueChange = { viewModel.updateRoomName(it) },
                    label = { Text("Название комнаты") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = roomName.isBlank() // Пример простой валидации на UI
                )
            }

            // Карточка для основных параметров комнаты
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(0.95f), // Сделал чуть шире для соответствия HouseScreen
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Параметры комнаты",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            Arrangement.spacedBy(8.dp),
                            Alignment.CenterVertically
                        ) {
                            DimensionTextField(
                                label = "Длина (м)",
                                value = roomLength,
                                modifier = Modifier.weight(1f),
                                onValueChange = { viewModel.updateRoomLength(it) },
                                isError = roomLength.toDoubleOrNull() == null && roomLength.isNotBlank() // Пример валидации
                            )
                            Spacer(modifier = Modifier)
                            DimensionTextField(
                                label = "Ширина (м)",
                                value = roomWidth,
                                modifier = Modifier.weight(1f),
                                onValueChange = { viewModel.updateRoomWidth(it) },
                                isError = roomWidth.toDoubleOrNull() == null && roomWidth.isNotBlank() // Пример валидации
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            DimensionTextField(
                                label = "Высота (м)",
                                value = roomHeight,
                                modifier = Modifier.weight(1f),
                                onValueChange = { viewModel.updateRoomHeight(it) })

                        }
                    }
                }
            }

            // Секции для Дверей, Окон, Стен (без изменений в их структуре)
            item {
                DimensionListSection(
                    title = "Размеры дверей", items = doors,
                    onAddItem = { viewModel.addDoor() },
                    onRemoveItem = { item -> viewModel.removeDoor(item) },
                    onItemWidthChange = { index, newWidth ->
                        viewModel.updateDoorWidth(
                            index,
                            newWidth
                        )
                    },
                    onItemHeightChange = { index, newHeight ->
                        viewModel.updateDoorHeight(
                            index,
                            newHeight
                        )
                    }
                )
            }
            item {
                DimensionListSection(
                    title = "Размеры окон", items = windows,
                    onAddItem = { viewModel.addWindow() },
                    onRemoveItem = { item -> viewModel.removeWindow(item) }, // Исправлено на removeWindow
                    onItemWidthChange = { index, newWidth ->
                        viewModel.updateWindowWidth(
                            index,
                            newWidth
                        )
                    }, // Исправлено
                    onItemHeightChange = { index, newHeight ->
                        viewModel.updateWindowHeight(
                            index,
                            newHeight
                        )
                    } // Исправлено
                )
            }
            item {
                DimensionListSection(
                    title = "Дополнительные стены", items = customWalls,
                    onAddItem = { viewModel.addCustomWall() },
                    onRemoveItem = { item -> viewModel.removeCustomWall(item) },
                    onItemWidthChange = { index, newWidth ->
                        viewModel.updateCustomWallWidth(
                            index,
                            newWidth
                        )
                    },
                    onItemHeightChange = { index, newHeight ->
                        viewModel.updateCustomWallHeight(
                            index,
                            newHeight
                        )
                    }
                )
            }

            // Кнопка Сохранить в конце списка
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DimensionTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false // Добавлен параметр isError
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        isError = isError // Передаем isError в OutlinedTextField
    )
}

@Composable
fun DimensionListSection(
    title: String,
    items: List<ItemDimension>, // Теперь просто List, т.к. управление через ViewModel
    onAddItem: () -> Unit,
    onRemoveItem: (ItemDimension) -> Unit, // Изменено для передачи всего объекта
    onItemWidthChange: (Int, String) -> Unit, // Новые колбэки
    onItemHeightChange: (Int, String) -> Unit  // Новые колбэки
) {
    Card(
        modifier = Modifier.fillMaxWidth(0.95f),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )

            items.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DimensionTextField( // DimensionTextField теперь принимает isError
                        label = "Ширина (м)",
                        value = item.width,
                        onValueChange = { newWidth ->
                            onItemWidthChange(index, newWidth)
                        },
                        modifier = Modifier.weight(1f),
                        isError = item.width.toDoubleOrNull() == null && item.width.isNotBlank() // Пример валидации
                    )
                    DimensionTextField( // DimensionTextField теперь принимает isError
                        label = "Высота (м)",
                        value = item.height,
                        onValueChange = { newHeight ->
                            onItemHeightChange(index, newHeight)
                        },
                        modifier = Modifier.weight(1f),
                        isError = item.height.toDoubleOrNull() == null && item.height.isNotBlank() // Пример валидации
                    )
                    IconButton(onClick = { onRemoveItem(item) }) { // Передаем объект для удаления
                        Icon(Icons.Filled.Delete, contentDescription = "Удалить")
                    }
                }
                if (index < items.size - 1) {
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
            Button(onClick = onAddItem, modifier = Modifier.fillMaxWidth()) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "Добавить",
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Добавить")
            }
        }
    }
}