package com.example.zamerpro.HomeDao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.zamerpro.House
import kotlinx.coroutines.flow.Flow

interface HomeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHouse(house:House)

    @Update
    suspend fun updateHouse(house:House)

    @Delete
    suspend fun deleteHouse(house:House)

    @Query("SELECT * FROM houses ORDER BY lastModified DESC")
    fun getAllHouses():Flow<List<House>>

    @Query("SELECT * FROM houses WHERE id = :houseId")
    fun getHouseById(houseId:String):Flow<House?>
}