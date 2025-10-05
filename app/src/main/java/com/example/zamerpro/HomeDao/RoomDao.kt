package com.example.zamerpro.HomeDao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.zamerpro.Room
import com.example.zamerpro.RoomWithObjects
import kotlinx.coroutines.flow.Flow
@Dao
interface RoomDao {
    @Query("SELECT * FROM rooms WHERE houseId = :houseId")
    fun getRoomsForHouseFlow(houseId: String): Flow<List<Room>>

    @Update
    suspend fun updateRoom(room: Room)

    @Query("SELECT * FROM rooms WHERE id = :roomId")
    suspend fun getRoomByIdSuspend(roomId: Int): Room?

    @Query("SELECT * FROM rooms WHERE houseId = :houseId")
    suspend fun getRoomsForHouseSuspend(houseId: String): List<Room>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoom(room: Room)

    @Delete
    suspend fun deleteRoom(room: Room)

    @Transaction
    @Query("SELECT * FROM rooms WHERE id = :roomId")
    fun getRoomWithObjects(roomId: Int): Flow<RoomWithObjects>
}