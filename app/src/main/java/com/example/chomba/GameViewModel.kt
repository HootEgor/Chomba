package com.example.chomba

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.chomba.data.Player
import com.example.chomba.data.Score
import com.example.chomba.data.getMissBarrel
import com.example.chomba.data.getTotalScore
import kotlinx.coroutines.launch

class GameViewModel(application: Application): AndroidViewModel(application) {

    val playerList = mutableStateOf<List<Player>>(listOf())
    var uiState = mutableStateOf(GameUiState())
        private set

    fun setCurrentPage(page: Int) {
        uiState.value = uiState.value.copy(currentPage = page)
    }

    fun addPlayer() {
        val player = Player()
        player.name = "Player ${playerList.value.size + 1}"
        playerList.value = playerList.value + player
    }

    fun getNumberOfVisiblePlayers(): Int {
        return playerList.value.filter { it.visible }.size
    }

    fun removePlayer(player: Player) {
        val updatedPlayerList = playerList.value.map { existingPlayer ->
            if (existingPlayer == player) {
                existingPlayer.copy(visible = false)
            } else {
                existingPlayer
            }
        }

        playerList.value = updatedPlayerList
    }

    fun updatePlayer(player: Player, newName: String, newColor: Color) {
        val updatedPlayerList = playerList.value.map { existingPlayer ->
            if (existingPlayer == player) {
                existingPlayer.copy(name = newName, color = newColor)
            } else {
                existingPlayer
            }
        }
        playerList.value = updatedPlayerList
    }

    fun saveScorePerRound(player: Player, score: Int) {
        val updatedPlayerList = playerList.value.map { existingPlayer ->
            if (existingPlayer == player) {
                existingPlayer.copy(scorePerRound = score)
            } else {
                existingPlayer
            }
        }
        playerList.value = updatedPlayerList
    }

    fun getCurrentRound(): Int {
        return playerList.value[0].scoreList.size + 1
    }

    fun getPlayersName(): List<String> {
        return playerList.value.map { it.name }
    }

    fun setDeclarer(name: String, score: Int) {
        val updatedPlayerList = playerList.value.map { existingPlayer ->
            if (existingPlayer.name == name) {
                existingPlayer.copy(declaration = score)
            } else {
                existingPlayer
            }
        }

        playerList.value = updatedPlayerList
        uiState.value = uiState.value.copy(declarer = playerList.value.find { it.name == name })
    }

    fun nextRound() {

        val updatedPlayerList = playerList.value.map { existingPlayer ->
            var score = existingPlayer.scorePerRound
            var type = 1

            if (existingPlayer.scorePerRound == 0) {
                type = 0
            } else if (existingPlayer.scorePerRound < 0) {
                type = -1
            }

            if(uiState.value.declarer?.name == existingPlayer.name) {

                if(existingPlayer.scorePerRound >= existingPlayer.declaration) {
                    type = 1
                } else {
                    type = 0
                }
                score = existingPlayer.declaration
            }

            if(existingPlayer.getTotalScore() + score >= 880) {
                if(existingPlayer.getTotalScore() >= 880 && score >= 120) {
                    score = 120
                } else {
                    score = 880 - existingPlayer.getTotalScore()
                }
            }


            if(uiState.value.playerOnBarrel?.name == existingPlayer.name) {
                if(type == 0 && existingPlayer.getMissBarrel() < 2) {
                    type = -2
                }
                else if(type != 1)
                    type = -3

                score = 120
            }else if(existingPlayer.getTotalScore() == 880) {
                type = 2
            }

            val newScore = Score(score, type)

            existingPlayer.copy(scoreList = (existingPlayer.scoreList + newScore),
                scorePerRound = 0)

        }

        playerList.value = updatedPlayerList

        playerList.value.map { existingPlayer ->
            if (existingPlayer.getTotalScore() == 1000) {
                setWinner(existingPlayer)
            }
        }

        val playerOnBarrel = getPlayerOnBarrel()
        if (playerOnBarrel != null){
            uiState.value = uiState.value.copy(playerOnBarrel = playerOnBarrel)
            setDeclarer(playerOnBarrel.name, 120)
        }
        else{
            uiState.value = uiState.value.copy(playerOnBarrel = null,
                declarer = null)
        }
    }

    private fun getPlayerOnBarrel(): Player? {

        val scoreListSize = playerList.value[0].scoreList.size
        var playersOnBarrel : List<Player> = listOf()
        for (player in playerList.value){
            if (player.getTotalScore() == 880){
                playersOnBarrel = playersOnBarrel + player
            }
        }

        if (playersOnBarrel.size == 1){
            return playersOnBarrel[0]
        }
        else if (playersOnBarrel.size > 1){
            for (player in playersOnBarrel){
                var count = 0
                for(i in playersOnBarrel.size-1 downTo 1){
                    if (player.scoreList[scoreListSize - i].type == 2 ||
                        player.scoreList[scoreListSize - i].type != -2){
                       count++
                    }
                }
                if (count == playersOnBarrel.size-1){
                    return player
                }
            }
        }

        return null
    }

    fun showScoreList(player: Player?, show: Boolean) {
        uiState.value = uiState.value.copy(showScoreList = show, showPlayer = player)
    }

    fun setWinner(player: Player?) {
        uiState.value = uiState.value.copy(winner = player)
    }

    fun startGame() {
        viewModelScope.launch {
            val updatedPlayerList = playerList.value.filter { it.visible }
            playerList.value = updatedPlayerList
        }.invokeOnCompletion {
            setCurrentPage(2)
        }
    }


}