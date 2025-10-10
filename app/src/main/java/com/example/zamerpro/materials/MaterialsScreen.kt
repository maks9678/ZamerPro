package com.example.zamerpro.materials

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.zamerpro.room.RoomViewModel
import com.example.zamerpro.room.TypeRoom

@Composable
fun MaterialsScreen(
    modifier: Modifier = Modifier,
    houseId: String,
    navController: NavController,
) {
    val application = LocalContext.current.applicationContext as Application
    val materialsViewModel: MaterialsViewModel =
        viewModel(factory = MaterialsViewModelFactory(houseId, application))
    val calculatedMaterials = materialsViewModel.calculatedMaterials.collectAsState()
    val customMaterials = materialsViewModel.customMaterials.collectAsState()
    val currentHouse = materialsViewModel.currentHouse.collectAsState()
    val materialList = materialsViewModel.materialList.collectAsState()
    val scope = rememberCoroutineScope()
    MaterialsScreenIternal(
        houseName = currentHouse.value?.name ?: "",
        materialList = materialList.value,
        onRoomTypeSelected = {},
        onRoomNameChange = {}
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewMaterialsScreen() {
    MaterialsScreenIternal(materialList = List(10) {
        Material(
            name = "fsdg",
            quantity = 0,
            unit = "",
            houseId = "0"
        )
    }, onRoomTypeSelected = {}, onRoomNameChange = {})
}

@Composable
fun MaterialsScreenIternal(
    houseName: String = "",
    materialList: List<Material>,
    onRoomTypeSelected: (roomType: TypeRoom) -> Unit,
    onRoomNameChange: (String) -> Unit
) {
    Scaffold() { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(color = MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(vertical = 8.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(materialList, key = { material -> material.name }) { material ->
                MaterialItem(material)
            }
        }
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
        Text(text = material.quantity.toString(), style = MaterialTheme.typography.titleMedium)
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