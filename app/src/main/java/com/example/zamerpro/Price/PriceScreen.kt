package com.example.zamerpro.Price

import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

@Composable
fun PriceScreen(
    navController: NavController,
    idHouse: String
) {
    val application = LocalContext.current.applicationContext as Application
    val viewModel: PriceViewModel = viewModel(
        factory = PriceViewModel.PriceViewModelFactory(application, idHouse)
    )
    val currentHouse by viewModel.currentHouse.collectAsState()
    val roomsInHouse by viewModel.roomsInHouse.collectAsState()


}

@Composable
@Preview(showBackground = true)
fun Preview() {
    PriceScreenInternal(
        rememberNavController(),
        currentHouse = previewHouse,
        roomsInHouse = previewsRoom,

        )
}

@Composable
fun PriceScreenInternal(
    navController: NavController,
    currentHouse: House?,
    roomsInHouse: List<Room>,
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
