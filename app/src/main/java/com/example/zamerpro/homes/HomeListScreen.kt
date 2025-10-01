package com.example.zamerpro.homes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.zamerpro.House
import com.example.zamerpro.home.HOUSE_SCREEN_ROUTE
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.text.style.TextAlign

const val HOUSES_LIST_SCREEN_ROUTE = "housesListScreen"

fun formatTimestamp(timestamp: Long): String {
    return java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
        .format(java.util.Date(timestamp))
}


@Preview(showBackground = true, name = "Список домов с данными")
@Composable
fun HousesListScreenWithDataPreview() {
    MaterialTheme { // Оберните в вашу тему
        val previewHouses = listOf(
            House(id = "1", name = "Дом у озера (Превью)", lastModified = System.currentTimeMillis()),
            House(id = "2", name = "Квартира в центре (Превью)", lastModified = System.currentTimeMillis() - 100000)
        )
        HousesListScreenInternal(
            houses = previewHouses,
            showDialog = false,
            newHouseName = "",
            onNewHouseNameChange = {},
            onShowDialogChange = {},
            onConfirmNewHouse = {},
            onDeleteHouse = {},
            onHouseClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Пустой список домов")
@Composable
fun HousesListScreenEmptyPreview() {
    MaterialTheme {
        HousesListScreenInternal(
            houses = emptyList(),
            showDialog = false,
            newHouseName = "",
            onNewHouseNameChange = {},
            onShowDialogChange = {},
            onConfirmNewHouse = {},
            onDeleteHouse = {},
            onHouseClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Экран с диалогом создания")
@Composable
fun HousesListScreenWithDialogPreview() {
    MaterialTheme {
        HousesListScreenInternal(
            houses = emptyList(),
            showDialog = true, // Показываем диалог
            newHouseName = "Мой новый дом",
            onNewHouseNameChange = {},
            onShowDialogChange = {},
            onConfirmNewHouse = {},
            onDeleteHouse = {},
            onHouseClick = {}
        )
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HousesListScreen(
    navController: NavController,
    viewModel: HousesListViewModel = viewModel()
) {
    val houses by viewModel.houses.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var newHouseName by remember { mutableStateOf("") }

    HousesListScreenInternal(
        houses = houses,
        showDialog = showDialog,
        newHouseName = newHouseName,
        onNewHouseNameChange = { newHouseName = it },
        onShowDialogChange = { showDialog = it },
        onConfirmNewHouse = {
            if (newHouseName.isNotBlank()) {
                viewModel.createNewHouse(newHouseName) { houseId ->
                    navController.navigate("$HOUSE_SCREEN_ROUTE/$houseId")
                }
                newHouseName = ""
                showDialog = false
            }
        },
        onDeleteHouse = { house -> viewModel.deleteHouse(house) },
        onHouseClick = { house ->
            navController.navigate("$HOUSE_SCREEN_ROUTE/${house.id}")
        }
    )
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HousesListScreenInternal(
    houses: List<House>,
    showDialog: Boolean,
    newHouseName: String,
    onNewHouseNameChange: (String) -> Unit,
    onShowDialogChange: (Boolean) -> Unit,
    onConfirmNewHouse: () -> Unit,
    onDeleteHouse: (House) -> Unit,
    onHouseClick: (House) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Мои Объекты") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,

                    // Цвет текста и иконок
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,

                    // Если есть навигационная иконка
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,

                    // Если есть действия (actions)
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ) )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onShowDialogChange(true) }) {
                Icon(Icons.Filled.Add, contentDescription = "Создать новый объект")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (houses.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(modifier = Modifier.padding(horizontal = 8.dp),
                        textAlign = TextAlign.Center,
                        text = "У вас пока нет объектов. Нажмите '+' для создания.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(all = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(houses, key = { it.id }) { house ->
                        HouseListItem(
                            house = house,
                            onClick = {
                                onHouseClick(house)
                            },
                            onDelete = { onDeleteHouse(house) }
                        )
                    }
                }
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { onShowDialogChange(false) },
                title = { Text("Новый объект") },
                text = {
                    OutlinedTextField(
                        value = newHouseName,
                        onValueChange = onNewHouseNameChange,
                        label = { Text("Название объекта") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    Button(
                        onClick =  onConfirmNewHouse) { Text("Создать") }
                },
                dismissButton = {
                    Button(onClick = { onShowDialogChange(false) }) { Text("Отмена") }
                }
            )
        }
    }
}

@Composable
fun HouseListItem(
    house: House,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(house.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    "Изменен: ${formatTimestamp(house.lastModified)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Удалить объект",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}