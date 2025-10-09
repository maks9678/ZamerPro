package com.example.zamerpro.materials

import androidx.lifecycle.ViewModel
import com.example.zamerpro.HomeDao.HomeDao
import com.example.zamerpro.HomeDao.MaterialsDao
import com.example.zamerpro.House
import com.example.zamerpro.Materials

enum class Measurement(val displayName: String, val shortForm: String) {
    METRE("метр", "м"),
    PIECE("штук", "шт"),
    SQUARE_METRE("квадратный метр", "м²"),
    KILOGRAM("килограмм", "кг"),
    LITRE("литр", "л");

    // Метод для получения отображаемого имени с числом
    fun getDisplayNameWithCount(count: Int): String {
        return when (this) {
            METRE -> when {
                count == 1 -> "$count метр"
                count in 2..4 -> "$count метра"
                else -> "$count метров"
            }

            PIECE -> when {
                count == 1 -> "$count штука"
                count in 2..4 -> "$count штуки"
                else -> "$count штук"
            }

            SQUARE_METRE -> when {
                count == 1 -> "$count квадратный метр"
                count in 2..4 -> "$count квадратных метра"
                else -> "$count квадратных метров"
            }

            KILOGRAM -> when {
                count == 1 -> "$count килограмм"
                count in 2..4 -> "$count килограмма"
                else -> "$count килограммов"
            }

            LITRE -> when {
                count == 1 -> "$count литр"
                count in 2..4 -> "$count литра"
                else -> "$count литров"
            }
        }
    }
    fun getShortForm(count: Double): String {
        return "$count $shortForm"
    }
}

class MaterialsViewModel(
    val houseId: String,
    private val materialsDao: MaterialsDao,
    private val homeDao: HomeDao
) : ViewModel() {

    val currentHouse = homeDao.getHouseByIdFlow(houseId)
    val materials = materialsDao.getMaterialsForHouse(houseId)
    fun calculationFugen(currentHouse: House): Int {
        val wallExpenditureFugen = currentHouse.totalWallArea / 100
        val windowExpenditureFugen = currentHouse.totalWindowMetre / 50
        return wallExpenditureFugen + windowExpenditureFugen
    }

    fun calculationPrimer(currentHouse: House): Int {
        val wallExpenditurePrimer = currentHouse.totalWallArea / 100
        return wallExpenditurePrimer
    }

    fun calculationSerpyanka(currentHouse: House): Int {
        val wallExpenditureSerpyanka = currentHouse.totalWallArea * 1.5
        return wallExpenditureSerpyanka.toInt()
    }

    fun calculationPutty(currentHouse: House): Int {
        val wallExpenditurePutty = currentHouse.totalWallArea * 2 / 25
        return wallExpenditurePutty
    }

    fun calculationGrindingWheels(currentHouse: House): Int {
        val wallExpenditureGrindingWheels = currentHouse.totalWallArea / 20
        return wallExpenditureGrindingWheels
    }

    fun getMaterials(currentHouse: House): Materials {
        val material = Materials(
            plasticCorners = 0,
            windowJoining = 0,
            serpyanka = calculationSerpyanka(currentHouse),
            fugen = calculationFugen(currentHouse),
            primer = calculationPrimer(currentHouse),
            putty = calculationPutty(currentHouse),
            grindingWheels = calculationGrindingWheels(currentHouse),
            extraMaterial = 0
        )
        return material
    }
}