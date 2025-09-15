package com.example.zamerpro.home

import android.app.Application
import androidx.activity.result.launch
import androidx.compose.animation.core.copy
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class HouseViewModel(
    private val houseDao: HomeDao,
    private val roomDao: RoomDao,
    private val currentHouseId: String
) : ViewModel() {

    // Переименовываем обратно или предоставляем алиасы, ожидаемые HouseScreen
    val currentHouse: StateFlow<House?> // Вместо houseDetails
    // roomsInHouse уже совпадает
    val roomsInHouse: StateFlow<List<SimpleRoom>>
    val totalArea: StateFlow<Double> // Вместо uiTotalArea
    val totalPerimeter: StateFlow<Double> // Вместо uiTotalPerimeter

    // Внутренние StateFlow для управления
    private val _currentHouse = MutableStateFlow<House?>(null)
    private val _roomsInHouse = MutableStateFlow<List<SimpleRoom>>(emptyList())
    private val _totalArea = MutableStateFlow(0.0)
    private val _totalPerimeter = MutableStateFlow(0.0)

    init {
        currentHouse = _currentHouse.asStateFlow()
        roomsInHouse = _roomsInHouse.asStateFlow()
        totalArea = _totalArea.asStateFlow()
        totalPerimeter = _totalPerimeter.asStateFlow()

        if (currentHouseId.isNotBlank()) {
            viewModelScope.launch {
                houseDao.getHouseByIdFlow(currentHouseId).collectLatest { house ->
                    _currentHouse.value = house
                    // Если HouseScreen ожидает, что totalArea/Perimeter приходят из currentHouse,
                    // то нужно обновлять _totalArea и _totalPerimeter здесь,
                    // либо HouseScreen должен сам их извлекать из currentHouse.value.
                    // Если HouseScreen ожидает, что ViewModel предоставляет отдельные totalArea/Perimeter,
                    // то эти значения должны быть синхронизированы с изменениями комнат ИЛИ с currentHouse.
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
        val newTotalPerimeter = currentRooms.sumOf { it.perimeter }

        _totalArea.value = newTotalArea
        _totalPerimeter.value = newTotalPerimeter

        // Обновляем данные дома в БД
        val currentHouseValue = _currentHouse.value
        if (currentHouseValue != null &&
            (currentHouseValue.totalArea != newTotalArea || currentHouseValue.totalPerimeter != newTotalPerimeter)) {
            viewModelScope.launch { // Запускаем новую корутину для операции с БД
                val updatedHouse = currentHouseValue.copy(
                    totalArea = newTotalArea,
                    totalPerimeter = newTotalPerimeter,
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