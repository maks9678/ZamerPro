package com.example.zamerpro.home

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID


// Убедитесь, что SimpleRoom определен и доступен здесь
// (возможно, в отдельном файле data/model)
data class SimpleRoom(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val area: Double,
    val perimeter: Double = 0.0 // Добавим для "метража", если это периметр
)

class HouseViewModel : ViewModel() {

    private val _roomsInHouse = MutableStateFlow<List<SimpleRoom>>(emptyList())
    val roomsInHouse: StateFlow<List<SimpleRoom>> = _roomsInHouse.asStateFlow()

    private val _totalArea = MutableStateFlow(0.0)
    val totalArea: StateFlow<Double> = _totalArea.asStateFlow()

    private val _totalPerimeter = MutableStateFlow(0.0) // Для "метража"
    val totalPerimeter: StateFlow<Double> = _totalPerimeter.asStateFlow()

    fun addRoom(newRoom: SimpleRoom) {
        // Проверка, чтобы не добавить комнату с тем же ID, если она уже есть
        // (на случай если LaunchedEffect сработает несколько раз до удаления из SavedStateHandle)
        if (_roomsInHouse.value.none { it.id == newRoom.id }) {
            _roomsInHouse.update { currentRooms ->
                currentRooms + newRoom
            }
            recalculateTotals()
        }
    }

    fun removeRoom(roomToRemove: SimpleRoom) {
        _roomsInHouse.update { currentRooms ->
            currentRooms.filterNot { it.id == roomToRemove.id }
        }
        recalculateTotals()
    }

    private fun recalculateTotals() {
        val currentRooms = _roomsInHouse.value
        _totalArea.value = currentRooms.sumOf { it.area }
        _totalPerimeter.value = currentRooms.sumOf { it.perimeter }
    }
}
