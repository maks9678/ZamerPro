package com.example.zamerpro.materials

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.wrapContentWidth
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
import androidx.compose.ui.draw.clip
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

const val MATERIAL_SCREEN_ROUTE = "materialScreen"

enum class DialogMode { ADD, EDIT }


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
            _houseState,
            materialsViewModel.editorState,
            materialsList,
            allMaterials,
            materialsViewModel::updateName,
            materialsViewModel::updateIntake,
            materialsViewModel::updateUnit,
            materialsViewModel::startAddMaterial,
            materialsViewModel::startEditMaterial,
            materialsViewModel::clearEditor,
            materialsViewModel::saveMaterial,
            materialsViewModel::removeMaterialFromHouse,
            materialsViewModel::addMaterialToHouse,
            materialsViewModel::calculation,

            )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMaterialsScreen() {
    MaterialsScreenIternal(
        House(name = "fdg"),
        MaterialEditorState(2, "fsdg", 2, MaterialType.AREA),
        listOf
            (
            Material(2, "fsdg", MaterialType.AREA, 12),
            Material(3, "fsdfdg", MaterialType.AREA, 112)
        ),
        listOf
            (
            Material(2, "fsdg", MaterialType.AREA, 12),
            Material(3, "fsdfdg", MaterialType.AREA, 112)
        ),
        {},
        {},
        {},
        {},
        {},
        {},
        {},
        {},
        {},
        { 10 }
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
    house: House,
    editorState: MaterialEditorState,
    calculatedMaterials: List<Material>,
    allMaterials: List<Material>,
    updateName: (String) -> Unit,
    updateIntake: (Int) -> Unit,
    updateUnit: (MaterialType) -> Unit,
    startAddMaterial: () -> Unit,
    startEditMaterial: (Material) -> Unit,
    clearEditor: () -> Unit,
    saveMaterial: () -> Unit,
    removeMaterial: (Int) -> Unit,
    addMaterialToHouse: (Int) -> Unit,
    calculated: (Material) -> Int,

    ) {
    var showDialog by remember { mutableStateOf(false) }
    var dialogMode by remember { mutableStateOf(DialogMode.ADD) }

    Scaffold(
        bottomBar = {
            Box(modifier = Modifier.fillMaxWidth()) {
                Button(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp),
                    onClick = {
                        dialogMode = DialogMode.ADD
                        startAddMaterial()
                        showDialog = true
                    }) {
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
                modifier = Modifier.padding(horizontal = 8.dp).clip(RoundedCornerShape(10.dp)).fillMaxWidth()
                    .background(color =MaterialTheme.colorScheme.surfaceVariant),
                contentPadding = PaddingValues(vertical = 8.dp, horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                stickyHeader {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .clip(RoundedCornerShape(2.dp))
                            ,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Название", modifier = Modifier.weight(1f).wrapContentWidth(Alignment.CenterHorizontally),
                            style = MaterialTheme.typography.titleMedium)
                        Text("Расход", modifier = Modifier.weight(1f).wrapContentWidth(Alignment.CenterHorizontally),
                            style = MaterialTheme.typography.titleMedium)
                        Text("Кол-во", modifier = Modifier.weight(1f).wrapContentWidth(Alignment.CenterHorizontally),
                            style = MaterialTheme.typography.titleMedium)
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
                                dialogMode = DialogMode.EDIT
                                startEditMaterial(material)
                                showDialog = true
                            },
                            { removeMaterial(material.id) },
                        )
                    }
                }
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = {
                    Text(
                        if (dialogMode == DialogMode.ADD)
                            "Добавить материал"
                        else
                            "Редактировать материал",
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = editorState.name,
                            onValueChange = {
                                updateName(it)
                            },
                            label = { Text("Название") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = editorState.intake.takeIf { it > 0 }?.toString() ?: "",
                            onValueChange = {
                                updateIntake(it.toIntOrNull() ?: 0)
                            },
                            label = { Text("расход на 1 кв/м ") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier.fillMaxWidth()

                        ) {
                            SegmentedButton(
                                selected = editorState.unit == MaterialType.AREA,
                                onClick = {
                                    updateUnit(MaterialType.AREA)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("м²")
                            }
                            SegmentedButton(
                                selected = editorState.unit == MaterialType.METRE,
                                onClick = {
                                    updateUnit(MaterialType.METRE)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("метр")
                            }
                        }

                        Text(
                            text = "Количество: ${
                                if (editorState.unit == MaterialType.AREA) house.totalWallArea
                                else house.totalWindowMetre
                            } ${if (editorState.unit == MaterialType.AREA) "м²" else "м"}",
                            style = MaterialTheme.typography.labelMedium
                        )

                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 128.dp),
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            val listMaterial: List<Material> =
                                allMaterials.filter { material ->
                                    material.id !in house.listMaterial
                                }
                            items(listMaterial) { item ->
                                Button(onClick = { addMaterialToHouse(item.id) }) {
                                    Text(text = item.name)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        enabled = editorState.name.isNotBlank() && editorState.intake > 0,
                        onClick = {
                            saveMaterial()
                            showDialog = false
                            clearEditor()
                        }
                    ) {
                        Text(if (dialogMode == DialogMode.ADD) "Добавить" else "Сохранить")
                    }
                },
                dismissButton = {
                    Button(onClick = {
                        clearEditor()
                        showDialog = false
                    }) {
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
        modifier = modifier.fillMaxWidth()
            .clip(RoundedCornerShape(2.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .border(2.dp,MaterialTheme.colorScheme.onPrimaryFixed,
                RoundedCornerShape(10.dp)),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.weight(1f).wrapContentWidth(Alignment.CenterHorizontally),
            text = material.name,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            modifier = Modifier.weight(1f).wrapContentWidth(Alignment.CenterHorizontally),
            text = material.intake.toString(),
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            modifier = Modifier.weight(1f).wrapContentWidth(Alignment.CenterHorizontally),
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