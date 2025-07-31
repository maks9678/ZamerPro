package com.example.zamerpro.room

import androidx.lifecycle.ViewModel
import com.example.zamerpro.home.SimpleRoom
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class RoomViewModel : ViewModel() {

    // --- Название комнаты ---
    private val _roomName = MutableStateFlow("")
    val roomName: StateFlow<String> = _roomName.asStateFlow()

    fun updateRoomName(newName: String) {
        _roomName.value = newName
    }

    // --- Основные параметры комнаты ---
    private val _roomHeight = MutableStateFlow("") // Высота пока не используется в SimpleRoom, но оставим
    val roomHeight: StateFlow<String> = _roomHeight.asStateFlow()

    private val _roomWidth = MutableStateFlow("")
    val roomWidth: StateFlow<String> = _roomWidth.asStateFlow()

    private val _roomLength = MutableStateFlow("")
    val roomLength: StateFlow<String> = _roomLength.asStateFlow()

    fun updateRoomHeight(newHeight: String) {
        _roomHeight.value = newHeight
    }

    fun updateRoomWidth(newWidth: String) {
        _roomWidth.value = newWidth
    }

    fun updateRoomLength(newLength: String) {
        _roomLength.value = newLength
    }

    // --- Двери ---
    private val _doors = MutableStateFlow(listOf(ItemDimension()))
    val doors: StateFlow<List<ItemDimension>> = _doors.asStateFlow()

    fun addDoor() {
        _doors.update { currentDoors -> currentDoors + ItemDimension() }
    }

    fun removeDoor(item: ItemDimension) {
        _doors.update { currentDoors -> currentDoors.filterNot { it.id == item.id } }
    }

    fun updateDoorWidth(index: Int, newWidth: String) {
        _doors.update { currentDoors ->
            currentDoors.mapIndexed { i, item ->
                if (i == index) item.copy(width = newWidth) else item
            }
        }
    }

    fun updateDoorHeight(index: Int, newHeight: String) {
        _doors.update { currentDoors ->
            currentDoors.mapIndexed { i, item ->
                if (i == index) item.copy(height = newHeight) else item
            }
        }
    }

    // --- Окна ---
    private val _windows = MutableStateFlow(listOf(ItemDimension()))
    val windows: StateFlow<List<ItemDimension>> = _windows.asStateFlow()

    fun addWindow() {
        _windows.update { currentWindows -> currentWindows + ItemDimension() }
    }

    fun removeWindow(item: ItemDimension) {
        // !!! Была ошибка: вызывался removeCustomWall вместо removeWindow !!!
        _windows.update { currentWindows -> currentWindows.filterNot { it.id == item.id } }
    }

    fun updateWindowWidth(index: Int, newWidth: String) {
        // !!! Была ошибка: вызывался updateCustomWallWidth вместо updateWindowWidth !!!
        _windows.update { currentWindows ->
            currentWindows.mapIndexed { i, item ->
                if (i == index) item.copy(width = newWidth) else item
            }
        }
    }

    fun updateWindowHeight(index: Int, newHeight: String) {
        // !!! Была ошибка: вызывался updateCustomWallHeight вместо updateWindowHeight !!!
        _windows.update { currentWindows ->
            currentWindows.mapIndexed { i, item ->
                if (i == index) item.copy(height = newHeight) else item
            }
        }
    }

    // --- Дополнительные стены ---
    private val _customWalls = MutableStateFlow(listOf(ItemDimension()))
    val customWalls: StateFlow<List<ItemDimension>> = _customWalls.asStateFlow()

    fun addCustomWall() {
        _customWalls.update { currentCustomWalls -> currentCustomWalls + ItemDimension() }
    }

    fun removeCustomWall(item: ItemDimension) {
        _customWalls.update { currentCustomWalls -> currentCustomWalls.filterNot { it.id == item.id } }
    }

    fun updateCustomWallWidth(index: Int, newWidth: String) {
        _customWalls.update { currentCustomWalls ->
            currentCustomWalls.mapIndexed { i, item ->
                if (i == index) item.copy(width = newWidth) else item
            }
        }
    }

    fun updateCustomWallHeight(index: Int, newHeight: String) {
        _customWalls.update { currentCustomWalls ->
            currentCustomWalls.mapIndexed { i, item ->
                if (i == index) item.copy(height = newHeight) else item
            }
        }
    }

    /**
     * Проверяет основные данные, рассчитывает площадь и возвращает SimpleRoom.
     * Возвращает null, если данные некорректны.
     */
    fun calculateAndGetSimpleRoom(): SimpleRoom? {
        val name = _roomName.value.trim()
        val width = _roomWidth.value.toDoubleOrNull()
        val length = _roomLength.value.toDoubleOrNull()

        if (name.isEmpty() || width == null || width <= 0 || length == null || length <= 0) {
            return null // Некорректные данные
        }

        val area = width * length
        return SimpleRoom(name = name, area = area)
    }

    /**
     * Сбрасывает все поля ввода к значениям по умолчанию.
     */
    fun resetAllFields() {
        _roomName.value = ""
        _roomHeight.value = ""
        _roomWidth.value = ""
        _roomLength.value = ""
        _doors.value = listOf(ItemDimension()) // Возвращаем к одному пустому элементу
        _windows.value = listOf(ItemDimension())
        _customWalls.value = listOf(ItemDimension())
    }
}