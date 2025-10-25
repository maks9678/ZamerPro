package com.example.zamerpro.Price

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.zamerpro.Class.House
import com.example.zamerpro.Class.Room
import com.example.zamerpro.Dao.AppDatabase
import com.example.zamerpro.Dao.HomeDao
import com.example.zamerpro.Dao.RoomDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class PriceViewModel(
    val idHouse: String,
    private val houseDao: HomeDao,
    private val roomDao: RoomDao
) : ViewModel() {

    val currentHouse: StateFlow<House?>
    val roomsInHouse: StateFlow<List<Room>>

    private val houseAndRooms = houseDao.getHouseWithRooms(idHouse)
val quantityWindows = roomsInHouse.value
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