package com.example.zamerpro.Price

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.zamerpro.Class.House
import com.example.zamerpro.Class.Work
import com.example.zamerpro.Dao.AppDatabase
import com.example.zamerpro.Dao.HomeDao
import com.example.zamerpro.Dao.RoomDao
import com.example.zamerpro.Dao.WorkDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PriceEditorState(
    val idHouse: String = "",
    val name: String = "",
    val priceWork: Int = 0,
    val areaMetreCustom: Multiplicand = Multiplicand.SQUARE,
    val customMultiplicand: Int = 0,
)

class PriceViewModel(
    val idHouse: String,
    private val houseDao: HomeDao,
    private val roomDao: RoomDao,
    private val workDao: WorkDao,
) : ViewModel() {

    val listAllWorks = workDao.getAllWorks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    private val _currentHouse: StateFlow<House?> = houseDao.getHouseByIdFlow(idHouse).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        null
    )
    val currentHouse = _currentHouse
    val listWorksInHouse: StateFlow<List<Work>> = combine(
        listAllWorks,
        currentHouse.filterNotNull()
    ) { works, house ->
        works.filter { it.idWork in house.listWork }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sumListWorkInHouse: StateFlow<Int> = listWorksInHouse
        .map { list -> list.sumOf { calculation(it) } } // map Flow<List<Work>> -> Flow<Double>
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    var editorState by mutableStateOf(PriceEditorState())
        private set

    fun updateWork(work: Work) {
        viewModelScope.launch {
            workDao.updateWork(work)
        }
    }
        fun startAddWork() {
            editorState = PriceEditorState()
        }

        fun startEditPrice(work: Work) {
            editorState = PriceEditorState(
                idHouse = work.idWork.toString(),
                name = work.name,
                areaMetreCustom = work.areaMetreCustom,
                customMultiplicand = work.customMultiplicand,
            )
        }


    fun clearEditor() {
        editorState = PriceEditorState()
    }

    fun saveWork() {
        viewModelScope.launch {
            if (editorState.idHouse == "") {
                val id = workDao.insertWork(
                    Work(
                        name = editorState.name,
                        areaMetreCustom = editorState.areaMetreCustom,
                        customMultiplicand = editorState.customMultiplicand,
                    )
                )
                addWorkToHouse(idHouse.toInt())
            } else {
                workDao.updateWork(
                    Work(
                        idWork = editorState.idHouse.toInt(),
                        name = editorState.name,
                        areaMetreCustom = editorState.areaMetreCustom,
                        customMultiplicand = editorState.customMultiplicand,
                    )
                )
            }
        }
    }

    fun addWorkToHouse(idWork: Int) {

        viewModelScope.launch {
            currentHouse.value?.let { house ->
                val updatedList = house.listMaterial.toMutableList()
                updatedList.add(idWork)

                val updatedHouse = house.copy(listMaterial = updatedList)
                houseDao.updateHouse(updatedHouse)
            }
        }
    }

    fun calculation(work: Work): Int {
        val house = currentHouse.value
        return if (house != null) {
            when (work.areaMetreCustom) {
                Multiplicand.METRE -> work.priceWork * house.totalWindowMetre
                Multiplicand.SQUARE -> work.priceWork * house.totalWallArea
                else -> work.priceWork * work.customMultiplicand
            }
        } else 0
    }


    class PriceViewModelFactory(
        private val application: Application,
        private val idHouse: String
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PriceViewModel::class.java)) {
                val db = AppDatabase.getDatabase(application)
                @Suppress("UNCHECKED_CAST")
                return PriceViewModel(
                    idHouse,
                    db.houseDao(),
                    roomDao = db.roomDao(),
                    db.workDao()
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}