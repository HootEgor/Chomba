package com.egorhoot.chomba.data

import com.egorhoot.chomba.GameUiState

data class Game(
    var id: String = "",
    var date: Long = 0,
    var playerList: List<Player> = listOf(),
    var uiState: GameUiState = GameUiState(),
)

fun Game.isFinished(): Boolean {
    for (player in playerList){
        if (player.getTotalScore() == 1000){
            return true
        }
    }
    return false
}

fun Game.isWinner(player: Player): Boolean {
    return player.getTotalScore() >= 1000
}

fun Game.totalRound(): Int {
    return playerList.maxOf { it.getMaxRound() }
}

fun Game.getChombaNum(suit: CardSuit): Int {
    return playerList.sumOf { it.getChombaNum(suit) }
}

fun Game.getTotalGain(): Int {
    return playerList.sumOf { it.getTotalGain() }
}

fun Game.getTotalLoss(): Int {
    return playerList.sumOf { it.getTotalLoss() }
}
