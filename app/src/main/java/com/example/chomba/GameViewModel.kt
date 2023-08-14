package com.example.chomba

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.chomba.data.Player
import com.example.chomba.data.Score
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

    fun nextRound() {
        val updatedPlayerList = playerList.value.map { existingPlayer ->
            var type = 1
            if (existingPlayer.scorePerRound == 0) {
                type = 0
            }
            val newScore = Score(existingPlayer.scorePerRound, type)
            existingPlayer.copy(scoreList = (existingPlayer.scoreList + newScore),
                scorePerRound = 0)
        }

        playerList.value = updatedPlayerList
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