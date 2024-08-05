package com.egorhoot.chomba.pages.user.leaderboard

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.egorhoot.chomba.ChombaViewModel
import com.egorhoot.chomba.data.Game
import com.egorhoot.chomba.data.LeaderBoardPlayer
import com.egorhoot.chomba.data.Player
import com.egorhoot.chomba.data.getScoreSum
import com.egorhoot.chomba.data.getTotalScore
import com.egorhoot.chomba.data.isFinished
import com.egorhoot.chomba.data.isWinner
import com.egorhoot.chomba.data.sortedByTotalScore
import com.egorhoot.chomba.data.sortedByWinStreak
import com.egorhoot.chomba.data.sortedByWins
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

class LeaderBoardViewModel @Inject constructor(): ChombaViewModel(){

    var uiState = mutableStateOf(LeaderBoardUiState())
        private set

    fun getLeaderBoardPlayers(gameList: List<Game>){
        val playersName = mutableListOf<String>()
        for (game in gameList) {
            if(!game.isFinished())
                continue
            for (player in game.playerList) {
                if(!playersName.contains(player.name)) {
                    playersName.add(player.name)
                }
            }
        }

        val players = mutableListOf<LeaderBoardPlayer>()
        for (playerName in playersName) {
            var wins = 0
            var totalScore = 0
            var winStreak = 0
            var maxWinStreak = 0
            for (game in gameList.reversed()) {
                if(!game.isFinished())
                    continue
                for (player in game.playerList) {
                    if (player.name == playerName) {
                        totalScore += player.getTotalScore()
                        if (game.isWinner(player)){
                            wins += 1
                            winStreak +=1
                        }
                        else{
                            winStreak = 0
                        }
                        if(winStreak > maxWinStreak)
                            maxWinStreak = winStreak

                    }
                }
            }
            players.add(LeaderBoardPlayer(playerName, wins, totalScore, maxWinStreak))
        }

        uiState.value = uiState.value.copy(players = players.sortedByWins())
    }

    fun sortPlayersByWins() {
        uiState.value = uiState.value.copy(players = uiState.value.players.sortedByWins())
    }

    fun sortPlayersByTotalScore() {
        uiState.value = uiState.value.copy(players = uiState.value.players.sortedByTotalScore())
    }

    fun sortPlayersByWinStreak() {
        uiState.value = uiState.value.copy(players = uiState.value.players.sortedByWinStreak())
    }
}