package com.example.zamerpro.room

import androidx.compose.ui.input.key.type
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zamerpro.HomeDao.RoomDao
import com.example.zamerpro.ItemDimension
import com.example.zamerpro.Opening
import com.example.zamerpro.OpeningType
import com.example.zamerpro.Room
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RoomViewModel(
    private val currentHouseId: String,
    private val currentRoomId: Int?,
    private val roomDao: RoomDao
) : ViewModel() {

    private val _roomName = MutableStateFlow("")
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

    private val _windows = MutableStateFlow(listOf(ItemDimension()))
    val windows: StateFlow<List<ItemDimension>> = _windows.asStateFlow()

    private val _customWalls = MutableStateFlow(listOf(ItemDimension()))
    val customWalls: StateFlow<List<ItemDimension>> = _customWalls.asStateFlow()

    init {
        // Если это режим редактирования, загружаем данные комнаты
        if (currentRoomId != null) {
            viewModelScope.launch {
                // Предполагается, что roomDao.getRoomWithObjects - это suspend-функция,
                // возвращающая RoomWithObjects? или Flow<RoomWithObjects>
                val roomWithObjects = roomDao.getRoomWithObjects(currentRoomId)?.firstOrNull()
                roomWithObjects?.let { existingData ->
                    val existingRoom = existingData.room
                    _roomName.value = existingRoom.name

                    // ИСПРАВЛЕНО: опечатка heigth -> height
                    _roomHeight.value = existingRoom.height.toString()
                    _roomLength.value = existingRoom.length.toString()
                    _roomWidth.value = existingRoom.width.toString()

                    // Загружаем данные для окон, дверей и т.д.
                    _doors.value = existingData.openings
                        .filter { it.type == OpeningType.DOOR }
                        .map { opening ->
                            // Явно указываем, что ItemDimension находится внутри Room
                            ItemDimension(
                                id = opening.id,
                                width = opening.width.toString(),
                                height = opening.height.toString()
                            )
                        }

                    _windows.value = existingData.openings
                        .filter { it.type == OpeningType.WINDOW }
                        .map { opening ->
                            ItemDimension(
                                id = opening.id,
                                width = opening.width.toString(),
                                height = opening.height.toString()
                            )
                        }

                    _customWalls.value = existingData.openings
                        .filter { it.type == OpeningType.OTHER_METRE }
                        .map { opening ->
                            ItemDimension(
                                id = opening.id,
                                width = opening.width.toString(),
                                height = opening.height.toString()
                            )
                        }
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
     * Собирает все данные, рассчитывает параметры и готовит объекты для сохранения в БД.
     * Возвращает пару: объект Room и список его проемов (Openings).
     * Возвращает null, если основные данные некорректны.
     */
    fun prepareRoomForSave(): Pair<Room, List<Opening>>? {
        val name = _roomName.value.trim()
        val width = _roomWidth.value.toDoubleOrNull()
        val length = _roomLength.value.toDoubleOrNull()
        val height = _roomHeight.value.toDoubleOrNull()

        // 1. Валидация основных полей
        if (name.isEmpty() || width == null || width <= 0 || length == null || length <= 0 || height == null || height <= 0) {
            return null
        }

        // 2. Расчет основных параметров комнаты
        val windowMetre = _windows.value.sumOf { window ->
            val w = window.width.toDoubleOrNull() ?: 0.0
            val h = window.height.toDoubleOrNull() ?: 0.0
            if (w > 0 && h > 0) {
                (h * 2) + w
            } else {
                0.0
            }
        }
        val floorArea = width * length
        val wallArea = (width + length) * 2 * length


        // 3. Создаем объект Room для сохранения
        val roomToSave = Room(
            id = currentRoomId ?: 0, // 0 для новой комнаты, чтобы Room сгенерировал ID
            houseId = currentHouseId, // ID дома, к которому комната принадлежит
            name = name,
            width = width,
            length = length,
            height = height,
            floorArea = floorArea,
            windowMetre = windowMetre,
            wallArea = wallArea
        )

        // 4. Собираем и конвертируем все проемы (двери, окна и т.д.) в список Opening
        val openingsToSave = mutableListOf<Opening>()

        val doorsList = _doors.value
            .mapNotNull { it.toOpening(OpeningType.DOOR, currentRoomId ?: 0) }
        val windowsList = _windows.value
            .mapNotNull { it.toOpening(OpeningType.WINDOW, currentRoomId ?: 0) }
        val customWallsList = _customWalls.value
            .mapNotNull { it.toOpening(OpeningType.OTHER_METRE, currentRoomId ?: 0) }
        val customAreaList = _customWalls.value
            .mapNotNull { it.toOpening(OpeningType.OTHER_AREA, currentRoomId ?: 0) }

        openingsToSave.addAll(doorsList)
        openingsToSave.addAll(windowsList)
        openingsToSave.addAll(customWallsList)
        openingsToSave.addAll(customAreaList)

        return Pair(roomToSave, openingsToSave)
    }

    /**
     * Вспомогательная функция для конвертации UI-состояния (ItemDimension) в сущность БД (Opening).
     * Возвращает null, если данные некорректны.
     */
    private fun ItemDimension.toOpening(type: OpeningType, roomId: Int): Opening? {
        val w = this.width.toDoubleOrNull()
        val h = this.height.toDoubleOrNull()
        if (w == null || w <= 0 || h == null || h <= 0) return null
        return Opening(id = this.id, roomId = roomId, type = type, width = w, height = h)
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
    suspend fun saveRoomData() {
        val dataToSave = prepareRoomForSave() ?: return // Получаем данные или выходим, если они невалидны

        val room = dataToSave.first
        val openings = dataToSave.second

        // Room сама разберется, обновить (update) или вставить (insert) комнату,
        // так как у Room есть @PrimaryKey
        val roomId = roomDao.insertRoom(room).toInt()

        // Сначала удаляем все старые проемы для этой комнаты, чтобы избежать дубликатов
        roomDao.deleteOpeningsByRoomId(roomId)

        // Вставляем новый список проемов
        openings.forEach { opening ->
            // Убедимся, что у проема правильный roomId, особенно для новой комнаты
            roomDao.insertOpening(opening.copy(roomId = roomId))
        }
    }
}