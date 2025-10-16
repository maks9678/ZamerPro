package com.example.zamerpro.homes

import android.app.Application
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.zamerpro.Class.House
import com.example.zamerpro.home.HOUSE_SCREEN_ROUTE
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAbsoluteAlignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.intellij.lang.annotations.JdkConstants

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
            House(
                id = "1",
                name = "Дом у озера (Превью)",
                lastModified = System.currentTimeMillis()
            ),
            House(
                id = "2",
                name = "Квартира в центре (Превью)",
                lastModified = System.currentTimeMillis() - 100000
            )
        )
        HousesListScreenInternal(
            houses = previewHouses,
            modifier = Modifier,
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
            modifier = Modifier,
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
            modifier = Modifier,
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
    modifier: Modifier = Modifier,
    navController: NavController,
) {
    val application = LocalContext.current.applicationContext as Application
    val viewModel: HousesListViewModel = viewModel(factory = AppViewModelProvider(application))
    val houses by viewModel.houses.collectAsState()
    val showDialog by viewModel.showDialog.collectAsState()
    var newHouseName by remember { mutableStateOf("") }

    HousesListScreenInternal(
        houses = houses,
        modifier = modifier,
        showDialog = showDialog,
        newHouseName = newHouseName,
        onNewHouseNameChange = { newHouseName = it },
        onShowDialogChange = { viewModel.onShowDialogChange(it) },
        onConfirmNewHouse = {
            if (newHouseName.isNotBlank()) {
                viewModel.createNewHouse(newHouseName) { houseId ->
                    navController.navigate("$HOUSE_SCREEN_ROUTE/$houseId")
                }
                newHouseName = ""
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
    modifier: Modifier,
    showDialog: Boolean,
    newHouseName: String,
    onNewHouseNameChange: (String) -> Unit,
    onShowDialogChange: (Boolean) -> Unit,
    onConfirmNewHouse: () -> Unit,
    onDeleteHouse: (House) -> Unit,
    onHouseClick: (House) -> Unit,
) {
    Scaffold(
        modifier,
        topBar = {
            TopAppBar(
                title = { Text("Объекты") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
    ) { paddingValues ->
        Column(
            modifier = modifier
                .padding(paddingValues)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Контент экрана (список домов или текст)
            if (houses.isEmpty()) {
                Text(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    textAlign = TextAlign.Center,
                    text = "У вас пока нет объектов. Нажмите '+' для создания."
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(8.dp), // Добавляем отступ снизу, чтобы FAB не перекрывал последний элемент
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                )
                {
                    items(houses, key = { it.id }) { house ->
                        HouseListItem(
                            house = house,
                            onClick = { onHouseClick(house) },
                            onDelete = { onDeleteHouse(house) }
                        )
                    }
                }
            }
        }

        // Диалог создания остается здесь, он будет показан поверх всего
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
                    Button(onClick = onConfirmNewHouse) { Text("Создать") }
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
            .height(130.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium,
        // 3. Можно задать цвета, если нужно (по умолчанию они из темы)
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant, // Чуть другой фон
        )
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),

        ) {
            Text(house.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                textAlign = TextAlign.Center)
            Row (modifier = Modifier,
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Изменен: ${formatTimestamp(house.lastModified)}",
                    style = MaterialTheme.typography.bodySmall
                )

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
}

// Эта фабрика подходит для ViewModel, которые наследуются от AndroidViewModel
class AppViewModelProvider(private val application: Application) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HousesListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HousesListViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}