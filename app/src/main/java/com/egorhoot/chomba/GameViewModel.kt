package com.egorhoot.chomba

import android.app.Application
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.egorhoot.chomba.ai.VoiceRecognitionViewModel
import com.egorhoot.chomba.data.CardSuit
import com.egorhoot.chomba.data.Player
import com.egorhoot.chomba.data.Score
import com.egorhoot.chomba.data.chombaScore
import com.egorhoot.chomba.data.getMissBarrel
import com.egorhoot.chomba.data.getTotalScore
import com.egorhoot.chomba.pages.user.ProfileScreenUiState
import com.egorhoot.chomba.pages.user.ProfileViewModel
import com.egorhoot.chomba.pages.user.leaderboard.LeaderBoardViewModel
import com.egorhoot.chomba.repo.UserRepository
import com.egorhoot.chomba.utils.IdConverter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.round

@HiltViewModel
class GameViewModel @Inject constructor(
    val userRepo: UserRepository,
    private val idConverter: IdConverter,
    val profileUi: MutableState<ProfileScreenUiState>
): ChombaViewModel() {

    val playerList = mutableStateOf<List<Player>>(listOf())
    var uiState = mutableStateOf(GameUiState())
        private set

    private fun startProgressGame(){
        uiState.value = uiState.value.copy(inProgress = true,
            saveMsg = R.string.in_progress)
    }

    private fun stopProgressGame(){
        uiState.value = uiState.value.copy(inProgress = false)
    }

    fun newGame() {
        profileUi.value = profileUi.value.copy(currentGameIndex = null)
        playerList.value = listOf()
        uiState.value = GameUiState()
        setCurrentPage(1)
    }

    fun setCurrentPage(page: Int) {
        uiState.value = uiState.value.copy(currentPage = page)

        if(page == 3){
            profileUi.value = profileUi.value.copy(currentScreen = 0)
        }
    }

    fun addPlayer() {
        val player = Player()
        playerList.value += player
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

    fun updatePlayer(player: Player, newName: String, newColor: String) {
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
            if (existingPlayer.name == player.name) {
                existingPlayer.copy(scorePerRound = score)
            } else {
                existingPlayer
            }
        }
        playerList.value = updatedPlayerList
    }

    fun setScorePerRoundD(player: Player){
        val updatedPlayerList = playerList.value.map { existingPlayer ->
            if (existingPlayer.name == player.name) {
                existingPlayer.copy(scorePerRound = existingPlayer.declaration)
            } else {
                existingPlayer
            }
        }
        playerList.value = updatedPlayerList
    }

    fun setBlind(player: Player) {
        val updatedPlayerList = playerList.value.map { existingPlayer ->
            if (existingPlayer.name == player.name ) {
                if(uiState.value.declarer == null){
                    existingPlayer.copy(blind = existingPlayer.blind.not())
                }else if (existingPlayer.name == uiState.value.declarer?.name){
                    existingPlayer.copy(blind = existingPlayer.blind.not())
                }else{
                    existingPlayer
                }

            } else {
                existingPlayer
            }
        }
        playerList.value = updatedPlayerList
    }

    fun getCurrentRound(): Int {
        return uiState.value.round
    }

    fun getPlayersName(): List<String> {
        return playerList.value.map { it.name }
    }

    fun setDeclarer(name: String, score: Int) {
        val updatedPlayerList = playerList.value.map { existingPlayer ->
            if (existingPlayer.name == name) {
                existingPlayer.copy(declaration = score)
            } else {
                existingPlayer.copy(blind = false)
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
            }

            if(uiState.value.declarer?.name == existingPlayer.name
                || uiState.value.playerOnBarrel?.name == existingPlayer.name) {

                type = if(existingPlayer.scorePerRound >= existingPlayer.declaration) {
                    1
                } else {
                    -1
                }
                score = existingPlayer.declaration
                if(existingPlayer.blind)
                    score *= 2
            }

            if(existingPlayer.getTotalScore() + score >= 880 && type != -1) {
                score = if(existingPlayer.getTotalScore() >= 880 && score >= 120) {
                    120
                } else {
                    880 - existingPlayer.getTotalScore()
                }
            }


            if(uiState.value.playerOnBarrel?.name == existingPlayer.name) {
                if(type == -1 && existingPlayer.getMissBarrel() < 2) {
                    type = -2
                    score = existingPlayer.scorePerRound
                }
                else if(type != 1){
                    type = -4
                    score = 120
                }


//                score = 120
            }else if(existingPlayer.getTotalScore() == 880) {
                type = 2
            }

            val newScore = Score(score, type, uiState.value.round, existingPlayer.takenChombas)

            existingPlayer.copy(scoreList = (existingPlayer.scoreList + newScore),
                scorePerRound = 0, blind = false,
                takenChombas = listOf())

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
            setDeclarer(playerOnBarrel.name, 125)
        }
        else{
            uiState.value = uiState.value.copy(playerOnBarrel = null,
                declarer = null)
        }

        uiState.value = uiState.value.copy(round = uiState.value.round + 1)
        uiState.value = uiState.value.copy(distributorIndex = nextDistributorIndex())
    }

    fun makeDissolution(){

        val declarer = playerList.value.find { it.name == uiState.value.declarer?.name }
        val declaration = declarer?.declaration
        val updatedPlayerList = playerList.value.map { existingPlayer ->
            var score = 0
            var type = 3

            if (declaration != null) {
                if(existingPlayer.name == uiState.value.declarer?.name) {
                    score = declaration
                    type = -3
                } else {
                    score = declaration.div(2)
                    score = (round((score / 5).toDouble()) * 5).toInt()
                    if(existingPlayer.getTotalScore() >= 880) {
                        score = 0
                        type = -2
                        if(uiState.value.playerOnBarrel?.name == existingPlayer.name && existingPlayer.getMissBarrel() == 2) {
                            type = -4
                            score = 120
                        }
                    }else if(existingPlayer.getTotalScore() + score >= 880) {
                        score = 880 - existingPlayer.getTotalScore()
                    }
                }
            }

            val newScore = Score(score, type, uiState.value.round)

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
            setDeclarer(playerOnBarrel.name, 125)
        }
        else{
            uiState.value = uiState.value.copy(playerOnBarrel = null,
                declarer = null)
        }

        uiState.value = uiState.value.copy(round = uiState.value.round + 1)
        uiState.value = uiState.value.copy(distributorIndex = nextDistributorIndex())
    }

    fun makePenalty(player: Player){
        val updatedPlayerList = playerList.value.map { existingPlayer ->
            var score = 0

            if(existingPlayer.name == player.name) {
                score = -120
            }

            val newScore = Score(score, 1, uiState.value.round)

            if (score != 0){
                existingPlayer.copy(scoreList = (existingPlayer.scoreList + newScore),
                    scorePerRound = 0)
            }
            else{
                existingPlayer
            }

        }

        playerList.value = updatedPlayerList
    }

    private fun nextDistributorIndex(): Int {
        val index = uiState.value.round % playerList.value.size
        return if (index == 0) playerList.value.size - 1 else index - 1
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
            var countList: List<Int> = listOf()
            for (player in playersOnBarrel){
                var count = 0
                for(i in playersOnBarrel.size-1 downTo 1){
                    if (player.scoreList[scoreListSize - i].type == 2 ||
                        player.scoreList[scoreListSize - i].type == -2){
                       count++
                    }
                }
                countList = countList + count
            }

            val minIndex = countList.indexOf(countList.minOrNull())
            var playerOnBarrel = playersOnBarrel[0]
            //count equal values in countList
            if(countList.count(countList[minIndex]::equals) == playersOnBarrel.size){
                for(player in playersOnBarrel){
                    makePenalty(player)
                }
                return null
            }else{
                for(player in playersOnBarrel){
                    if(playersOnBarrel.indexOf(player) == minIndex){
                        playerOnBarrel = player
                    }else{
                        makePenalty(player)
                    }
                }
            }

            return playerOnBarrel
        }

        return null
    }

    fun showScoreList(player: Player?, show: Boolean) {
        uiState.value = uiState.value.copy(showScoreList = show, showPlayer = player)
    }

    private fun setWinner(player: Player?) {
        uiState.value = uiState.value.copy(winner = player)
        showAlert(profileUi, R.string.winner, "${player?.name}",
            {dismissAlert(profileUi)}, {dismissAlert(profileUi)})
    }

    fun startGame() {
        viewModelScope.launch {
            val updatedPlayerList = playerList.value.filter { it.visible }
            playerList.value = updatedPlayerList
            for (player in playerList.value){
                if (player.name.trim() == ""){
                    showAlert(profileUi, R.string.error, idConverter.getString(R.string.player_name_empty),
                        {dismissAlert(profileUi)}, {dismissAlert(profileUi)})
                    return@launch
                }
            }

            val names = playerList.value.map { it.name }
            if (names.size != names.toSet().size){
                showAlert(profileUi, R.string.error, idConverter.getString(R.string.duplicate_names),
                    {dismissAlert(profileUi)}, {dismissAlert(profileUi)})
                return@launch
            }
            uiState.value = uiState.value.copy(distributorIndex = nextDistributorIndex())
            setCurrentPage(2)
        }
    }

    fun takeChomba(player: Player, suit: Int) {
        val updatedPlayerList = playerList.value.map { existingPlayer ->
            if (existingPlayer.name == player.name) {
                existingPlayer.copy(takenChombas = existingPlayer.takenChombas + CardSuit.values()[suit],
                    scorePerRound = existingPlayer.scorePerRound + chombaScore(suit))
            } else {
                existingPlayer
            }
        }
        playerList.value = updatedPlayerList
    }

    fun undoChomba(player: Player, suit: Int) {
        val updatedPlayerList = playerList.value.map { existingPlayer ->
            if (existingPlayer.name == player.name) {
                existingPlayer.copy(takenChombas = existingPlayer.takenChombas.filter { it.ordinal != suit },
                    scorePerRound = existingPlayer.scorePerRound - chombaScore(suit))
            } else {
                existingPlayer
            }
        }
        playerList.value = updatedPlayerList
    }

    fun saveGame(exit: Boolean = false) {
        viewModelScope.launch {
            startProgressGame()
            userRepo.saveGame(profileUi, playerList, uiState)
        }.invokeOnCompletion {
            stopProgressGame()
            if (exit)
                setCurrentPage(0)
        }

    }

    fun continueGame(){
        val game = profileUi.value.gameList.find { it.id == profileUi.value.currentGameIndex }!!
        viewModelScope.launch {
            playerList.value = game.playerList
            uiState.value = game.uiState
        }.invokeOnCompletion {
            setCurrentPage(2)
        }
    }

    fun isCurrentGameFinished(): Boolean {
        for (player in playerList.value){
            if (player.getTotalScore() == 1000){
                return true
            }
        }
        return false
    }

    fun onUndoLastRound(){
        showAlert(profileUi, R.string.undo_last_round, idConverter.getString(R.string.are_you_sure),
            {undoLastRound()
            dismissAlert(profileUi)},
            {dismissAlert(profileUi)})
    }

    private fun undoLastRound(){
        var updated = false
        val updatedPlayerList = playerList.value.map { existingPlayer ->
            val scoreList = existingPlayer.scoreList
            if (scoreList.isNotEmpty()) {
                updated = true
                existingPlayer.copy(scoreList = scoreList.filter { it.round != uiState.value.round-1 },
                    scorePerRound = 0, blind = false,
                    takenChombas = listOf())
            } else {
                existingPlayer
            }
        }

        if (updated) {
            playerList.value = updatedPlayerList
            uiState.value = uiState.value.copy(round = if (uiState.value.round - 1 < 1) 1 else uiState.value.round - 1)
            uiState.value = uiState.value.copy(distributorIndex = nextDistributorIndex())
        }
    }




}