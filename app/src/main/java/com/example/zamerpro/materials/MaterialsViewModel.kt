package com.example.zamerpro.materials

import androidx.lifecycle.ViewModel
import com.example.zamerpro.HomeDao.HomeDao
import com.example.zamerpro.HomeDao.MaterialsDao
import com.example.zamerpro.House
import com.example.zamerpro.Material

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

    fun getMaterials(currentHouse: House): Material {
        val material = Material(
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