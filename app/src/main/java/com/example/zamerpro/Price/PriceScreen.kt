package com.example.zamerpro.Price

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.zamerpro.home.previewHouse
import com.example.zamerpro.home.previewsRoom
import com.example.zamerpro.ui.theme.Text

const val PRICE_SCREEN_ROUTE = "priceScreen"

data class Price(
    var priceArea: Int=600,
    var priceMetre: Int=600,
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

    val price = Price()
    PriceScreenInternal(
        navController,
        currentHouse,
        price.priceArea,
        price.priceMetre,
        viewModel.quantityWindows,
        price.priceCoverWindows
    )

}
@ExperimentalMaterial3Api
@Composable
@Preview(showBackground = true)
fun Preview() {
    PriceScreenInternal(
        rememberNavController(),
        currentHouse = previewHouse,
        600,
        600,
        8,
        300

        )
}

@ExperimentalMaterial3Api
@Composable
fun PriceScreenInternal(
    navController: NavController,
    currentHouse: House?,
    priceArea: Int,
    priceMetre: Int,
    quantityWindows: Int,
    priceCoverWindows: Int

) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentHouse?.name ?: "Мой Дом") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item {
                currentHouse?.let {
                    PointWorkItem(
                        "Шпаклевка квадратуры",
                        currentHouse.totalWallArea,
                        priceArea
                    )
                    PointWorkItem(
                        "Шпаклевка метража",
                        currentHouse.totalWindowMetre,
                        priceMetre
                    )
                    PointWorkItem(
                        "Укрывка окон",
                        quantityWindows,
                        priceCoverWindows

                    )
                }
            }
        }

    }
}

@Composable
fun PointWorkItem(
    nameWork: String,
    amountWork: Int,
    priceWork: Int,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column {
            Text(text = nameWork)

            Row {
                Text(text = amountWork.toString())
                Text(text = priceWork.toString())
                Text(
                    text = { amountWork * priceWork }.toString(),
                )
            }
        }
    }
}
