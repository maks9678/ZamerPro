package com.example.zamerpro.Price

import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Scaffold
import android.app.Application
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.zamerpro.Class.House
import com.example.zamerpro.Class.Supplies
import com.example.zamerpro.Class.Work
import com.example.zamerpro.home.previewHouse
import com.example.zamerpro.materials.DialogMode
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import kotlinx.coroutines.launch

const val PRICE_SCREEN_ROUTE = "priceScreen"

enum class Multiplicand(val displayName: String) {
    SQUARE("квадратура"),
    METRE("метраж"),
    CUSTOM("свое число")
}

@ExperimentalMaterial3Api
@Composable
fun PriceScreen(
    navController: NavController,
    houseId: String
) {
    val application = LocalContext.current.applicationContext as Application
    val viewModel: PriceViewModel = viewModel(
        factory = PriceViewModel.PriceViewModelFactory(application, houseId)
    )
    val currentHouse by viewModel.currentHouse.collectAsState()
    val listWorkInHouse by viewModel.listWorksInHouse.collectAsState()
    val sumListWork by viewModel.sumListWorkInHouse.collectAsState()
    val listAllWork by viewModel.listAllWorks.collectAsState()
    val editorState by viewModel.editorState

    currentHouse?.let {
        WorkScreenInternal(
            it,
            editorState,
            listAllWork,
            listWorkInHouse,
            sumListWork,
            viewModel::addSupplies,
            viewModel::deleteSupplies,
            viewModel::deleteWork,
            viewModel::startEditPrice,
            viewModel::clearEditor,
            viewModel::saveWork,
            viewModel::addWorkToHouse,
            viewModel::calculation,
            viewModel::updateTextWork,
            viewModel::updatePriceWork,
            viewModel::updateMultiplicandWork,
            viewModel::updateCustomWork,
        )
    }
}


@ExperimentalMaterial3Api
@Composable
@Preview(showBackground = true)
fun Preview() {
    WorkScreenInternal(
        currentHouse = previewHouse,
        editorState = PriceEditorState(),
        emptyList(),
        emptyList(),
        12,
        { },
        {},
        {},
        {},
        {},
        {},
        {},
        { 12 },
        {},
        {},
        {},
        {},
    )
}

