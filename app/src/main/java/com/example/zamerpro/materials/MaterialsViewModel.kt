package com.example.zamerpro.materials

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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.objecthunter.exp4j.ExpressionBuilder

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
        materialsDao.getMaterialsForHouse(houseId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val houseMaterials = _houseMaterials

    // UI State для полей ввода
    var newMaterialName by mutableStateOf("")
    var newMaterialIntake by mutableStateOf(0)
    var newMaterialUnit by mutableStateOf<MaterialType>(MaterialType.AREA)

    enum class MaterialType {
        AREA, // Квадратура
        METRE  // Метраж
    }

    fun addNewMaterial() {
        if (newMaterialName.isNotBlank()) {
            val material = Material(
                name = newMaterialName,
                intake = newMaterialIntake,
                unit = newMaterialUnit,
                houseId = this.houseId
            )
            viewModelScope.launch {
                materialsDao.insert(material)
            }
            newMaterialName = ""
            newMaterialIntake = 0
            newMaterialUnit = MaterialType.AREA
        }
    }

    fun calculation(material: Material): Int {
        val house = currentHouse.value
        if (house != null) {
            return if (material.unit == MaterialType.AREA) material.intake * house.totalWallArea
            else   material.intake * house.totalQuantityWindows
        } else return 0
    }

    fun editMaterial() {
        val material = Material(
            name = newMaterialName,
            intake = newMaterialIntake,
            unit = newMaterialUnit,
            houseId = this.houseId
        )
        viewModelScope.launch {
            materialsDao.update(material)
        }
    }

    fun removeMaterial(material:Material) {
        viewModelScope.launch {
            materialsDao.delete(material)
        }
    }
}