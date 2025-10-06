package com.example.zamerpro.room

import android.app.Application
import androidx.activity.result.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.zamerpro.HomeDao.AppDatabase
import com.example.zamerpro.ItemDimension
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.collections.forEachIndexed

const val ROOM_INPUT_ROUTE = "roomInput"
const val ROOM_EDIT_ROUTE = "roomEdit"
const val NEW_ROOM_RESULT_KEY = "new_room_details"
const val UPDATED_ROOM_RESULT_KEY = "updated_room_details"
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomInputScreenInternal(
    modifier: Modifier = Modifier,
    roomName: String,
    roomHeight: String,
    roomWidth: String,
    roomLength: String,
    doors: List<ItemDimension>,
    windows: List<ItemDimension>,
    customWalls: List<ItemDimension>,
    onRoomNameChange: (String) -> Unit,
    onRoomHeightChange: (String) -> Unit,
    onRoomWidthChange: (String) -> Unit,
    onRoomLengthChange: (String) -> Unit,
    onAddDoor: () -> Unit,
    onRemoveDoor: (ItemDimension) -> Unit,
    onDoorWidthChange: (Int, String) -> Unit,
    onDoorHeightChange: (Int, String) -> Unit,
    onAddWindow: () -> Unit,
    onRemoveWindow: (ItemDimension) -> Unit,
    onWindowWidthChange: (Int, String) -> Unit,
    onWindowHeightChange: (Int, String) -> Unit,
    onAddCustomWall: () -> Unit,
    onRemoveCustomWall: (ItemDimension) -> Unit,
    onCustomWallWidthChange: (Int, String) -> Unit,
    onCustomWallHeightChange: (Int, String) -> Unit,
    onSaveClick: () -> Unit
) {
    Scaffold(
        bottomBar = {
            Button(
                onClick = onSaveClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                Icon(Icons.Filled.Done, contentDescription = "Сохранить комнату")
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Сохранить и добавить в дом")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(color = MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(vertical = 8.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                OutlinedTextField(
                    value = roomName,
                    onValueChange = onRoomNameChange,
                    label = { Text("Название комнаты") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = roomName.isBlank()
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Параметры комнаты",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            DimensionTextField(
                                label = "Длина (м)",
                                value = roomLength,
                                modifier = Modifier.weight(1f),
                                onValueChange = onRoomLengthChange, // Используем коллбэк
                                isError = roomLength.toDoubleOrNull() == null && roomLength.isNotBlank()
                            )
                            DimensionTextField(
                                label = "Ширина (м)",
                                value = roomWidth,
                                modifier = Modifier.weight(1f),
                                onValueChange = onRoomWidthChange, // Используем коллбэк
                                isError = roomWidth.toDoubleOrNull() == null && roomWidth.isNotBlank()
                            )
                            DimensionTextField(
                                label = "Высота (м)",
                                value = roomHeight,
                                modifier = Modifier.weight(1f),
                                onValueChange = onRoomHeightChange
                            )
                        }
                    }
                }
            }

            item {
                DimensionListSection(
                    title = "Размеры дверей", items = doors,
                    onAddItem = onAddDoor,
                    onRemoveItem = onRemoveDoor,
                    onItemWidthChange = onDoorWidthChange,
                    onItemHeightChange = onDoorHeightChange
                )
            }
            item {
                DimensionListSection(
                    title = "Размеры окон", items = windows,
                    onAddItem = onAddWindow,
                    onRemoveItem = onRemoveWindow,
                    onItemWidthChange = onWindowWidthChange,
                    onItemHeightChange = onWindowHeightChange
                )
            }
            item {
                DimensionListSection(
                    title = "Дополнительные стены", items = customWalls,
                    onAddItem = onAddCustomWall,
                    onRemoveItem = onRemoveCustomWall,
                    onItemWidthChange = onCustomWallWidthChange,
                    onItemHeightChange = onCustomWallHeightChange
                )
            }
        }
    }
}
@Preview(showBackground = true, name = "RoomInputScreen (Пустое)")
@Composable
fun RoomInputScreenEmptyPreview() {
    MaterialTheme {
        RoomInputScreenInternal(
            roomName = "",
            roomHeight = "",
            roomWidth = "",
            roomLength = "",
            doors = listOf(ItemDimension()),
            windows = listOf(ItemDimension()),
            customWalls = listOf(ItemDimension()),
            onRoomNameChange = {},
            onRoomHeightChange = {},
            onRoomWidthChange = {},
            onRoomLengthChange = {},
            onAddDoor = {},
            onRemoveDoor = {},
            onDoorWidthChange = { _, _ -> },
            onDoorHeightChange = { _, _ -> },
            onAddWindow = {},
            onRemoveWindow = {},
            onWindowWidthChange = { _, _ -> },
            onWindowHeightChange = { _, _ -> },
            onAddCustomWall = {},
            onRemoveCustomWall = {},
            onCustomWallWidthChange = { _, _ -> },
            onCustomWallHeightChange = { _, _ -> },
            onSaveClick = {}
        )
    }
}

