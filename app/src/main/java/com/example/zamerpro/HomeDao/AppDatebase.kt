package com.example.zamerpro.HomeDao

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.zamerpro.House
import com.example.zamerpro.Room
import com.example.zamerpro.SimpleRoom

@Database(entities=[House::class,SimpleRoom::class],version=1,exportSchema= false)
abstract class AppDatabase :RoomDatabase(){
    abstract fun houseDao():HouseDao
    abstract fun roomDao(): RoomDao
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "zamer_pro_database"
                )
                    // .fallbackToDestructiveMigration() // Если не хотите писать миграции при изменении схемы
                    .build()
                INSTANCE = instance
                instance
            }
        }
}