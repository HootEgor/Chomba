package com.egorhoot.chomba.pages.onlinegame

import com.egorhoot.chomba.data.OnLineGame

data class OnLineGameUiState (
    val game: OnLineGame = OnLineGame(),
    val rooms: List<String> = emptyList(),
    val topBarText: String = "",
)


