package com.example.chomba

import com.example.chomba.data.User

data class GameUiState(
    val currentPage: Int = 0,
    val userList: MutableList<User> = mutableListOf(),
    )
