package com.example.chomba

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.chomba.data.Player
import kotlinx.coroutines.launch

class GameViewModel(application: Application): AndroidViewModel(application) {

    var uiState = mutableStateOf(GameUiState())
        private set

    fun setCurrentPage(page: Int) {
        uiState.value = uiState.value.copy(currentPage = page)
    }

    fun addPlayer() {
        val player = Player()
        player.name = "Player ${uiState.value.playerList.size + 1}"
        uiState.value = uiState.value.copy(playerList = uiState.value.playerList + player)

    }

    fun getNumberOfVisiblePlayers(): Int {
        return uiState.value.playerList.filter { it.visible }.size
    }

    fun removePlayer(player: Player) {
        val updatedPlayerList = uiState.value.playerList.map { existingPlayer ->
            if (existingPlayer == player) {
                existingPlayer.copy(visible = false)
            } else {
                existingPlayer
            }
        }

        uiState.value = uiState.value.copy(playerList = updatedPlayerList)
    }

    fun updatePlayer(player: Player, newName: String, newColor: Color) {
        val updatedPlayerList = uiState.value.playerList.map { existingPlayer ->
            if (existingPlayer == player) {
                existingPlayer.copy(name = newName, color = newColor)
            } else {
                existingPlayer
            }
        }
        uiState.value = uiState.value.copy(playerList = updatedPlayerList)
    }

    fun startGame() {
        viewModelScope.launch {
            val updatedPlayerList = uiState.value.playerList.filter { it.visible }
            uiState.value = uiState.value.copy(playerList = updatedPlayerList)
            setCurrentPage(2)
        }
    }

}