package com.example.zamerpro.materials

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zamerpro.HomeDao.HomeDao
import com.example.zamerpro.HomeDao.MaterialsDao
import com.example.zamerpro.Class.House
import com.example.zamerpro.Class.HomeSupplies
import com.example.zamerpro.Class.Material
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CalculatedMaterial(
    val name: String,
    val value: String
)

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
    val materialList: StateFlow<List<Material>> = materialsDao.getMaterialsForHouse(houseId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentHouse = _currentHouse
    val calculatedMaterials: StateFlow<List<CalculatedMaterial>> = _currentHouse.map { house ->
        if (house != null) {
            getCalculatedMaterials(house) // Эта функция теперь возвращает List<CalculatedMaterial>
        } else {
            emptyList()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 2. Поток для ДОБАВЛЕННЫХ ВРУЧНУЮ материалов (без изменений)
    val customMaterials: StateFlow<List<Material>> = materialsDao.getMaterialsForHouse(houseId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    // UI State для полей ввода (без изменений)
    var newMaterialName by mutableStateOf("")
    var newMaterialQuantity by mutableStateOf("")
    var newMaterialUnit by mutableStateOf("")

    fun addNewMaterial() {
        val quantity = newMaterialQuantity.toDoubleOrNull()
        if (newMaterialName.isNotBlank() && quantity != null) {
            val material = Material(
                name = newMaterialName,
                quantity = quantity.toInt(),
                unit = newMaterialUnit.ifBlank { "шт" },
                houseId = this.houseId
            )
            viewModelScope.launch {
                materialsDao.insert(material)
            }
            newMaterialName = ""
            newMaterialQuantity = ""
            newMaterialUnit = ""
        }
    }

    private fun getCalculatedMaterials(currentHouse: House): List<CalculatedMaterial> {
        return listOf(
            CalculatedMaterial("Серпянка", "${calculationSerpyanka(currentHouse)} м"),
            CalculatedMaterial("Фугенфюллер", "${calculationFugen(currentHouse)} мешок(ка)"),
            CalculatedMaterial("Грунтовка", "${calculationPrimer(currentHouse)} л"),
            CalculatedMaterial("Шпаклевка", "${calculationPutty(currentHouse)} мешок(ка)"),
            CalculatedMaterial(
                "Шлифовальные круги",
                "${calculationGrindingWheels(currentHouse)} шт"
            )
        )
    }

    private fun calculationFugen(currentHouse: House): Int {
        val wallExpenditureFugen = currentHouse.totalWallArea / 100
        val windowExpenditureFugen = currentHouse.totalWindowMetre / 50
        return wallExpenditureFugen + windowExpenditureFugen
    }

    private fun calculationPrimer(currentHouse: House): Int {
        val wallExpenditurePrimer = currentHouse.totalWallArea / 100
        return wallExpenditurePrimer
    }

    private fun calculationSerpyanka(currentHouse: House): Int {
        val wallExpenditureSerpyanka = currentHouse.totalWallArea * 1.5
        return wallExpenditureSerpyanka.toInt()
    }

    private fun calculationPutty(currentHouse: House): Int {
        val wallExpenditurePutty = currentHouse.totalWallArea * 2 / 25
        return wallExpenditurePutty
    }

    private fun calculationGrindingWheels(currentHouse: House): Int {
        val wallExpenditureGrindingWheels = currentHouse.totalWallArea / 20
        return wallExpenditureGrindingWheels
    }

}