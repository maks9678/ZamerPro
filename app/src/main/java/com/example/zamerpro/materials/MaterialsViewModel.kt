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
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
enum class MaterialType {
    AREA, // Квадратура
    METRE  // Метраж
}
data class MaterialEditorState(
    val id: Int? = null,          // null → ADD, not null → EDIT
    val name: String = "",
    val intake: Int = 0,
    val unit: MaterialType = MaterialType.AREA,
) {
    val isValid: Boolean
        get() = name.isNotBlank() && intake > 0
}
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
        currentHouse.filterNotNull().flatMapLatest { house ->
            materialsDao.getMaterialsByIds(house.listMaterial)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList()
        )

    val houseMaterials = _houseMaterials

    private val _famousMaterials: StateFlow<List<Material>> =
        materialsDao.getAllMaterials().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000), emptyList()
        )
    val famousMaterials = _famousMaterials // известные материалы

    var editorState by mutableStateOf(MaterialEditorState())
        private set
    fun updateName(name: String) {
        editorState = editorState.copy(name = name)
    }

    fun updateIntake(intake: Int) {
        editorState = editorState.copy(intake = intake)
    }

    fun updateUnit(unit: MaterialType) {
        editorState = editorState.copy(unit = unit)
    }

    fun startAddMaterial() {
        editorState = MaterialEditorState()
    }

    fun startEditMaterial(material: Material) {
        editorState = MaterialEditorState(
            id = material.id,
            name = material.name,
            intake = material.intake,
            unit = material.unit
        )
    }
    fun clearEditor() {
        editorState = MaterialEditorState()
    }
    fun saveMaterial() {
        if (!editorState.isValid) return

        viewModelScope.launch {
            if (editorState.id == null) {
                val id = materialsDao.insert(
                    Material(
                        name = editorState.name,
                        intake = editorState.intake,
                        unit = editorState.unit
                    )
                )
                addMaterialToHouse(id.toInt())
            } else {
                materialsDao.update(
                    Material(
                        id = editorState.id!!,
                        name = editorState.name,
                        intake = editorState.intake,
                        unit = editorState.unit
                    )
                )
            }
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
            else material.intake * house.totalWindowMetre
        } else 0
    }

    fun removeMaterial(material: Material) {
        viewModelScope.launch {
            materialsDao.delete(material)
        }
        removeMaterialFromHouse(material.id)
    }

    fun removeMaterialFromHouse(materialId: Int) {
        val house = currentHouse.value ?: return
        val updatedList = house.listMaterial.toMutableList().apply { remove(materialId) }

        val updatedHouse = house.copy(listMaterial = updatedList)
        viewModelScope.launch {
            homeDao.updateHouse(updatedHouse) // обновляем в базе
        }
    }
}