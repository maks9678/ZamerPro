package com.example.zamerpro

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import kotlinx.android.parcel.Parcelize
import java.util.UUID

class Material(
    val corners: Int,
    val serpyanka: Int,
    val fugen:Int,
    val primer:Int,
    val putty:Int,
    val grindingWheels:Int,
    val extraMaterial:Int,
)
enum class OpeningType {
    DOOR, WINDOW
}

@Entity(
    tableName = "openings",
    foreignKeys = [
        ForeignKey(
            entity = Room::class,                // С какой таблицей связываем
            parentColumns = ["id"],              // Поле в родительской таблице (Room)
            childColumns = ["roomId"],           // Поле в этой таблице (Opening)
            onDelete = ForeignKey.CASCADE        // Что делать при удалении комнаты
        )
    ],
    indices = [Index("roomId")]
)
data class Opening(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val roomId: Int,
    val type: OpeningType,
    val width: Double,
    val height: Double
)
@Parcelize
data class ItemDimension(
    var id: String = UUID.randomUUID().toString(),
    var width: String = "",
    var height: String = ""
) : Parcelable

@Entity(tableName = "houses")
data class House(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val lastModified: Long = System.currentTimeMillis(),
)
data class HouseWithRooms(
    @Embedded
    val house: House,

    @Relation(
        parentColumn = "id",      // Поле в House
        entityColumn = "houseId"  // Поле в Room
    )
    val rooms: List<Room>
)
@Parcelize
@Entity(tableName = "rooms")
@TypeConverters(Converters::class)
data class Room(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val houseId: String,
    val name: String,
    val area: Double ,
    val metre: Double ,
    var width: Double,
    var length: Double,
    var height: Double,
    var windowMetre: Double,
    var optionallyArea:List<Double> = emptyList<Double>()
): Parcelable
data class RoomWithObjects(
    @Embedded
    val room: Room,

    @Relation(
        entity = Opening::class, // Указываем, что связываем с Opening
        parentColumn = "id",
        entityColumn = "roomId"
    )
    val openings: List<Opening> // Список всех дверей и окон
)
class Converters {
    @TypeConverter
    fun fromString(value: String?): List<Double> {
        return value?.split(',')?.mapNotNull { it.toDoubleOrNull() } ?: emptyList()
    }

    @TypeConverter
    fun fromList(list: List<Double>): String {
        return list.joinToString(",")
    }
}