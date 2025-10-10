package com.example.zamerpro.MaterialsList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zamerpro.HomeDao.HomeDao
import com.example.zamerpro.House
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class MaterialsListViewModel(
    val houseDao: HomeDao,
) : ViewModel() {
    val houses: StateFlow<List<House>> = houseDao.getAllHouses().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    fun searchHousesByName(query: String): Flow<List<House>> {
        return houseDao.searchHousesByName(query)
    }
}