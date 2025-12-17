package com.example.zamerpro.materials

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.createFromAutofillValue
import androidx.compose.ui.draw.alpha
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
import kotlin.compareTo

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
    if (_houseState != null) {
        MaterialsScreenIternal(
            _houseState.name,
            _houseState,
            materialsList,
            { materialsViewModel::addNewMaterial },
            { materialsViewModel::editMaterial },
            { materialsViewModel::removeMaterial },
            materialsViewModel::calculation,
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
            Material(2, "fsdg", MaterialsViewModel.MaterialType.AREA, 12, ""),
            Material(3, "fsdfdg", MaterialsViewModel.MaterialType.AREA, 112, "")
        ),
        {}, {}, {}, { 0 }
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewMaterialsItem() {
    MaterialItem(
        Material(
            name = "serpyanca",
            intake = 1,
            unit = MaterialsViewModel.MaterialType.AREA,
            houseId = "3"
        ),
        Modifier.fillMaxSize(),
        { 0 }, {}
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
    onRemoveFormulaMaterial: (String) -> Unit,
    calculated: (Material) -> Int,

    ) {
    var showAddDialog by remember { mutableStateOf(false) }
    var materialName by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(MaterialsViewModel.MaterialType.AREA) }
    var intake by remember { mutableStateOf("") }

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
                contentPadding = PaddingValues(vertical = 8.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                stickyHeader{
                    Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Название")
                    Text("Расход")
                    Text("Количество")
                    Text("")
                }}
                if (calculatedMaterials.isNotEmpty()) {
                    items(calculatedMaterials, key = { it.name }) { material ->
                        // Новый Composable для этого типа данных
                        MaterialItem(
                            material,
                            modifier = Modifier.clickable { onEditMaterialClick(material) },
                            totalMaterial = { calculated(material) },
                            onEditMaterialClick
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
                            value = materialName,
                            onValueChange = { materialName = it },
                            label = { Text("Название материала") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = intake,
                            onValueChange = { input ->
                                intake = input.filter { it.isDigit() }
                            },
                            label = { Text("расход на 1 кв/м ") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier.fillMaxWidth()

                        ) {
                            SegmentedButton(
                                selected = selectedType == MaterialsViewModel.MaterialType.AREA,
                                onClick = {
                                    selectedType = MaterialsViewModel.MaterialType.AREA
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("на квадратуру")
                            }
                            SegmentedButton(
                                selected = selectedType == MaterialsViewModel.MaterialType.METRE,
                                onClick = {
                                    selectedType = MaterialsViewModel.MaterialType.METRE
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("на метраж")
                            }
                        }

                        Text(
                            text = "Количество: ${
                                if (selectedType == MaterialsViewModel.MaterialType.AREA) house.totalWallArea
                                else house.totalWindowMetre
                            } ${if (selectedType == MaterialsViewModel.MaterialType.AREA) "м²" else "м"}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (materialName.isNotBlank() && intake.toInt() > 0) {
                                onAddMaterialClick()
                                materialName = ""
                                intake = ""
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
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = material.name, style = MaterialTheme.typography.bodyLarge)
        Text(text = material.intake.toString(), style = MaterialTheme.typography.bodyLarge)
        Text(
            text = totalMaterial(material).toString(),
            style = MaterialTheme.typography.bodyLarge
        )
        IconButton(
            onClick = { onEditMaterialClick(material) },
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