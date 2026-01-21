package com.example.zamerpro.Price

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.zamerpro.Class.House
import com.example.zamerpro.Class.Work
import com.example.zamerpro.home.previewHouse
import com.google.android.material.dialog.MaterialAlertDialogBuilder

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
    val listCost by viewModel.listCost.collectAsState()
    val sumListWork by viewModel.sumListWork.collectAsState()
    val listWork by viewModel.listWork.collectAsState()
    val listAvailableWorks by viewModel.listAvailableWorks.collectAsState()

    PriceScreenInternal(
        navController,
        currentHouse,
        listCost,
        viewModel::addCost,
        viewModel::totalCost,
        viewModel::addListSumWork,
        sumListWork,
        addWork = {viewModel::addWork} ,
        listWork,
        listAvailableWorks,
        addWorkHouse = viewModel::addWorkHouse
    )

}


@ExperimentalMaterial3Api
@Composable
@Preview(showBackground = true)
fun Preview() {
    PriceScreenInternal(
        rememberNavController(),
        currentHouse = previewHouse,

        onListCost = listOf(
            CostItem("пельмени", 123),
            CostItem("дрова", 241)
        ),
        { _, _ -> 0 },
        { 12 },
        {},
        12,
        { },
        emptyList(),
        emptyList(),
        {}
    )
}

