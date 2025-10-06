package com.example.zamerpro.HomeDao

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.zamerpro.House
import androidx.room.TypeConverters
import com.example.zamerpro.Converters
import com.example.zamerpro.Opening
import com.example.zamerpro.Room

@Database(entities = [House::class, Room::class, Opening::class], version = 5, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun houseDao(): HomeDao
    abstract fun roomDao(): RoomDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "zamer_pro_database"
                )
                    // 2. УДАЛЯЕМ ВСЕ МИГРАЦИИ
                    // .addMigrations(...)

                    // 3. ДОБАВЛЯЕМ ДЕСТРУКТИВНУЮ МИГРАЦИЮ
                    // Эта команда говорит Room: "При обновлении версии удали старую БД и создай новую".
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}