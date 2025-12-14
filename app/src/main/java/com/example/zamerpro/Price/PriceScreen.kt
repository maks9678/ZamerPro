package com.example.zamerpro.Price

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.zamerpro.Class.House
import com.example.zamerpro.home.previewHouse

const val PRICE_SCREEN_ROUTE = "priceScreen"

data class Price(
    var priceArea: Int = 700,
    var priceMetre: Int = 700,
    var priceCoverWindows: Int = 300,
)

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
    val customWorks by viewModel.customWorks.collectAsState()

    // Очищаем список сумм при изменении дома
    androidx.compose.runtime.LaunchedEffect(currentHouse?.id) {
        viewModel.clearListSum()
    }

    PriceScreenInternal(
        navController,
        currentHouse,
        listCost,
        viewModel::addCost,
        viewModel::totalCost,
        viewModel::addListSum,
        sumListWork,
        viewModel.totalCost(),
        customWorks,
        viewModel::addCustomWork,
        viewModel::removeCustomWork,
        viewModel.getCustomWorksSum()
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
        { _, _ -> },
        { 364 },
        { },
        1200,
        364,
        emptyList(),
        { _, _, _ -> },
        { },
        0
    )
}

@ExperimentalMaterial3Api
@Composable
fun PriceScreenInternal(
    navController: NavController,
    currentHouse: House?,
    onListCost:List<CostItem>,
    onAddCost: (String, Int) -> Unit,
    onTotalCost: () -> Int,
    onAddSum: (Int) -> Unit,
    sumListWork: Int,
    totalCostValue: Int,
    customWorks: List<CustomWork>,
    onAddCustomWork: (String, WorkType, Int) -> Unit,
    onRemoveCustomWork: (CustomWork) -> Unit,
    customWorksSum: Int
) {
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
            val price = Price()
            item {
                Text("Виды работ:", style = MaterialTheme.typography.titleMedium)
                currentHouse?.let {
                    PointWorkItem(
                        key = "wall_area",
                        nameWork = "Шпаклевка квадратуры",
                        amountWork = currentHouse.totalWallArea,
                        priceWork = price.priceArea,
                        onAddSum = onAddSum
                    )
                    PointWorkItem(
                        key = "window_metre",
                        nameWork = "Шпаклевка метража",
                        amountWork = currentHouse.totalWindowMetre,
                        priceWork = price.priceMetre,
                        onAddSum = onAddSum
                    )
                    PointWorkItem(
                        key = "window_cover",
                        nameWork = "Укрывка окон",
                        amountWork = currentHouse.totalQuantityWindows,
                        priceWork = price.priceCoverWindows,
                        onAddSum = onAddSum
                    )
                    Text(
                        text = "Сумма по работам : ${sumListWork + customWorksSum} ₽",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } ?: run {
                    Text("Загрузка данных...")
                }
            }
            // Секция кастомных работ
            item {
//                CustomWorksSection(
//                    customWorks = customWorks,
//                    currentHouse = currentHouse,
//                    onAddCustomWork = onAddCustomWork,
//                    onRemoveCustomWork = onRemoveCustomWork
//                )
            }
            item {
                Cost(
                    onListCost,
                    onAddCost,
                    onTotalCost
                )

                Text(
                    text = "Итого за объект : ${sumListWork + customWorksSum + totalCostValue} ₽",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
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
fun PointWorkItem(
    key: String,
    nameWork: String,
    amountWork: Int,
    priceWork: Int,
    onAddSum: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var priceWorkText by remember(key) { mutableStateOf(priceWork.toString()) }
    var lastReportedSum by remember(key) { mutableStateOf<Int?>(null) }

    val priceWorkInt = priceWorkText.toIntOrNull() ?: 0
    val sum = amountWork * priceWorkInt

    // Обновляем сумму при изменении
    androidx.compose.runtime.LaunchedEffect(sum, amountWork) {
        val difference = if (lastReportedSum == null) {
            sum // Первая инициализация - добавляем полную сумму
        } else {
            sum - lastReportedSum!! // Добавляем разницу
        }
        if (difference != 0) {
            onAddSum(difference)
            lastReportedSum = sum
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp, 8.dp)) {
            Text(
                text = nameWork,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "$amountWork *")

                OutlinedTextField(
                    value = priceWorkText,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() || input.isEmpty() }) {
                            priceWorkText = input
                        }
                    },
                    label = { Text("Цена") },
                    modifier = Modifier
                        .width(70.dp)
                        .height(60.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )

                Text(text = " р =")
                Text(text = "${sum} рублей")
            }
        }
    }
}
