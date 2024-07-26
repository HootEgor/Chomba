package com.egorhoot.chomba.data

import com.egorhoot.chomba.GameUiState

data class Game(
    var id: String = "",
    var date: Long = 0,
    var playerList: List<Player> = listOf(),
    var uiState: GameUiState = GameUiState(),
)
