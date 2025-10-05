package com.example.zamerpro.HomeDao

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.zamerpro.House
import androidx.room.Room
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.zamerpro.Converters
import com.example.zamerpro.Opening

@Database(entities = [House::class, Room::class, Opening::class], version = 4, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun houseDao(): HomeDao
    abstract fun roomDao(): RoomDao // Убедитесь, что RoomDao правильно определен и аннотирован @Dao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Определяем миграцию с версии 1 на версию 2
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE `SimpleRoom_new` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`houseId` TEXT NOT NULL, " + // Правильный тип
                            "`name` TEXT NOT NULL, " +
                            "`area` REAL NOT NULL, " +
                            "`metre` REAL NOT NULL)"
                )
                // 2. Копируем данные из старой таблицы в новую
                db.execSQL(
                    "INSERT INTO `SimpleRoom_new` (id, houseId, name, area, metre) " +
                            "SELECT id, houseId, name, area, metre FROM `SimpleRoom`"
                )
                // 3. Удаляем старую таблицу
                db.execSQL("DROP TABLE `SimpleRoom`")
                // 4. Переименовываем новую таблицу в старую
                db.execSQL("ALTER TABLE `SimpleRoom_new` RENAME TO `simple_rooms`") // Убедитесь, что имя таблицы правильное
            }
        }


        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "zamer_pro_database"
                )
                    .addMigrations(MIGRATION_3_4) // <--- ДОБАВЬТЕ МИГРАЦИЮ ЗДЕСЬ
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
