package com.example.zamerpro.materials

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zamerpro.Dao.HomeDao
import com.example.zamerpro.Dao.MaterialsDao
import com.example.zamerpro.Class.House
import com.example.zamerpro.Class.Material
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MaterialsViewModel(
    val houseId: String,
    private val materialsDao: MaterialsDao,
    private val homeDao: HomeDao
) : ViewModel() {

    private val _currentHouse: StateFlow<House?> = homeDao.getHouseByIdFlow(houseId).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        null
    )
    val currentHouse = _currentHouse

    private val _houseMaterials: StateFlow<List<Material>> =
        materialsDao.getMaterialsByIds(currentHouse.value?.listMaterial ?: emptyList())
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val houseMaterials = _houseMaterials

    private val _famousMaterials: StateFlow<List<Material>> =
        materialsDao.getAllMaterials().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000), emptyList()
        )
    val famousMaterials = _famousMaterials

    // UI State для полей ввода
    var newMaterialName by mutableStateOf("")
        private set

    fun onNewMaterialName(newName: String) {
        newMaterialName = newName
    }

    var newMaterialIntake by mutableStateOf(0)
        private set

    fun onNewMaterialIntake(newIntake: Int) {
        newMaterialIntake = newIntake
    }

    var newMaterialUnit by mutableStateOf<MaterialType>(MaterialType.AREA)
        private set

    fun onNewMaterialUnit(newUnit: MaterialType) {
        newMaterialUnit = newUnit
    }

    enum class MaterialType {
        AREA, // Квадратура
        METRE  // Метраж
    }

    fun addNewMaterial() {
        Log.i("MaterialViewModel", "+")
        if (newMaterialName.isNotBlank()) {
            val material = Material(
                name = newMaterialName,
                intake = newMaterialIntake,
                unit = newMaterialUnit,
            )

            viewModelScope.launch {
                val idMaterial = materialsDao.insert(material)
                addMaterialToHouse(idMaterial.toInt())
            }
            newMaterialName = ""
            newMaterialIntake = 0
            newMaterialUnit = MaterialType.AREA
        }
    }

    fun addMaterialToHouse(idMaterial: Int) {

        viewModelScope.launch {
            currentHouse.value?.let { house ->
                val updatedList = house.listMaterial.toMutableList()
                updatedList.add(idMaterial)

                val updatedHouse = house.copy(listMaterial = updatedList)
                homeDao.updateHouse(updatedHouse)
            }
        }
    }

    fun calculation(material: Material): Int {
        val house = currentHouse.value
        return if (house != null) {
            if (material.unit == MaterialType.AREA) material.intake * house.totalWallArea
            else material.intake * house.totalQuantityWindows
        } else 0
    }

    fun editNewMaterial() {
        val material = Material(
            name = newMaterialName,
            intake = newMaterialIntake,
            unit = newMaterialUnit,
        )
        viewModelScope.launch {
            materialsDao.update(material)
        }
    }

    fun removeMaterial(material: Material) {
        viewModelScope.launch {
            materialsDao.delete(material)
        }
    }
}