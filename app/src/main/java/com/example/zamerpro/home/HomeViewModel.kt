package com.example.zamerpro.home

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.zamerpro.HomeDao.AppDatabase
import com.example.zamerpro.HomeDao.HomeDao
import com.example.zamerpro.HomeDao.RoomDao
import com.example.zamerpro.House
import com.example.zamerpro.SimpleRoom
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HouseViewModel(
    private val houseDao: HomeDao,
    private val roomDao: RoomDao,
    private val currentHouseId: String
) : ViewModel() {

    val currentHouse: StateFlow<House?>
    val roomsInHouse: StateFlow<List<SimpleRoom>>
    val totalArea: StateFlow<Int>
    val totalPerimeter: StateFlow<Int>

    private val _currentHouse = MutableStateFlow<House?>(null)
    private val _roomsInHouse = MutableStateFlow<List<SimpleRoom>>(emptyList())
    private val _totalArea = MutableStateFlow(0)
    private val _totalMetre = MutableStateFlow(0)

    init {
        currentHouse = _currentHouse.asStateFlow()
        roomsInHouse = _roomsInHouse.asStateFlow()
        totalArea = _totalArea.asStateFlow()
        totalPerimeter = _totalMetre.asStateFlow()

        if (currentHouseId.isNotBlank()) {
            viewModelScope.launch {
                houseDao.getHouseByIdFlow(currentHouseId).collectLatest { house ->
                    _currentHouse.value = house
                }
            }
            viewModelScope.launch {
                roomDao.getRoomsForHouseFlow(currentHouseId).collectLatest { rooms ->
                    _roomsInHouse.value = rooms
                    recalculateTotalsAndUpdateHouseIfNeeded() // Единый метод для пересчета и возможного обновления дома
                }
            }
        }
    }

    fun addRoom(newRoomData: SimpleRoom) {
        viewModelScope.launch {
            // Предположим, что SimpleRoom имеет поле houseId, которое нужно установить
            val roomWithHouseId = newRoomData.copy(houseId = currentHouseId) // Убедитесь, что SimpleRoom имеет поле houseId
            roomDao.insertRoom(roomWithHouseId) // Добавляем в БД. Flow из DAO обновит _roomsInHouse.
            // recalculateTotalsAndUpdateHouseIfNeeded() будет вызван автоматически из-за collectLatest на _roomsInHouse
        }
    }

    fun removeRoom(roomToRemove: SimpleRoom) {
        viewModelScope.launch {
            roomDao.deleteRoom(roomToRemove) // Удаляем из БД. Flow из DAO обновит _roomsInHouse.
            // recalculateTotalsAndUpdateHouseIfNeeded() будет вызван автоматически
        }
    }

    private fun recalculateTotalsAndUpdateHouseIfNeeded() {
        val currentRooms = _roomsInHouse.value
        val newTotalArea = currentRooms.sumOf { it.area }
        val newTotalMetre = currentRooms.sumOf { it.metre }

        _totalArea.value = newTotalArea
        _totalMetre.value = newTotalMetre

        // Обновляем данные дома в БД
        val currentHouseValue = _currentHouse.value
        if (currentHouseValue != null &&
            (currentHouseValue.totalArea != newTotalArea || currentHouseValue.totalMetre != newTotalMetre)) {
            viewModelScope.launch { // Запускаем новую корутину для операции с БД
                val updatedHouse = currentHouseValue.copy(
                    totalArea = newTotalArea,
                    totalMetre = newTotalMetre,
                    lastModified = System.currentTimeMillis()
                )
                houseDao.updateHouse(updatedHouse) // Flow из houseDao обновит _currentHouse
            }
        }
    }

    // Фабрика для ViewModel, если она нужна для передачи зависимостей
    // Убедитесь, что Application передается, если он нужен для получения экземпляров Dao
    class HouseViewModelFactory(
        private val application: Application, // Или直接 Dao
        private val houseId: String
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HouseViewModel::class.java)) {
                // Здесь нужно получить экземпляры houseDao и roomDao
                // Например, через AppDatabase.getDatabase(application).houseDao()
                val db = AppDatabase.getDatabase(application) // Предполагается, что у вас есть такой метод
                @Suppress("UNCHECKED_CAST")
                return HouseViewModel(db.houseDao(), db.roomDao(), houseId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}