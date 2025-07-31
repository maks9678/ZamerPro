package com.example.zamerpro.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.util.UUID

    data class SimpleRoom(
        val id: String = UUID.randomUUID().toString(),
        val name: String,
        val area: Double // Площадь в квадратных метрах
    )

    class HouseViewModel : ViewModel() {

        // Приватное состояние для списка комнат в доме
        private val _roomsInHouse = MutableStateFlow<List<SimpleRoom>>(emptyList())
        val roomsInHouse: StateFlow<List<SimpleRoom>> = _roomsInHouse.asStateFlow()
        val totalArea: StateFlow<Double> = _roomsInHouse.map { rooms ->
            rooms.sumOf { it.area }
        }.stateIn( // Используем stateIn для корректной работы в viewModelScope
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // Начинаем сбор, когда есть подписчики
            initialValue = 0.0 // Начальное значение
        )
        fun addRoom(room: SimpleRoom) {
            _roomsInHouse.update { currentRooms -> currentRooms + room }
        }

        fun removeRoom(room: SimpleRoom) {
            _roomsInHouse.update { currentRooms -> currentRooms.filterNot { it.id == room.id } }
        }
    }