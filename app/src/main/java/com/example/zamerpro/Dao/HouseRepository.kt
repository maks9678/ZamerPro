package com.example.zamerpro.Dao

import com.example.zamerpro.Class.House
import com.example.zamerpro.Class.HouseWithRooms
import com.example.zamerpro.Class.Room
import com.example.zamerpro.Result
import com.example.zamerpro.safeCall
import kotlinx.coroutines.flow.Flow

/**
 * Repository-слой для работы с домами и связанными данными.
 * Сейчас реализован полностью на Room, без серверной части.
 */
interface HouseRepository {
    /**
     * Возвращает поток со списком всех домов, отсортированных по дате изменения.
     */
    fun getAllHouses(): Flow<List<House>>

    /**
     * Поиск домов по части названия.
     */
    fun searchHousesByName(query: String): Flow<List<House>>

    // --- Базовые операции (без обёртки Result) ---

    /**
     * Создает новый дом с указанным именем и возвращает его ID.
     */
    suspend fun createHouse(name: String): String

    /**
     * Удаляет дом.
     */
    suspend fun deleteHouse(house: House)

    // --- Безопасные операции с Result для ViewModel/UI ---

    suspend fun safeCreateHouse(name: String): Result<String>

    suspend fun safeDeleteHouse(house: House): Result<Unit>

    /**
     * Наблюдение за конкретным домом по ID.
     */
    fun getHouseById(houseId: String): Flow<House?>

    /**
     * Наблюдение за домом и его комнатами.
     */
    fun getHouseWithRooms(houseId: String): Flow<HouseWithRooms?>

    /**
     * Поток комнат для конкретного дома.
     */
    fun getRoomsForHouse(houseId: String): Flow<List<Room>>
}

/**
 * Локальная реализация репозитория на основе Room DAO.
 * В дальнейшем сюда можно будет добавить сетевой источник данных и кэширование.
 */
class HouseRepositoryImpl(
    private val homeDao: HomeDao,
    private val roomDao: RoomDao,
) : HouseRepository {

    override fun getAllHouses(): Flow<List<House>> = homeDao.getAllHouses()

    override fun searchHousesByName(query: String): Flow<List<House>> =
        homeDao.searchHousesByName(query)

    override suspend fun createHouse(name: String): String {
        val newHouse = House(name = name)
        homeDao.insertHouse(newHouse)
        return newHouse.id
    }

    override suspend fun deleteHouse(house: House) {
        homeDao.deleteHouse(house)
    }

    override suspend fun safeCreateHouse(name: String): Result<String> =
        safeCall { createHouse(name) }

    override suspend fun safeDeleteHouse(house: House): Result<Unit> =
        safeCall { deleteHouse(house) }

    override fun getHouseById(houseId: String): Flow<House?> =
        homeDao.getHouseByIdFlow(houseId)

    override fun getHouseWithRooms(houseId: String): Flow<HouseWithRooms?> =
        homeDao.getHouseWithRooms(houseId)

    override fun getRoomsForHouse(houseId: String): Flow<List<Room>> =
        roomDao.getRoomsForHouseFlow(houseId)
}