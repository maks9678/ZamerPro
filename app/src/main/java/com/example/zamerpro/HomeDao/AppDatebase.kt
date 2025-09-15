package com.example.zamerpro.HomeDao

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.zamerpro.House
import com.example.zamerpro.SimpleRoom
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [House::class, SimpleRoom::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun houseDao(): HomeDao
    abstract fun roomDao(): RoomDao // Убедитесь, что RoomDao правильно определен и аннотирован @Dao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Определяем миграцию с версии 1 на версию 2
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Здесь вы должны написать SQL-запросы для изменения схемы.
                // Например, если вы добавили таблицу SimpleRoom в версии 2:
                // db.execSQL("CREATE TABLE IF NOT EXISTS `SimpleRoom` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL)")
                //
                // Если вы добавили новый столбец в существующую таблицу House:
                // db.execSQL("ALTER TABLE House ADD COLUMN new_column_name TEXT")
                //
                // Если вы ничего не меняли в схеме, а просто увеличили версию
                // (например, чтобы добавить новый DAO без изменения таблиц),
                // то тело этого метода может остаться пустым.
                // НО! Если вы ДОБАВИЛИ сущность SimpleRoom, то вам НУЖНО создать для нее таблицу.
                // Предполагая, что SimpleRoom - новая таблица:
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `SimpleRoom` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`name` TEXT NOT NULL)" // Добавьте сюда остальные поля вашей сущности SimpleRoom
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "zamer_pro_database"
                )
                    .addMigrations(MIGRATION_1_2) // <--- ДОБАВЬТЕ МИГРАЦИЮ ЗДЕСЬ
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}