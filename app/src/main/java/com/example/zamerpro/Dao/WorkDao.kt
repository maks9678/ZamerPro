package com.example.zamerpro.Dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.zamerpro.Class.Work
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkDao {
    @Query("SELECT * FROM workList " )
    fun getAllWorks(): Flow<List<Work>>

    @Query("SELECT * FROM workList WHERE idWork =:id")
    fun getWork(id:Int): Work

    @Update
    suspend fun updateWork(work:Work)

    @Delete
    suspend fun deleteWork(work:Work)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWork(work: Work)
}