@ExperimentalMaterial3Api
@Composable
fun PriceScreenInternal(
    navController: NavController,
    currentHouse: House?,
    onListCost: List<CostItem>,
    onAddCost: (String, Int) -> Unit,
    onTotalCost: () -> Int,
    onAddSum: (Int) -> Unit,
    sumListWork: Int,
    addWork: (Work) -> Unit,
    listWork: List<Work>,
    listAvailableWorks:List<Work>,
    addWorkHouse: (Int) -> Unit
) {
    var isShowAddWork by remember { mutableStateOf(false) }
    Scaffold { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text("Виды работ:")
                currentHouse?.let {
                    listWork.forEach { work ->
                        PointWorkItem(work, onAddSum, it)
                    }
                    Button(
                        modifier = Modifier,
                        onClick = { isShowAddWork = true }) {
                        Text(text = "Добавить работу")
                    }


                    Text(text = "Сумма по работам : ${sumListWork}")
                }
            }

            item {
                Cost(
                    onListCost,
                    onAddCost,
                    onTotalCost
                )


                Text(text = "Итого за объект : ")
            }
        }
        if (isShowAddWork) {
            AddWorkDialog(listAvailableWorks,{},{}, addWork)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewAddWorkDialog() {
    AddWorkDialog(emptyList(),{},{}, {})
}

@Composable
fun AddWorkDialog(
    listAvailableWorks: List<Work>,
    addWorkHouse: (Work) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (work: Work) -> Unit
) {
    var workName by remember { mutableStateOf("") }
    var priceWork by remember { mutableStateOf("") }
    var squareMetreCustom by remember { mutableStateOf(Multiplicand.SQUARE) }
    var customMultiplicand by remember { mutableStateOf<Int?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Добавить работу") },
        text = {
            Column {
                OutlinedTextField(
                    value = workName,
                    onValueChange = { workName = it },
                    label = { Text("Название работы") }
                )
                OutlinedTextField(
                    value = priceWork,
                    onValueChange = { priceWork = it },
                    label = { Text("цена кв/м") }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Multiplicand.entries.forEach { option ->
                        Column(Modifier.clickable { squareMetreCustom = option }) {
                            RadioButton(
                                selected = squareMetreCustom == option,
                                onClick = { squareMetreCustom = option }
                            )
                            Text(option.displayName)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                Text("Доступные работы")
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                ) {
                    items(listAvailableWorks) { item ->
                        Button(modifier = Modifier.fillMaxWidth(), onClick = {addWorkHouse(item)}) {
                            Text("${item.name}")
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val work = Work(
                        name = workName,
                        priceWork = priceWork.toInt(),
                        areaMetreCustom = squareMetreCustom
                    )
                    onConfirm(work)
                    onDismiss()
                }
            ) {
                Text("ОК")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

@Composable
@Preview(showBackground = true)
fun PreviewPointWorkItem() {
    PointWorkItem(
        Work(name = "dsgsd", priceWork = 12, areaMetreCustom = Multiplicand.METRE),
        {},
        house = House(name = "adfsas")
    )
}

@Composable
fun PointWorkItem(
    work: Work,
    onAddSum: (Int) -> Unit,
    house: House,
    modifier: Modifier = Modifier,
) {
    var priceWorkText by remember { mutableStateOf(work.priceWork.toString()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp, 8.dp)) {
            Text(
                text = work.name,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = priceWorkText,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() }) {
                            priceWorkText = input
                        }
                    },
                    label = { Text("Цена") },
                    modifier = Modifier
                        .width(70.dp)
                        .height(60.dp), // стандартная высота TextField
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )

                val priceWork = priceWorkText.toIntOrNull() ?: 0
                val multiplicandTwo =
                    if (work.areaMetreCustom == Multiplicand.METRE) {
                        house.totalWindowMetre
                    } else if (work.areaMetreCustom == Multiplicand.SQUARE) {
                        house.totalWallArea
                    } else work.customMultiplicand

                val sum = multiplicandTwo * priceWork
                Text(text = " р =")
                Text(text = "$sum р")
                onAddSum(sum)
            }
        }
    }
}

@Composable
fun Cost(
    listCost: List<CostItem>,
    onAddCost: (String, Int) -> Unit,
    onTotalCost: () -> Int,
) {
    var nameInput by remember { mutableStateOf("") }
    var priceInput by remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Расходники:", Modifier.fillMaxWidth())
        listCost.forEach { item ->
            Text("${item.name}: ${item.prise} ₽")
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
            modifier = Modifier.fillMaxWidth(0.5f),// стандартная высота TextField
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
            .padding(16.dp), onClick = {
            onAddCost(nameInput, priceInput.toInt())
            nameInput = ""
            priceInput = ""
        }) {
        Text("Добавить чек")

    }
    Text(
        "Сумма по расходникам :${onTotalCost.invoke()} "
    )


}

@Composable
fun CustomOutlinedBasicTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    width: Dp = 30.dp,
    height: Dp = 30.dp,
    labelPadding: PaddingValues = PaddingValues(bottom = 4.dp),
) {
    var isFocused by remember { mutableStateOf(false) }
    Column(modifier = modifier.width(width)) {
        Text(
            text = label,
            modifier = Modifier.padding(labelPadding),
            fontSize = 14.sp,
            color = if (isFocused) Color(0xFF6200EE) else Color.Gray
        )
        Box(
            modifier = Modifier
                .height(height)
                .border(
                    width = if (isFocused) 2.dp else 1.dp,
                    color = if (isFocused) Color(0xFF6200EE) else Color.Gray,
                    shape = RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 8.dp, vertical = 12.dp)
                .onFocusChanged { focusState ->
                    isFocused = focusState.isFocused
                },
        ) {
            BasicTextField(
                value = value,
                onValueChange = {},
                singleLine = true,
                modifier = Modifier.fillMaxSize(),
                textStyle = LocalTextStyle.current.copy(fontSize = 16.sp, color = Color.Black)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AreaSelector() {
    var selectedOption by remember { mutableStateOf("square") } // "square", "meter", "custom"
    var customInput by remember { mutableStateOf("") }

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = selectedOption == "square",
                onClick = { selectedOption = "square" }
            )
            Text("Квадратура")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = selectedOption == "meter",
                onClick = { selectedOption = "meter" }
            )
            Text("Метраж")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = selectedOption == "custom",
                onClick = { selectedOption = "custom" }
            )
            Text("Другое")
        }

        if (selectedOption == "custom") {
            OutlinedTextField(
                value = customInput,
                onValueChange = { customInput = it },
                label = { Text("Введите свой вариант") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Выбрано: ${
                when (selectedOption) {
                    "square" -> "Квадратура"
                    "meter" -> "Метраж"
                    "custom" -> customInput.ifBlank { "Нет ввода" }
                    else -> ""
                }
            }")
    }
}



