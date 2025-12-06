package com.example.zamerpro.Dao

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.zamerpro.Class.House
import androidx.room.TypeConverters
import com.example.zamerpro.Class.Converters
import com.example.zamerpro.Class.Material
import com.example.zamerpro.Class.Opening
import com.example.zamerpro.Class.Room

@Database(entities = [House::class, Room::class, Opening::class, Material::class], version = 8, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun houseDao(): HomeDao
    abstract fun roomDao(): RoomDao
abstract fun materialsDao(): MaterialsDao

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