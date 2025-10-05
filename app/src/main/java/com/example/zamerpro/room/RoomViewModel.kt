package com.example.zamerpro.room

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zamerpro.HomeDao.RoomDao
import com.example.zamerpro.ItemDimension
import com.example.zamerpro.Room
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RoomViewModel(
    private val currentHouseId: String,
    private val currentRoomId:Int?,
    private val roomDao: RoomDao
) : ViewModel() {

    // --- Название комнаты ---
    private val _roomName = MutableStateFlow("комната")
    val roomName: StateFlow<String> = _roomName.asStateFlow()

    fun updateRoomName(newName: String) {
        _roomName.value = newName
    }

    // --- Основные параметры комнаты ---
    private val _roomHeight = MutableStateFlow("")
    val roomHeight: StateFlow<String> = _roomHeight.asStateFlow()

    private val _roomWidth = MutableStateFlow("")
    val roomWidth: StateFlow<String> = _roomWidth.asStateFlow()

    private val _roomLength = MutableStateFlow("")
    val roomLength: StateFlow<String> = _roomLength.asStateFlow()

    private val _roomArea = MutableStateFlow(0.0)
    val roomArea: StateFlow<Double> = _roomArea.asStateFlow()

    private val _roomMetre = MutableStateFlow(0.0)
    val roomMetre: StateFlow<Double> = _roomMetre.asStateFlow()


    init {
        // Если это режим редактирования, загружаем данные комнаты
        if (currentRoomId != null) {
            // 3. Используем правильный launch из kotlinx.coroutines
            viewModelScope.launch {
                // Предполагается, что getRoomByIdSuspend есть в RoomDao и возвращает SimpleRoom?
                val room: Room? = roomDao.getRoomByIdSuspend(currentRoomId)
                room?.let { existingRoom -> // Даем переменной осмысленное имя
                    _roomName.value = existingRoom.name
                    // 4. Поля в SimpleRoom у вас, скорее всего, Double, а не String
                    _roomWidth.value = existingRoom.width.toString()
                    _roomLength.value = existingRoom.length.toString()
                    _roomHeight.value = existingRoom.height.toString()
                    // Здесь также нужно загрузить и установить данные для окон, дверей и т.д.
                }
            }
        }
    }
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
        _windows.update { currentWindows -> currentWindows.filterNot { it.id == item.id } }
    }

    fun updateWindowWidth(index: Int, newWidth: String) {
        _windows.update { currentWindows ->
            currentWindows.mapIndexed { i, item ->
                if (i == index) item.copy(width = newWidth) else item
            }
        }
    }

    fun updateWindowHeight(index: Int, newHeight: String) {
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
    fun calculateAndGetSimpleRoom(): Room? {
        val name = _roomName.value.trim()
        val width = _roomWidth.value.toDoubleOrNull()?:0.0
        val length = _roomLength.value.toDoubleOrNull()?:0.0
        val height = _roomHeight.value.toDoubleOrNull()?:0.0
        val houseIdToSave = (currentRoomId ?: 0).toString()

        if (name.isEmpty() ||
            width == null || width <= 0 ||
            length == null || length <= 0 ||
            height == null || height <= 0) {
            return null
        }

        val area = (width * (length + height) * 2)
        val metre =  (_windows.value[currentHouseId.toInt()].height.toFloat() +
                _windows.value[currentHouseId.toInt()].width.toFloat()*2)

        return Room(name = name, houseId =houseIdToSave, area = area, metre = metre) // id сгенерируется по умолчанию
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
        _customWalls.value = emptyList<ItemDimension>()
    }
}