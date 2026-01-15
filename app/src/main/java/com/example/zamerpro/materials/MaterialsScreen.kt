package com.example.zamerpro.materials

import android.app.Application
import android.util.Log
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.zamerpro.Class.House
import com.example.zamerpro.Dao.AppDatabase
import com.example.zamerpro.Class.Material
import com.example.zamerpro.materials.MaterialsViewModel.MaterialType

const val MATERIAL_SCREEN_ROUTE = "materialScreen"


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
    val _houseState = houseState
    val materialsList by materialsViewModel.houseMaterials.collectAsState()
    val allMaterials by materialsViewModel.famousMaterials.collectAsState()
    if (_houseState != null) {
        MaterialsScreenIternal(
            _houseState.name,
            _houseState,
            materialsList,
            materialsViewModel::addNewMaterial,
            materialsViewModel::editNewMaterial,
            materialsViewModel::removeMaterialFromHouse,
            materialsViewModel::calculation,
            materialsViewModel.newMaterialName,
            materialsViewModel.newMaterialIntake,
            materialsViewModel.newMaterialUnit,
            materialsViewModel::onNewMaterialName,
            materialsViewModel::onNewMaterialIntake,
            materialsViewModel::onNewMaterialUnit,
            allMaterials,
            materialsViewModel::addMaterialToHouse
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMaterialsScreen() {
    MaterialsScreenIternal(
        "Дом",
        House(name = "fdg"),
        listOf
            (
            Material(2, "fsdg", MaterialType.AREA, 12),
            Material(3, "fsdfdg", MaterialType.AREA, 112)
        ),
        {},
        {},
        {},
        { 0 },
        "dsfds",
        33,
        MaterialType.AREA,
        {},
        {},
        {},
        emptyList(),

        {}
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewMaterialsItem() {
    MaterialItem(
        Material(
            name = "serpyanca",
            intake = 1,
            unit = MaterialType.AREA,
        ),
        Modifier.fillMaxSize(),
        { 0 }, {}, {}
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialsScreenIternal(
    houseName: String,
    house: House,
    calculatedMaterials: List<Material>,
    onAddMaterialClick: () -> Unit,
    onEditMaterialClick: (Material) -> Unit,
    onRemoveMaterialFromHouse: (Int) -> Unit,
    calculated: (Material) -> Int,
    newMaterialName: String,
    newMaterialIntake: Int,
    newMaterialUnit: MaterialType,
    onNewMaterialName: (String) -> Unit,
    onNewMaterialIntake: (Int) -> Unit,
    onNewMaterialUnit: (MaterialType) -> Unit,
    allMaterials: List<Material>,
    onAddMaterialHouseClick: (Int) -> Unit,

    ) {
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            Box(modifier = Modifier.fillMaxWidth()) {
                Button(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp),
                    onClick = { showAddDialog = true }) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = "Добавить материал",
                        modifier = Modifier.size(18.dp)
                    )
                    Text("Добавить")
                }
            }
        })
    { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(vertical = 8.dp, horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                stickyHeader {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth().padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Название", modifier = Modifier.weight(1f))
                        Text("Расход", modifier = Modifier.weight(1f))
                        Text("Кол-во", modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(100.dp))
                    }
                }
                if (calculatedMaterials.isNotEmpty()) {
                    items(calculatedMaterials, key = { it.id }) { material ->
                        // Новый Composable для этого типа данных
                        MaterialItem(
                            material,
                            modifier = Modifier,
                            totalMaterial = calculated,
                            onEditMaterialClick = {
                                showAddDialog = true
                                onEditMaterialClick(material)
                            },
                            { onRemoveMaterialFromHouse(material.id) },
                        )
                    }
                }
            }
        }
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Добавить материал") },
                text = {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newMaterialName,
                            onValueChange = { onNewMaterialName(it) },
                            label = { Text("Название материала") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = if (newMaterialIntake == 0) "" else newMaterialIntake.toString(),
                            onValueChange = { input ->
                                val number = input.toIntOrNull() ?: 0
                                onNewMaterialIntake(number)
                            },
                            label = { Text("расход на 1 кв/м ") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier.fillMaxWidth()

                        ) {
                            SegmentedButton(
                                selected = newMaterialUnit == MaterialType.AREA,
                                onClick = {
                                    onNewMaterialUnit(MaterialType.AREA)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("на квадратуру")
                            }
                            SegmentedButton(
                                selected = newMaterialUnit == MaterialType.METRE,
                                onClick = {
                                    onNewMaterialUnit(MaterialType.METRE)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("на метраж")
                            }
                        }

                        Text(
                            text = "Количество: ${
                                if (newMaterialUnit == MaterialType.AREA) house.totalWallArea
                                else house.totalWindowMetre
                            } ${if (newMaterialUnit == MaterialType.AREA) "м²" else "м"}",
                            style = MaterialTheme.typography.bodySmall
                        )

                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 128.dp),
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            val listMaterial: List<Material> = allMaterials.filter{
                                material->
                                material.id !in house.listMaterial
                            }
                            items(listMaterial) { item ->
                                Button(onClick = { onAddMaterialHouseClick(item.id) }) {
                                    Text(text = item.name)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newMaterialName.isNotBlank() && newMaterialIntake > 0) {
                                onAddMaterialClick()
                                Log.i("MaterialScreen", "+")
                                showAddDialog = false
                            }
                        }
                    ) {
                        Text("Добавить")
                    }
                },
                dismissButton = {
                    Button(onClick = { showAddDialog = false }) {
                        Text("Отмена")
                    }
                }
            )
        }
    }
}


@Composable
fun MaterialItem(
    material: Material,
    modifier: Modifier,
    totalMaterial: (Material) -> Int,
    onEditMaterialClick: (Material) -> Unit,
    onRemoveMaterial: (Material) -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(modifier = Modifier.weight(1f),text = material.name, style = MaterialTheme.typography.bodyLarge)
        Text(modifier = Modifier.weight(1f),text = material.intake.toString(), style = MaterialTheme.typography.bodyLarge)
        Text(modifier = Modifier.weight(1f),
            text = totalMaterial(material).toString(),
            style = MaterialTheme.typography.bodyLarge
        )
        Box(modifier = Modifier.weight(1f)) {
            Row {
                IconButton(
                    onClick = { onEditMaterialClick(material) },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(
                        Icons.Filled.Create,
                        contentDescription = "Редактировать объект",
                        tint = MaterialTheme.colorScheme.secondary

                    )
                }
                IconButton(
                    onClick = { onRemoveMaterial(material) },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
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