package com.example.zamerpro.room

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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.zamerpro.ItemDimension
import java.util.UUID
import kotlin.collections.forEachIndexed

const val ROOM_INPUT_ROUTE = "roomInput"
// Ключ для возврата результата
const val NEW_ROOM_RESULT_KEY = "new_room_details"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomInputScreen(
    modifier: Modifier = Modifier,
    viewModel: RoomViewModel = viewModel(),
    navController: NavController,
    // navController: NavController // Передайте сюда ваш NavController
) {
    val roomName by viewModel.roomName.collectAsState()
    val roomHeight by viewModel.roomHeight.collectAsState()
    val roomWidth by viewModel.roomWidth.collectAsState()
    val roomLength by viewModel.roomLength.collectAsState()
    val doors by viewModel.doors.collectAsState()
    val windows by viewModel.windows.collectAsState()
    val customWalls by viewModel.customWalls.collectAsState()

    // Состояние для отображения Snackbar об ошибке (опционально)
    // val snackbarHostState = remember { SnackbarHostState() }
    // var showValidationError by remember { mutableStateOf(false) }

    /*
    if (showValidationError) {
        LaunchedEffect(snackbarHostState) {
            snackbarHostState.showSnackbar(
                message = "Ошибка: Проверьте правильность введенных данных (название, ширина, длина).",
                duration = SnackbarDuration.Short
            )
            showValidationError = false // Сбросить флаг после показа
        }
    }
    */

    Scaffold(
        /* bottomBar = { // Можно разместить кнопку "Сохранить" внизу экрана
            Button(
                onClick = {
                    val simpleRoom = viewModel.calculateAndGetSimpleRoom()
                    if (simpleRoom != null) {
                        // navController.previousBackStackEntry?.savedStateHandle?.set("new_room_details", simpleRoom)
                        // viewModel.resetAllFields()
                        // navController.popBackStack()
                        // println("Room saved: $simpleRoom") // Для отладки
                    } else {
                        // showValidationError = true
                        // println("Validation failed") // Для отладки
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Icon(Icons.Filled.Done, contentDescription = "Сохранить комнату")
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Сохранить комнату")
            }
        }, */
        // snackbarHost = { SnackbarHost(snackbarHostState) } // Для отображения сообщений
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues) // Применяем padding от Scaffold
                .background(color = MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp, vertical = 8.dp), // Внутренние отступы для контента
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Поле для названия комнаты
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
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Параметры комнаты",
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        DimensionTextField(
                            label = "Высота (м)",
                            value = roomHeight,
                            onValueChange = { viewModel.updateRoomHeight(it) })
                        Spacer(modifier = Modifier.height(8.dp))
                        DimensionTextField(
                            label = "Ширина (м)",
                            value = roomWidth,
                            onValueChange = { viewModel.updateRoomWidth(it) },
                            isError = roomWidth.toDoubleOrNull() == null && roomWidth.isNotBlank() // Пример валидации
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        DimensionTextField(
                            label = "Длина (м)",
                            value = roomLength,
                            onValueChange = { viewModel.updateRoomLength(it) },
                            isError = roomLength.toDoubleOrNull() == null && roomLength.isNotBlank() // Пример валидации
                        )
                    }
                }
            }

            // Секции для Дверей, Окон, Стен (без изменений в их структуре)
            item {
                DimensionListSection(
                    title = "Размеры дверей", items = doors,
                    onAddItem = { viewModel.addDoor() },
                    onRemoveItem = { item -> viewModel.removeDoor(item) },
                    onItemWidthChange = { index, newWidth -> viewModel.updateDoorWidth(index, newWidth) },
                    onItemHeightChange = { index, newHeight -> viewModel.updateDoorHeight(index, newHeight) }
                )
            }
            item {
                DimensionListSection(
                    title = "Размеры окон", items = windows,
                    onAddItem = { viewModel.addWindow() },
                    onRemoveItem = { item -> viewModel.removeWindow(item) }, // Исправлено на removeWindow
                    onItemWidthChange = { index, newWidth -> viewModel.updateWindowWidth(index, newWidth) }, // Исправлено
                    onItemHeightChange = { index, newHeight -> viewModel.updateWindowHeight(index, newHeight) } // Исправлено
                )
            }
            item {
                DimensionListSection(
                    title = "Дополнительные стены", items = customWalls,
                    onAddItem = { viewModel.addCustomWall() },
                    onRemoveItem = { item -> viewModel.removeCustomWall(item) },
                    onItemWidthChange = { index, newWidth -> viewModel.updateCustomWallWidth(index, newWidth) },
                    onItemHeightChange = { index, newHeight -> viewModel.updateCustomWallHeight(index, newHeight) }
                )
            }

            // Кнопка Сохранить в конце списка
            item {
                Spacer(modifier = Modifier.height(8.dp)) // Отступ перед кнопкой
                Button(
                    onClick = {
                        val simpleRoom = viewModel.calculateAndGetSimpleRoom()
                        if (simpleRoom != null) {
                            navController.previousBackStackEntry?.savedStateHandle?.set("new_room_details", simpleRoom)
                            viewModel.resetAllFields()
                           navController.popBackStack()
                            println("LOG: Room to be saved: $simpleRoom")
                            viewModel.resetAllFields() // Сбрасываем поля для теста
                        } else {
                            // Логика отображения ошибки (например, через Snackbar или Toast)
                            // showValidationError = true
                            println("LOG: Validation failed for room save.")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    Icon(Icons.Filled.Done, contentDescription = "Сохранить комнату")
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Сохранить и добавить в дом")
                }
                Spacer(modifier = Modifier.height(16.dp)) // Отступ после кнопки
            }
        }
    }
}

// DimensionTextField и DimensionListSection остаются без изменений,
// но DimensionTextField теперь может принимать isError параметр.
// Я добавлю его в определение, чтобы код выше компилировался.

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DimensionTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
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
        modifier = Modifier.fillMaxWidth(0.9f), // В последних изменениях RoomInputScreen используется 0.95f
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            items.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
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

            Spacer(modifier = Modifier.height(8.dp))
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