@ExperimentalMaterial3Api
@Composable
fun WorkScreenInternal(
    currentHouse: House,
    editorState: PriceEditorState,
    listAllWork: List<Work>,
    listWorksInHouse: List<Work>,
    sumListWorkInHouse: Int,
    addSupplies: (Supplies) -> Unit,
    deleteSupplies: (Supplies) -> Unit,
    deleteWork: (Work) -> Unit,
    startEditPrice: (Work) -> Unit,
    clearEditor: () -> Unit,
    saveWork: () -> Unit,
    addWorkToHouse: (Int) -> Unit,
    calculation: (Work) -> Int,
    updateTextWork: (String) -> Unit,
    updatePriceWork: (Int) -> Unit,
    updateMultiplicandWork: (Multiplicand) -> Unit,
    updateCustomWork: (Int) -> Unit,
) {

    var isShowAddWork by remember { mutableStateOf(false) }
    var addEdit by remember { mutableStateOf(DialogMode.ADD) }
    val sumSupplies = currentHouse.listSupplies.sumOf { it.price }
    val scaffoldState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    @Composable
    fun WorkDialog(
    ) {

        AlertDialog(
            onDismissRequest = {
                isShowAddWork = false
                clearEditor()
            },
            title = {
                Text(
                    if (addEdit == DialogMode.ADD) "Добавить работу"
                    else "Редактировать работу"
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = editorState.name,
                        onValueChange = { newText ->
                            updateTextWork(newText)
                        },
                        label = { Text("Название работы") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = editorState.priceWork.takeIf { it > 0 }?.toString() ?: "",
                        onValueChange = { newText ->
                            val price = newText.toIntOrNull() ?: 0
                            updatePriceWork(price)
                        },
                        label = { Text("цена кв/м") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    when (editorState.areaMetreCustom) {
                        Multiplicand.CUSTOM -> {
                            OutlinedTextField(
                                value = editorState.customMultiplicand.takeIf { it > 0 }?.toString()
                                    ?: "",
                                onValueChange = {
                                    updateCustomWork(it.toIntOrNull() ?: 0)
                                },
                                label = { Text("Свое число") }
                            )
                        }

                        Multiplicand.SQUARE -> {
                            Text(
                                text = "Квадратура : ${currentHouse.totalWallArea}",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }

                        Multiplicand.METRE -> {
                            Text(
                                "Метраж : ${currentHouse.totalWindowMetre}",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Multiplicand.entries.forEach { option ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                RadioButton(
                                    selected = editorState.areaMetreCustom == option,
                                    onClick = { updateMultiplicandWork(option) }
                                )
                                Text(text = "${option.displayName}")
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Доступные работы")
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(listAllWork.filter { it.idWork !in currentHouse.listWork }) { item ->
                            Button(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { addWorkToHouse(item.idWork) }) {
                                Text(
                                    "${item.name}",
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    enabled = editorState.name.isNotBlank() &&
                            editorState.priceWork > 0,
                    onClick = {
                        saveWork()
                        isShowAddWork = false
                        clearEditor()
                    }
                ) {
                    Text("ОК")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    isShowAddWork = false
                    clearEditor()
                }) {
                    Text("Отмена")
                }
            }
        )
    }
    Scaffold(
        snackbarHost = { SnackbarHost(scaffoldState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text("Виды работ:")
            }
            items(listWorksInHouse) { work ->
                PointWorkItem(work, currentHouse, calculation, {
                    addEdit = DialogMode.EDIT
                    isShowAddWork = true
                }, startEditPrice, deleteWork)
            }
            item {
                Button(
                    modifier = Modifier,
                    onClick = { isShowAddWork = true }) {
                    Text(text = "Добавить работу")
                }
                Text(text = "Сумма по работам : ${sumListWorkInHouse}")

            }
            item {
                Check(
                    currentHouse.listSupplies,
                    scaffoldState = scaffoldState,
                    addSupplies,
                    deleteSupplies,
                )

                Text(
                    "Сумма по расходникам :${sumSupplies} "
                )
                Text(text = "Итого за объект :${sumListWorkInHouse + sumSupplies} ")
            }
        }
        if (isShowAddWork) {
            WorkDialog()
        }
    }

}

@Composable
@Preview(showBackground = true)
fun PreviewPointWorkItem() {
    PointWorkItem(
        Work(name = "dsgsd", priceWork = 12, areaMetreCustom = Multiplicand.METRE),
        house = House(name = "adfsas"),
        { 12 },
        {},
        {},
        {}
    )
}

@Composable
fun PointWorkItem(
    work: Work,
    house: House,
    calculation: (Work) -> Int,
    startEditPrice: () -> Unit,
    clickEditWork: (Work) -> Unit,
    clickDeleteWork: (Work) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = MaterialTheme.colorScheme.primary,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.secondaryContainer),
        modifier = Modifier
            .clickable {
            startEditPrice()
            clickEditWork(work)
        }
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            ,
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp, 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = work.name,
                    textAlign = TextAlign.Start,
                    modifier = Modifier,
                )
                val textAreaMetreCustom = when (work.areaMetreCustom) {
                    Multiplicand.METRE -> "${house.totalWindowMetre} m"
                    Multiplicand.SQUARE -> "${house.totalWallArea} кв"
                    else -> "${work.customMultiplicand}"
                }
                Text(text = "${work.priceWork} р * $textAreaMetreCustom = ${calculation(work)} р")
            }
            IconButton(
                modifier = Modifier.size(40.dp),
                onClick = { clickDeleteWork(work) }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Удалить работу"
                )
            }
        }
    }
}

@Composable
fun Check(
    listSupplies: List<Supplies>,
    scaffoldState: SnackbarHostState,
    onAddSupplies: (Supplies) -> Unit,
    clickDeleteSupplies: (Supplies) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    var nameInput by remember { mutableStateOf("") }
    var priceInput by remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            "Расходники:", Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        listSupplies.forEach { item ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.secondaryContainer),
                color = MaterialTheme.colorScheme.primary
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                )
                {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = "${item.name}: ${item.price} ₽"
                    )
                    IconButton(
                        modifier = Modifier.size(40.dp),
                        onClick = { clickDeleteSupplies(item) }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Удалить"
                        )
                    }
                }
            }
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = nameInput,
            onValueChange = { input ->
                if (input.all { it.isLetter() }) {
                    nameInput = input
                }
            },
            label = { Text("Чек") },
            modifier = Modifier.fillMaxWidth(0.7f),// стандартная высота TextField
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )

        OutlinedTextField(
            value = priceInput,
            onValueChange = { input ->
                if (input.all { it.isDigit() }) {
                    priceInput = input
                }
            },
            label = { Text("Цена") },
            modifier = Modifier.fillMaxWidth(), // стандартная высота TextField
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }
    Button(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp), onClick = {
            if (nameInput.isNotEmpty() && priceInput.isNotEmpty()) {
                onAddSupplies(Supplies(name = nameInput, price = priceInput.toInt()))
                nameInput = ""
                priceInput = ""
            } else {
                coroutineScope.launch {
                    scaffoldState.showSnackbar("Заполните все поля")
                }
            }
        }) {
        Text("Добавить чек")
    }
}