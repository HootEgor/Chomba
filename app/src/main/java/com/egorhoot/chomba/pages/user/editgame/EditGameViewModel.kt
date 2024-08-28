package com.egorhoot.chomba.pages.user.editgame

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.egorhoot.chomba.ChombaViewModel
import com.egorhoot.chomba.data.Game
import com.egorhoot.chomba.data.Player
import com.egorhoot.chomba.data.Score
import com.egorhoot.chomba.pages.user.ProfileScreenUiState
import com.egorhoot.chomba.repo.UserRepository
import com.egorhoot.chomba.utils.IdConverter
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EditGameViewModel @Inject constructor(
    val userRepo: UserRepository,
    private val idConverter: IdConverter,
    val profileUi: MutableState<ProfileScreenUiState>
) : ChombaViewModel(){
    var uiState = mutableStateOf(EditGameUiState())
        private set

    fun editScoreValue(player: Player, score: Score, value: Int){
        val newScore = score.copy(value = value)
        val newGame = profileUi.value.currentGame!!.copy(
            playerList = profileUi.value.currentGame!!.playerList.map {
                if(it.name == player.name){
                    it.copy(
                        scoreList = it.scoreList.map {
                            if(it == score){
                                newScore
                            } else {
                                it
                            }
                        }
                    )
                } else {
                    it
                }
            }
        )

        profileUi.value = profileUi.value.copy (
            currentGame = newGame
        )
    }

    fun editScoreRound(player: Player, score: Score, round: Int){
        val newScore = score.copy(round = round)
        val newGame = profileUi.value.currentGame!!.copy(
            playerList = profileUi.value.currentGame!!.playerList.map {
                if(it.name == player.name){
                    it.copy(
                        scoreList = it.scoreList.map {
                            if(it == score){
                                newScore
                            } else {
                                it
                            }
                        }
                    )
                } else {
                    it
                }
            }
        )

        profileUi.value = profileUi.value.copy (
            currentGame = newGame
        )
    }

    fun editScoreType(player: Player, score: Score, type: Int){
        val newScore = score.copy(type = type)
        val newGame = profileUi.value.currentGame!!.copy(
            playerList = profileUi.value.currentGame!!.playerList.map {
                if(it.name == player.name){
                    it.copy(
                        scoreList = it.scoreList.map {
                            if(it == score){
                                newScore
                            } else {
                                it
                            }
                        }
                    )
                } else {
                    it
                }
            }
        )

        profileUi.value = profileUi.value.copy (
            currentGame = newGame
        )
    }

    fun editPlayerName(player: Player, name: String){
        val newGame = profileUi.value.currentGame!!.copy(
            playerList = profileUi.value.currentGame!!.playerList.map {
                if(it.name == player.name){
                    it.copy(name = name)
                } else {
                    it
                }
            }
        )

        profileUi.value = profileUi.value.copy (
            currentGame = newGame
        )
    }

    fun saveGame(){
        val game = profileUi.value.currentGame!!
        userRepo.editGame(
            profileUi = profileUi,
            game = game
        )

        profileUi.value = profileUi.value.copy(
            gameList = profileUi.value.gameList.map {
                if(it.id == game.id){
                    game
                } else {
                    it
                }
            }
        )
    }

    fun undoGame(){
        profileUi.value = profileUi.value.copy(
            currentGame = profileUi.value.gameList.find {
                it.id == profileUi.value.currentGame!!.id
            }
        )
    }

}