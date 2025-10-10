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

enum class Measurement(val displayName: String, val shortForm: String) {
    METRE("метр", "м"),
    PIECE("штука", "шт"),
    SQUARE_METRE("квадратный метр", "м²"),
    KILOGRAM("килограмм", "кг"),
    LITRE("литр", "л");
    // Можете добавить сюда другие единицы, если нужно
}
@Entity(
tableName = "materials",
foreignKeys = [
ForeignKey(
entity = House::class,
parentColumns = ["id"],
childColumns = ["houseId"],
onDelete = ForeignKey.CASCADE // Если удалить дом, все его материалы тоже удалятся
)
]
)
data class Material(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,         // Название материала, например "Обои"
    val quantity: Int,     // Количество, например 10.5
    val unit: String,         // Единица измерения, например "рулон" или "кв.м."
    val houseId: String          // Внешний ключ для связи с домом
)
class HomeSupplies(
    val id: String = UUID.randomUUID().toString(),
    val houseId: String,
    val listOfMaterials: List<Material>,
    val plasticCorners: Int,
    val windowJoining:Int,
    val serpyanka: Int,
    val fugen:Int,
    val primer:Int,
    val putty:Int,
    val grindingWheels:Int,
    val extraMaterial:Int,
)
enum class OpeningType {
    DOOR, WINDOW, OTHER_METRE,OTHER_AREA
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
//НАДО ПЕРЕМЕСТИТЬ В КОМНАТУ
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
    val totalWallArea: Int = 0,
    val totalWindowMetre: Int = 0,
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
    var width: Double,
    var length: Double,
    var height: Double,
    var windowMetre: Double,
    val wallArea: Double,// Площадь СТЕН (для обоев): floorPerimeter * height
    val floorArea: Double,// Площадь ПОЛА: width * length
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