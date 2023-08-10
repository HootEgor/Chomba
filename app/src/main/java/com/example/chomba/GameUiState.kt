package com.example.chomba

import com.example.chomba.data.Player

data class GameUiState(
    val currentPage: Int = 0,
    val playerList: List<Player> = listOf()
    )
