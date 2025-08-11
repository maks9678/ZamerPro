package com.example.zamerpro.HomeDao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.zamerpro.SimpleRoom
import kotlinx.coroutines.flow.Flow

interface RoomDao {
    @Insert(onConflict= OnConflictStrategy.REPLACE)
    suspend fun insertRoom(room:SimpleRoom)

    @Update
    suspend fun updateRoom(room:SimpleRoom)

    @Delete
    suspend fun deleteRoom(room:SimpleRoom)

    @Query("SELECT * FROM rooms WHERE id = :roomId")
    fun getRoomsForHouse(houseId:String):Flow<List<SimpleRoom>>

    @Query("SELECT * FROM rooms WHERE id= :roomId")
    fun getRoomById(roomId:String):Flow<SimpleRoom?>
}