package com.example.zamerpro.materials

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.zamerpro.HomeDao.AppDatabase
import com.example.zamerpro.Material

@Composable
fun MaterialsScreen(
    modifier: Modifier = Modifier,
    houseId: String,
    navController: NavController,
) {
    val application = LocalContext.current.applicationContext as Application
    val materialsViewModel: MaterialsViewModel =
        viewModel(factory = MaterialsViewModelFactory(houseId, application))

    val houseState by materialsViewModel.currentHouse.collectAsState()
    val calculatedMaterials by materialsViewModel.calculatedMaterials.collectAsState()
    val customList by materialsViewModel.customMaterials.collectAsState()
    MaterialsScreenIternal(
        houseName = houseState?.name ?: "",
        calculatedMaterials = calculatedMaterials,
        customMaterials = customList
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewMaterialsScreen() {
    MaterialsScreenIternal("Дом", listOf(CalculatedMaterial("fsdg", "fsdg")), listOf())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialsScreenIternal(
    houseName: String,
    calculatedMaterials: List<CalculatedMaterial>,
    customMaterials: List<Material>
) {
    Scaffold(
        topBar = { TopAppBar(modifier = Modifier.fillMaxWidth(), title = { Text(text = houseName) }) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(color = MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(vertical = 8.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (calculatedMaterials.isNotEmpty()) {
                item {
                    Text(
                        "Рассчитанные материалы",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                items(calculatedMaterials, key = { it.name }) { material ->
                    // Новый Composable для этого типа данных
                    CalculatedMaterialItem(material)
                }
            }

            // --- Секция добавленных вручную материалов ---
            if (customMaterials.isNotEmpty()) {
                item {
                    Text(
                        "Добавленные вручную",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = 16.dp) // Отступ между секциями
                    )
                }
                items(customMaterials, key = { it.id }) { material ->
                    // Ваш существующий MaterialItem
                    MaterialItem(material)
                }
            }

            // --- Сообщение, если оба списка пусты ---
            if (calculatedMaterials.isEmpty() && customMaterials.isEmpty()) {
                item {
                    Text("Для этого объекта еще нет материалов.")
                }
            }
        }
    }
}

@Composable
fun CalculatedMaterialItem(material: CalculatedMaterial) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = material.name, style = MaterialTheme.typography.bodyLarge)
        Text(text = material.value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMaterialsItem() {
    MaterialItem(Material(name = "fsdg", quantity = 0, unit = "", houseId = "0"))
}

@Composable
fun MaterialItem(material: Material) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(text = material.name, style = MaterialTheme.typography.titleMedium)
        Text(
            text = material.quantity.toString(),
            style = MaterialTheme.typography.titleMedium
        )
        Text(text = material.unit, style = MaterialTheme.typography.titleMedium)
    }
}

class MaterialsViewModelFactory(
    private val houseId: String,
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MaterialsViewModel::class.java)) {
            val db = AppDatabase.getDatabase(application)
            @Suppress("UNCHECKED_CAST")
            return MaterialsViewModel(houseId, db.materialsDao(), db.houseDao()) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}