package com.example.zamerpro.Price

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.zamerpro.Class.House
import com.example.zamerpro.Class.Room
import com.example.zamerpro.Dao.AppDatabase
import com.example.zamerpro.Dao.HomeDao
import com.example.zamerpro.Dao.RoomDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class CostItem(
    val name: String,
    val prise: Int,
)

enum class WorkType {
    AREA, // Квадратура
    METRE  // Метраж
}

data class CustomWork(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val type: WorkType,
    val amount: Int, // Количество (квадратура или метраж из объекта)
    val pricePerUnit: Int, // Цена за единицу
    val houseId: String
)

class PriceViewModel(
    val idHouse: String,
    private val houseDao: HomeDao,
    private val roomDao: RoomDao
) : ViewModel() {

    val currentHouse: StateFlow<House?>
    val roomsInHouse: StateFlow<List<Room>>


    private val _listSumWork = MutableStateFlow<List<Int>>(emptyList())
    val listSumWork: StateFlow<List<Int>> = _listSumWork
    
    val sumListWork: StateFlow<Int> = _listSumWork
        .map { it.sumOf { sum -> sum } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )
    
    fun addListSum(sum: Int) {
        _listSumWork.value = _listSumWork.value + sum
    }
    
    fun clearListSum() {
        _listSumWork.value = emptyList()
    }
    private val _listCost = MutableStateFlow<List<CostItem>>(emptyList())
    val listCost: StateFlow<List<CostItem>> = _listCost

    fun addCost(name: String, price: Int) {
        _listCost.value = _listCost.value + CostItem(name, price)
        Log.i("PriceViewModel", "${_listCost.value}")
    }

    fun totalCost(): Int = listCost.value.sumOf { it.prise }
    
    // Кастомные работы
    private val _customWorks = MutableStateFlow<List<CustomWork>>(emptyList())
    val customWorks: StateFlow<List<CustomWork>> = _customWorks
    
    fun addCustomWork(name: String, type: WorkType, pricePerUnit: Int) {
        val house = currentHouse.value ?: return
        val amount = when (type) {
            WorkType.AREA -> house.totalWallArea
            WorkType.METRE -> house.totalWindowMetre
        }
        val work = CustomWork(
            name = name,
            type = type,
            amount = amount,
            pricePerUnit = pricePerUnit,
            houseId = idHouse
        )
        _customWorks.value = _customWorks.value + work
    }
    
    fun removeCustomWork(work: CustomWork) {
        _customWorks.value = _customWorks.value.filterNot { it.id == work.id }
    }
    
    fun getCustomWorksSum(): Int {
        return customWorks.value.sumOf { it.amount * it.pricePerUnit }
    }
    
    private val houseAndRooms = houseDao.getHouseWithRooms(idHouse)

    init {
        currentHouse = houseAndRooms
            .combine(houseDao.getHouseByIdFlow(idHouse)) { houseWithRoom, house ->
                houseWithRoom?.house ?: house
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )
        roomsInHouse = houseAndRooms
            .combine(roomDao.getRoomsForHouseFlow(idHouse)) { houseWithRooms, rooms ->
                houseWithRooms?.rooms ?: rooms
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }

    class PriceViewModelFactory(
        private val application: Application,
        private val idHouse: String
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PriceViewModel::class.java)) {
                val db = AppDatabase.getDatabase(application)
                @Suppress("UNCHECKED_CAST")
                return PriceViewModel(idHouse, db.houseDao(), roomDao = db.roomDao()) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
