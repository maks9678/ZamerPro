package com.example.zamerpro.MaterialsList

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.example.zamerpro.House
import com.example.zamerpro.home.HOUSE_SCREEN_ROUTE
import com.example.zamerpro.homes.HousesListViewModel

const val MATERIALS_LIST_SCREEN_ROUTE = "materialsListScreen"

@Composable
@Preview(showBackground = true)
fun MaterialsListScreenPreview() {
    val previewHouses = listOf(
        House(id = "1", name = "Дом у озера", totalWallArea = 100, totalWindowMetre = 5),
        House(id = "2", name = "Квартира в центре", totalWallArea = 200, totalWindowMetre = 10),
        House(id = "3", name = "Дом у моря", totalWallArea = 150, totalWindowMetre = 45)
    )
    MaterialsListScreenInternal(
        houses = previewHouses,
        onHouseClick = {},
    )
}

@Composable
fun MaterialsListScreen(
    navController: NavController,
    viewModel: MaterialsListViewModel = viewModel()
) {
    val houses by viewModel.houses.collectAsState()

    MaterialsListScreenInternal(houses = houses, onHouseClick = { house ->
        navController.navigate("$HOUSE_SCREEN_ROUTE/${house.id}")
    })
}

@Composable
fun MaterialListItem(
    house: House,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(text = house.name, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.padding(vertical = 8.dp))
            Text(text = house.totalWallArea.toString(), style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.padding(vertical = 8.dp))
            Text(
                text = house.totalWindowMetre.toString(),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialsListScreenInternal(
    houses: List<House>,
    onHouseClick: (House) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Материалы") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,

                    // Цвет текста и иконок
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,

                    // Если есть навигационная иконка
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,

                    // Если есть действия (actions)
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (houses.isEmpty()) {
                Text(text = "У вас нет объектов")
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(houses, key = { it.id }) { house ->
                        MaterialListItem(
                            house = house,
                            onClick = {
                                onHouseClick(house)
                            })
                    }
                }
            }

        }
    }
}