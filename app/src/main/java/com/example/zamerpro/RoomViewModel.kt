package com.example.zamerpro

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.flow.MutableStateFlow

class RoomViewModel {
    data class RoomState(
        val room: Room? = null,
    )
    private val _roomState = MutableLiveData(RoomState())
    val roomState : LiveData<RoomState>
        get()=_roomState
}