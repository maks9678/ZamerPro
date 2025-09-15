package com.example.zamerpro.HomeDao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.zamerpro.SimpleRoom
import kotlinx.coroutines.flow.Flow
@Dao
interface RoomDao {
    @Query("SELECT * FROM rooms WHERE houseId = :houseId")
    fun getRoomsForHouseFlow(houseId: String): Flow<List<SimpleRoom>> // Для наблюдения

    @Query("SELECT * FROM rooms WHERE houseId = :houseId")
    suspend fun getRoomsForHouseSuspend(houseId: String): List<SimpleRoom> // Для однократного получения

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoom(room: SimpleRoom)

    @Delete
    suspend fun deleteRoom(room: SimpleRoom)
}