package com.example.zamerpro.homes

import android.app.Application
import androidx.activity.result.launch
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.zamerpro.HomeDao.AppDatabase
import com.example.zamerpro.House
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HousesListViewModel (application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application) // Получаем экземпляр БД
    private val houseDao = database.houseDao() // Получаем DAO из экземпляра БД
    private val _showDialog = MutableStateFlow(false)
    val showDialog: StateFlow<Boolean> = _showDialog.asStateFlow()

    fun onShowDialogChange(show: Boolean) {
        _showDialog.value = show
    }


    // Эта часть теперь будет работать правильно, так как getAllHouses() возвращает Flow
    val houses: StateFlow<List<House>> = houseDao.getAllHouses()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L), // Рекомендуется добавлять 'L' для Long
            initialValue = emptyList()
        )

    fun createNewHouse(houseName: String, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            // Предполагается, что конструктор House устанавливает ID и lastModified
            val newHouse = House(name = houseName)
            houseDao.insertHouse(newHouse) // insertHouse теперь suspend и помечен @Insert
            onSuccess(newHouse.id)
        }
    }

    fun deleteHouse(house: House) {
        viewModelScope.launch {
            houseDao.deleteHouse(house)
        }
    }

    // Если вы добавили поиск в ViewModel, используя searchHousesByName из DAO:
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val searchedHouses: StateFlow<List<House>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                houseDao.getAllHouses() // Показываем все, если запрос пуст
            } else {
                houseDao.searchHousesByName(query)
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