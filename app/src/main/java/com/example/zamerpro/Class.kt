package com.example.zamerpro

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.UUID

data class Room(
    var width: Int,
    var length: Int,
    var height: Int,
    var windowMetre: Int,
    var priceQuadrature: Int,
    var priceMetre: Int,
    var quantityWindow:Int,
    var quantityDoor:Int
    )
data class Object(
    val rooms: List<Room>
)
class Material(
    val corners: Int,
    val serpyanka: Int,
    val fugen:Int,
    val primer:Int,
    val putty:Int,
    val grindingWheels:Int,
    val extraMaterial:Int,

)

@Parcelize
data class ItemDimension(
    var id: String = UUID.randomUUID().toString(),
    var width: String = "",
    var height: String = ""
) : Parcelable