package com.example.zamerpro

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.util.UUID


data class ItemDimension(
    var id: String = UUID.randomUUID().toString(), 
    var width: String = "",
    var height: String = ""
)

@Preview(showBackground = true)
@Composable
fun RoomScreenPreview() {
    MaterialTheme {
        RoomInputScreen(modifier = Modifier)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomInputScreen(modifier: Modifier) {
    var roomHeight by remember { mutableStateOf("") }
    var roomWidth by remember { mutableStateOf("") }
    var roomLength by remember { mutableStateOf("") }

    val doors = remember { mutableStateListOf(ItemDimension()) } // Начинаем с одной двери
    val windows = remember { mutableStateListOf(ItemDimension()) } // Начинаем с одного окна
    val customWalls = remember { mutableStateListOf(ItemDimension()) } // Для "необычных стен"

    LazyColumn( // Используем LazyColumn для прокручиваемого содержимого, если элементов станет много
        modifier = modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp) // Пространство между карточками
    ) {
        // Карточка для основных параметров комнаты
        item {
            Card(
                modifier = Modifier.fillMaxWidth(0.9f),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp), // Уменьшил внутренний padding
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
                        onValueChange = { roomHeight = it })
                    Spacer(modifier = Modifier.height(8.dp))
                    DimensionTextField(
                        label = "Ширина (м)",
                        value = roomWidth,
                        onValueChange = { roomWidth = it })
                    Spacer(modifier = Modifier.height(8.dp))
                    DimensionTextField(
                        label = "Длина (м)",
                        value = roomLength,
                        onValueChange = { roomLength = it })
                }
            }
        }

        // Секция для Дверей
        item {
            DimensionListSection(
                title = "Размеры дверей",
                items = doors,
                onAddItem = { doors.add(ItemDimension()) },
                onRemoveItem = { index -> doors.removeAt(index) },
                onItemChange = { index, newItem -> doors[index] = newItem }
            )
        }

        // Секция для Окон
        item {
            DimensionListSection(
                title = "Размеры окон",
                items = windows,
                onAddItem = { windows.add(ItemDimension()) },
                onRemoveItem = { index -> windows.removeAt(index) },
                onItemChange = { index, newItem -> windows[index] = newItem }
            )
        }

        // Секция для "Необычных стен"
        item {
            DimensionListSection(
                title = "Дополнительные стены", // Или "Стены нестандартных размеров"
                items = customWalls,
                onAddItem = { customWalls.add(ItemDimension()) },
                onRemoveItem = { index -> customWalls.removeAt(index) },
                onItemChange = { index, newItem -> customWalls[index] = newItem }
            )
        }
    }
}

// Переиспользуемый Composable для текстового поля ввода одного размера
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DimensionTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true
    )
}

// Переиспользуемый Composable для секции со списком размеров (двери, окна, стены)
@Composable
fun DimensionListSection(
    title: String,
    items: MutableList<ItemDimension>, // Используем MutableList для совместимости с mutableStateListOf
    onAddItem: () -> Unit,
    onRemoveItem: (Int) -> Unit,
    onItemChange: (Int, ItemDimension) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(0.9f),
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

            // Отображаем поля для каждого элемента в списке
            items.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DimensionTextField(
                        label = "Ширина (м)",
                        value = item.width,
                        onValueChange = { newItemWidth ->
                            onItemChange(index, item.copy(width = newItemWidth))
                        },
                        modifier = Modifier.weight(1f)
                    )
                    DimensionTextField(
                        label = "Высота (м)",
                        value = item.height,
                        onValueChange = { newItemHeight ->
                            onItemChange(index, item.copy(height = newItemHeight))
                        },
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { onRemoveItem(index) }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Удалить")
                    }
                }
                if (index < items.size - 1) { // Не добавляем Divider после последнего элемента
                    Spacer(modifier = Modifier.height(4.dp)) // Небольшой отступ вместо Divider
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
