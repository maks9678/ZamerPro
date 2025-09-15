package com.example.zamerpro.HomeDao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.zamerpro.House
import kotlinx.coroutines.flow.Flow
@Dao
interface HomeDao {
    @Query("SELECT * FROM houses WHERE id = :houseId")
    fun getHouseByIdFlow(houseId: String): Flow<House?> // Для наблюдения

    @Query("SELECT * FROM houses WHERE id = :houseId")
    suspend fun getHouseByIdSuspend(houseId: String): House? // Для однократного получения

    @Update
    suspend fun updateHouse(house: House)
    @Delete
    suspend fun deleteHouse(house: House)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHouse(house: House)
    @Query("SELECT * FROM houses ORDER BY lastModified DESC")
    fun getAllHouses(): Flow<List<House>>
    @Query("SELECT * FROM houses WHERE name LIKE '%' || :query || '%' ORDER BY lastModified DESC")
    fun searchHousesByName(query: String): Flow<List<House>>
}