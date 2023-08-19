package com.example.chomba.data

import com.example.chomba.GameUiState
import java.util.Date

data class Game(
    var id: String = "",
    var date: Long = 0,
    var playerList: List<Player> = listOf(),
    var uiState: GameUiState = GameUiState(),
)
