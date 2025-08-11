package com.example.zamerpro

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "houses")
data class House(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val lastModified: Long = System.currentTimeMillis())


@Entity(
    tableName = "rooms",
    foreignKeys = [ForeignKey(
        entity = House::class,
        parentColumns = ["id"],
        childColumns = ["houseId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["houseId"])] // Индекс для быстрого поиска комнат по дому
)
data class SimpleRoom(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val houseId: String, // Внешний ключ для связи с House
    val name: String,
    val area: Double,
    val perimeter: Double = 0.0
)