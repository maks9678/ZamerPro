package com.example.zamerpro.HomeDao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.zamerpro.Opening
import com.example.zamerpro.Room
import com.example.zamerpro.RoomWithObjects
import kotlinx.coroutines.flow.Flow
@Dao
interface RoomDao {
    // --- Существующие методы ---
    @Query("SELECT * FROM rooms WHERE houseId = :houseId")
    fun getRoomsForHouseFlow(houseId: String): Flow<List<Room>>

    @Update
    suspend fun updateRoom(room: Room)

    @Query("SELECT * FROM rooms WHERE id = :roomId")
    suspend fun getRoomByIdSuspend(roomId: Int): Room?

    @Query("SELECT * FROM rooms WHERE houseId = :houseId")
    suspend fun getRoomsForHouseSuspend(houseId: String): List<Room>

    @Delete
    suspend fun deleteRoom(room: Room)

    @Transaction
    @Query("SELECT * FROM rooms WHERE id = :roomId")
    fun getRoomWithObjects(roomId: Int): Flow<RoomWithObjects?>

    // --- НОВЫЕ МЕТОДЫ, КОТОРЫЕ НУЖНО ДОБАВИТЬ ---

    /**
     * Вставляет комнату. Если комната уже существует, заменяет ее.
     * Возвращает ID вставленной или обновленной строки.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoom(room: Room): Long // Изменено: возвращает Long для получения ID

    /**
     * Вставляет один проем (дверь/окно).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOpening(opening: Opening)

    /**
     * Удаляет ВСЕ проемы, связанные с конкретной комнатой.
     * Это нужно для того, чтобы при сохранении сначала очистить старые
     * проемы, а потом вставить обновленный список.
     */
    @Query("DELETE FROM openings WHERE roomId = :roomId")
    suspend fun deleteOpeningsByRoomId(roomId: Int)
}