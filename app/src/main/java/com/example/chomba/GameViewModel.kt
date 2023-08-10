package com.example.chomba

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.chomba.data.User
import kotlin.math.log

class GameViewModel(): ViewModel() {

    var uiState = mutableStateOf(GameUiState())
        private set

    fun setCurrentPage(page: Int) {
        uiState.value = uiState.value.copy(currentPage = page)
    }

    fun addUser() {
        val user = User()
        uiState.value = uiState.value.copy(userList = (uiState.value.userList + user).toMutableList())
    }

}