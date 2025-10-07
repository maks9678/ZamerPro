package com.example.zamerpro.home

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.zamerpro.HomeDao.AppDatabase
import com.example.zamerpro.HomeDao.HomeDao
import com.example.zamerpro.HomeDao.RoomDao
import com.example.zamerpro.House
import com.example.zamerpro.HouseWithRooms
import com.example.zamerpro.Room
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HouseViewModel(
    private val houseDao: HomeDao,
    private val roomDao: RoomDao,
    private val currentHouseId: String
) : ViewModel() {

    val currentHouse: StateFlow<House?>
    val roomsInHouse: StateFlow<List<Room>>

    private val houseAndRooms = houseDao.getHouseWithRooms(currentHouseId)

    init {
        // 2. ПРЕОБРАЗУЕМ ПОТОКИ ИЗ DAO В STATEFLOW
        currentHouse = houseAndRooms
            .combine(houseDao.getHouseByIdFlow(currentHouseId)) { houseWithRooms, house ->
                houseWithRooms?.house ?: house
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )

        roomsInHouse = houseAndRooms
            .combine(roomDao.getRoomsForHouseFlow(currentHouseId)) { houseWithRooms, rooms ->
                houseWithRooms?.rooms ?: rooms
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }

    fun addRoom(newRoomData: Room) {
        viewModelScope.launch {
            val roomWithHouseId = newRoomData.copy(houseId = currentHouseId)
            roomDao.insertRoom(roomWithHouseId)
            // Обновление дома произойдет автоматически, так как roomsInHouse изменится
            recalculateTotalsAndUpdateHouse()
        }
    }

    fun updateRoom(room: Room) {
        viewModelScope.launch {
            roomDao.updateRoom(room)
            recalculateTotalsAndUpdateHouse()
        }
    }

    fun removeRoom(roomToRemove: Room) {
        viewModelScope.launch {
            roomDao.deleteRoom(roomToRemove)
            recalculateTotalsAndUpdateHouse()
        }
    }

    // 3. УПРОЩЕННЫЙ МЕТОД ПЕРЕСЧЕТА
    private fun recalculateTotalsAndUpdateHouse() {
        viewModelScope.launch {
            val house = currentHouse.value ?: return@launch
            val rooms = roomsInHouse.value

            val newTotalWallArea = rooms.sumOf { it.wallArea }.toInt()
            val newTotalWindowMetre = rooms.sumOf { it.windowMetre }.toInt()

            // 4. ПРОВЕРЯЕМ, НУЖНО ЛИ ОБНОВЛЕНИЕ, ИСПОЛЬЗУЯ ПОЛЯ HOUSE
            if (house.totalWallArea!= newTotalWallArea || house.totalWindowMetre != newTotalWindowMetre) {
                val updatedHouse = house.copy(
                    totalWallArea = newTotalWallArea,
                    totalWindowMetre = newTotalWindowMetre,
                    lastModified = System.currentTimeMillis()
                )
                houseDao.updateHouse(updatedHouse)
            }
        }
    }

    class HouseViewModelFactory(
        private val application: Application,
        private val houseId: String
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HouseViewModel::class.java)) {
                val db = AppDatabase.getDatabase(application)
                @Suppress("UNCHECKED_CAST")
                return HouseViewModel(db.houseDao(), db.roomDao(), houseId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}