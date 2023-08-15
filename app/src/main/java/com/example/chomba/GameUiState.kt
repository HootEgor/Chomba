package com.example.chomba

import com.example.chomba.data.Player

data class GameUiState(
    val currentPage: Int = 0,
    val declarer: Player? = null,
    val playerOnBarrel: Player? = null,
    val winner: Player? = null,
    val showScoreList: Boolean = false,
    val showPlayer: Player? = null,
    )
