package com.example.zamerpro.homes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.zamerpro.Dao.AppDatabase
import com.example.zamerpro.Dao.HouseRepository
import com.example.zamerpro.Dao.HouseRepositoryImpl
import com.example.zamerpro.Class.House
import com.example.zamerpro.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HousesListViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val houseRepository: HouseRepository =
        HouseRepositoryImpl(database.houseDao(), database.roomDao())
    private val _showDialog = MutableStateFlow(false)
    val showDialog: StateFlow<Boolean> = _showDialog.asStateFlow()

    fun onShowDialogChange(show: Boolean) {
        _showDialog.value = show
    }


    // Теперь ViewModel работает через Repository, а не напрямую с DAO
    val houses: StateFlow<List<House>> = houseRepository.getAllHouses()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L), // Рекомендуется добавлять 'L' для Long
            initialValue = emptyList()
        )

    fun createNewHouse(
        houseName: String,
        onSuccess: (String) -> Unit,
        onError: (Throwable) -> Unit = {}
    ) {
        viewModelScope.launch {
            when (val result = houseRepository.safeCreateHouse(houseName)) {
                is Result.Success -> {
                    onSuccess(result.data)
                    onShowDialogChange(false)
                }

                is Result.Error -> onError(result.throwable)
                Result.Loading -> Unit
            }
        }
    }

    fun deleteHouse(house: House, onError: (Throwable) -> Unit = {}) {
        viewModelScope.launch {
            when (val result = houseRepository.safeDeleteHouse(house)) {
                is com.example.zamerpro.Result.Success -> Unit
                is com.example.zamerpro.Result.Error -> onError(result.throwable)
                com.example.zamerpro.Result.Loading -> Unit
            }
        }
    }

    // Если вы добавили поиск в ViewModel, используя searchHousesByName из DAO:
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val searchedHouses: StateFlow<List<House>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                houseRepository.getAllHouses() // Показываем все, если запрос пуст
            } else {
                houseRepository.searchHousesByName(query)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
}