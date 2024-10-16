package com.egorhoot.chomba

import com.egorhoot.chomba.data.Player

data class GameUiState(

    val declarer: Player? = null,
    val playerOnBarrel: Player? = null,
    val winner: Player? = null,
    val showScoreList: Boolean = false,
    val showPlayer: Player? = null,
    val round: Int = 1,
    val distributorIndex: Int = 0,
    val takePlayerNameList: List<String> = emptyList(),

    val inProgress: Boolean = false,
    val saveMsg: Int = R.string.in_progress,
    )
