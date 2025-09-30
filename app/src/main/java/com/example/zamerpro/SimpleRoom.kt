package com.example.zamerpro

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Entity(tableName = "houses")
data class House(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val lastModified: Long = System.currentTimeMillis(),
    val totalArea: Int = 0,
    val totalMetre: Int = 0
)
@Parcelize
@Entity(tableName = "rooms")
data class SimpleRoom(
    @PrimaryKey (autoGenerate = true)
    val id: Int = 0,
    val houseId: String ,
    val name: String,
    val area: Int = 0,
    val metre: Int = 0
): Parcelable