package com.example.zamerpro.Price

import android.app.Application
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.zamerpro.Class.House
import com.example.zamerpro.Class.Supplies
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
    val idWork: Int? = null,
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
        Log.d("Debug", "works=${works.size}; houseWork=${house.listWork.size}")
        works.filter { it.idWork in house.listWork }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sumListWorkInHouse: StateFlow<Int> = listWorksInHouse
        .map { list -> list.sumOf { calculation(it) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    private val _editorState = mutableStateOf(PriceEditorState())
    val editorState: State<PriceEditorState> get() = _editorState

    fun deleteSupplies(supplies: Supplies) {
        val house = currentHouse.value ?: return
        viewModelScope.launch {
            val newHouse = house.copy(
                listSupplies = house.listSupplies.filter {
                    Log.i("PriceViewModel","${it.id}    ${supplies.id}")
                    it.id != supplies.id })
            houseDao.updateHouse(newHouse)

        }
    }

    fun deleteWork(work: Work) {
        viewModelScope.launch {
            workDao.deleteWork(work)
        }
    }

    fun addSupplies(supplies: Supplies) {
        val house = currentHouse.value ?: return
        val updateList = house.listSupplies.orEmpty() + supplies
        val updateHouse = house.copy(listSupplies = updateList)
        viewModelScope.launch {
            houseDao.updateHouse(updateHouse)
        }
    }

    fun startEditPrice(work: Work) {
        _editorState.value = _editorState.value.copy(
            idWork = work.idWork,
            name = work.name,
            priceWork = work.priceWork,
            areaMetreCustom = work.areaMetreCustom,
            customMultiplicand = work.customMultiplicand,
        )
    }

    fun updateTextWork(text: String) {
        _editorState.value = _editorState.value.copy(name = text)
    }

    fun updatePriceWork(price: Int) {
        _editorState.value = _editorState.value.copy(priceWork = price)
    }

    fun updateMultiplicandWork(type: Multiplicand) {
        _editorState.value = _editorState.value.copy(areaMetreCustom = type)
    }

    fun updateCustomWork(number: Int) {
        _editorState.value = _editorState.value.copy(customMultiplicand = number)
    }

    fun clearEditor() {
        _editorState.value = PriceEditorState()
    }

    fun saveWork() {
        viewModelScope.launch {
            val idWork = _editorState.value.idWork
            if (idWork == null) {
                val id = workDao.insertWork(
                    if (_editorState.value.areaMetreCustom == Multiplicand.CUSTOM) {
                        Work(
                            name = _editorState.value.name,
                            priceWork = _editorState.value.priceWork,
                            areaMetreCustom = _editorState.value.areaMetreCustom,
                            customMultiplicand = _editorState.value.customMultiplicand.toInt(),
                        )
                    } else {
                        Work(
                            name = _editorState.value.name,
                            priceWork = _editorState.value.priceWork,
                            areaMetreCustom = _editorState.value.areaMetreCustom,
                        )
                    }
                )
                Log.i("PriceHouse", "создана работа")
                Log.i("PriceHouse", "${listWorksInHouse.value}")
                addWorkToHouse(id.toInt())
            } else {
                workDao.updateWork(
                    Work(
                        idWork= idWork,
                        name = _editorState.value.name,
                        priceWork = _editorState.value.priceWork,
                        areaMetreCustom = _editorState.value.areaMetreCustom,
                        customMultiplicand = _editorState.value.customMultiplicand,
                    )
                )
            }
        }
    }

    fun addWorkToHouse(idWork: Int) {

        viewModelScope.launch {
            currentHouse.value?.let { house ->
                val updatedList = house.listWork.toMutableList()
                updatedList.add(idWork)
                Log.i("PriceHouse", "добавлена работа с id $idWork")
                val updatedHouse = house.copy(listWork = updatedList.toList())
                houseDao.updateHouse(updatedHouse)
            }
        }
        Log.i("PriceHouse", "добавлена работа с id $idWork")
    }

    fun calculation(work: Work): Int {
        val house = _currentHouse.value
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