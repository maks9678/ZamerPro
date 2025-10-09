package com.example.zamerpro.HomeDao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.zamerpro.House
import com.example.zamerpro.Material
import kotlinx.coroutines.flow.Flow

@Dao
interface MaterialsDao {

    /**
     * Получает ВСЕ материалы для конкретного дома в виде потока (Flow).
     * Это основной метод для отображения списка материалов.
     * Flow автоматически обновит UI, если данные в таблице изменятся.
     * @param houseId ID дома, для которого нужно получить материалы.
     */
    @Query("SELECT * FROM materials WHERE houseId = :houseId ORDER BY name ASC")
    fun getMaterialsForHouse(houseId: String): Flow<List<Material>>

    /**
     * Получает ОДИН конкретный материал по его ID.
     * Полезно для экрана редактирования или детального просмотра материала.
     * @param id Уникальный ID материала.
     */
    @Query("SELECT * FROM materials WHERE id = :id")
    fun getMaterialById(id: Int): Flow<Material>

    /**
     * Добавляет новый материал в базу данных.
     * OnConflictStrategy.IGNORE означает, что если попытаться вставить материал с ID,
     * который уже существует, операция будет проигнорирована.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(material: Material)

    /**
     * Обновляет существующий материал.
     * Room находит нужную запись по PrimaryKey (полю 'id') объекта material.
     */
    @Update
    suspend fun update(material: Material)

    /**
     * Удаляет материал из базы данных.
     */
    @Delete
    suspend fun delete(material: Material)
}