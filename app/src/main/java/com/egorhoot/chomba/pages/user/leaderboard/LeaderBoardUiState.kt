package com.egorhoot.chomba.pages.user.leaderboard

import com.egorhoot.chomba.data.LeaderBoardPlayer

data class LeaderBoardUiState (
    val players: List<LeaderBoardPlayer> = listOf(),
    val inProgress: Boolean = false,
)