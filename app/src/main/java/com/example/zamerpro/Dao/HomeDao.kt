package com.example.zamerpro.Dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Ignore
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.zamerpro.Class.House
import com.example.zamerpro.Class.HouseWithRooms
import com.example.zamerpro.Class.Supplies
import kotlinx.coroutines.flow.Flow

@Dao
interface HomeDao {
    @Query("SELECT * FROM houses WHERE id = :houseId")
    fun getHouseByIdFlow(houseId: String): Flow<House?> // Для наблюдения

    @Query("SELECT * FROM houses WHERE id = :houseId")
    suspend fun getHouseByIdSuspend(houseId: String): House? // Для однократного получения

    @Update(onConflict = OnConflictStrategy.IGNORE )
    suspend fun updateHouse(house: House)

    @Delete
    suspend fun deleteHouse(house: House)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHouse(house: House)

    @Query("SELECT * FROM houses ORDER BY lastModified DESC")
    fun getAllHouses(): Flow<List<House>>

    @Query("SELECT * FROM houses WHERE name LIKE '%' || :query || '%' ORDER BY lastModified DESC")
    fun searchHousesByName(query: String): Flow<List<House>>


    /**
     * Получает дом и все связанные с ним комнаты одним запросом.
     * @Transaction гарантирует, что чтение из обеих таблиц произойдет атомарно.
     */
    @Transaction
    @Query("SELECT * FROM houses WHERE id = :houseId")
    fun getHouseWithRooms(houseId: String): Flow<HouseWithRooms?>

}