@Preview(showBackground = true, name = "RoomInputScreen с данными")
@Composable
fun RoomInputScreenWithDataPreview() {
    MaterialTheme {
        RoomInputScreenInternal(
            roomName = "Гостиная (Превью)",
            roomHeight = "2.75",
            roomWidth = "4.8",
            roomLength = "5.2",
            doors = listOf(ItemDimension(width = "0.8", height = "2.0")),
            windows = listOf(ItemDimension(width = "1.2", height = "1.5")),
            customWalls = emptyList(),
            onRoomNameChange = {},
            onRoomHeightChange = {},
            onRoomWidthChange = {},
            onRoomLengthChange = {},
            onAddDoor = {},
            onRemoveDoor = {},
            onDoorWidthChange = { _, _ -> },
            onDoorHeightChange = { _, _ -> },
            onAddWindow = {},
            onRemoveWindow = {},
            onWindowWidthChange = { _, _ -> },
            onWindowHeightChange = { _, _ -> },
            onAddCustomWall = {},
            onRemoveCustomWall = {},
            onCustomWallWidthChange = { _, _ -> },
            onCustomWallHeightChange = { _, _ -> },
            onSaveClick = {}
        )
    }
}

class RoomViewModelFactory(
    private val houseId: String,
    private val roomId: Int?,
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RoomViewModel::class.java)) {
            val db = AppDatabase.getDatabase(application)
            @Suppress("UNCHECKED_CAST")
            return RoomViewModel(houseId, roomId, db.roomDao()) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomInputScreen(
    modifier: Modifier = Modifier,
    houseId: String,
    roomId: Int?,
    navController: NavController,
) {
    val application = androidx.compose.ui.platform.LocalContext.current.applicationContext as Application
    val viewModel: RoomViewModel = viewModel(factory = RoomViewModelFactory(houseId, roomId, application))

    // 1. Получаем CoroutineScope для асинхронных операций
    val scope = rememberCoroutineScope()

    val roomName by viewModel.roomName.collectAsState()
    val roomHeight by viewModel.roomHeight.collectAsState()
    val roomWidth by viewModel.roomWidth.collectAsState()
    val roomLength by viewModel.roomLength.collectAsState()
    val doors by viewModel.doors.collectAsState()
    val windows by viewModel.windows.collectAsState()
    val customWalls by viewModel.customWalls.collectAsState()

    RoomInputScreenInternal(
        modifier = modifier,
        roomName = roomName,
        roomHeight = roomHeight,
        roomWidth = roomWidth,
        roomLength = roomLength,
        doors = doors,
        windows = windows,
        customWalls = customWalls,
        onRoomNameChange = viewModel::updateRoomName, // Используем ссылки на методы для краткости
        onRoomHeightChange = viewModel::updateRoomHeight,
        onRoomWidthChange = viewModel::updateRoomWidth,
        onRoomLengthChange = viewModel::updateRoomLength,
        onAddDoor = viewModel::addDoor,
        onRemoveDoor = viewModel::removeDoor,
        onDoorWidthChange = viewModel::updateDoorWidth,
        onDoorHeightChange = viewModel::updateDoorHeight,
        onAddWindow = viewModel::addWindow,
        onRemoveWindow = viewModel::removeWindow,
        onWindowWidthChange = viewModel::updateWindowWidth,
        onWindowHeightChange = viewModel::updateWindowHeight,
        onAddCustomWall = viewModel::addCustomWall,
        onRemoveCustomWall = viewModel::removeCustomWall,
        onCustomWallWidthChange = viewModel::updateCustomWallWidth,
        onCustomWallHeightChange = viewModel::updateCustomWallHeight,
        onSaveClick = {
            // 2. Запускаем корутину для сохранения
            scope.launch {
                // 3. Вызываем новую suspend-функцию
                viewModel.saveRoomData()

                // 4. После завершения сохранения возвращаемся на предыдущий экран
                // Ничего передавать не нужно, т.к. предыдущий экран сам обновит данные из БД
                withContext(Dispatchers.Main) {
                    viewModel.resetAllFields()
                    navController.popBackStack()
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DimensionTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        isError = isError
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
                        isError = item.width == null // Пример валидации
                    )
                    DimensionTextField( // DimensionTextField теперь принимает isError
                        label = "Высота (м)",
                        value = item.height,
                        onValueChange = { newHeight ->
                            onItemHeightChange(index, newHeight)
                        },
                        modifier = Modifier.weight(1f),
                        isError = item.height == null // Пример валидации
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