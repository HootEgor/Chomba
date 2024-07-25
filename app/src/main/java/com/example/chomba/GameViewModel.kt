package com.example.chomba

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.chomba.ai.VoiceRecognitionViewModel
import com.example.chomba.data.CardSuit
import com.example.chomba.data.Player
import com.example.chomba.data.Score
import com.example.chomba.data.getMissBarrel
import com.example.chomba.data.getTotalScore
import com.example.chomba.pages.user.ProfileViewModel
import kotlinx.coroutines.launch
import kotlin.math.round

class GameViewModel(application: Application): AndroidViewModel(application) {


    val profileVM = ProfileViewModel(application)

    val playerList = mutableStateOf<List<Player>>(listOf())
    var uiState = mutableStateOf(GameUiState())
        private set

    val profileUi by profileVM::profileUi

    val voiceRec = VoiceRecognitionViewModel(application,profileUi.value.selectedLanguage)


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
        if(page == 2){
            voiceRec.setLanguage(profileUi.value.selectedLanguage)
        }
    }

    fun addPlayer() {
        val player = Player()
        player.name = ""
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

                if(existingPlayer.scorePerRound >= existingPlayer.declaration) {
                    type = 1
                } else {
                    type = -1
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

            val newScore = Score(score, type)

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

        uiState.value = uiState.value.copy(round = uiState.value.round + 1,
            distributorIndex = nextDistributorIndex())
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
                    }else if(existingPlayer.getTotalScore() + score >= 880) {
                        score = 880 - existingPlayer.getTotalScore()
                    }
                }
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
            setDeclarer(playerOnBarrel.name, 125)
        }
        else{
            uiState.value = uiState.value.copy(playerOnBarrel = null,
                declarer = null)
        }

        uiState.value = uiState.value.copy(round = uiState.value.round + 1,
            distributorIndex = nextDistributorIndex())
    }

    fun makePenalty(player: Player){
        val updatedPlayerList = playerList.value.map { existingPlayer ->
            var score = 0

            if(existingPlayer.name == player.name) {
                score = -120
            }

            val newScore = Score(score, 1)

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
        val index = uiState.value.distributorIndex
        return if (index == playerList.value.size - 1) 0 else index + 1
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

    fun setWinner(player: Player?) {
        uiState.value = uiState.value.copy(winner = player)
        profileVM.showAlert(R.string.winner, "${player?.name}",
            {profileVM.dismissAlert()}, {profileVM.dismissAlert()})
    }

    fun startGame() {
        viewModelScope.launch {
            val context = getApplication<Application>()
            val updatedPlayerList = playerList.value.filter { it.visible }
            playerList.value = updatedPlayerList
            for (player in playerList.value){
                if (player.name.trim() == ""){
                    profileVM.showAlert(R.string.error, context.getString(R.string.player_name_empty),
                        {profileVM.dismissAlert()}, {profileVM.dismissAlert()})
                    return@launch
                }
            }

            val names = playerList.value.map { it.name }
            if (names.size != names.toSet().size){
                profileVM.showAlert(R.string.error, context.getString(R.string.duplicate_names),
                    {profileVM.dismissAlert()}, {profileVM.dismissAlert()})
                return@launch
            }
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

    private fun chombaScore(suit: Int): Int{
        return when(suit){
            0 -> 40
            1 -> 60
            2 -> 80
            3 -> 100
            else -> 0
        }
    }

    fun saveGame(exit: Boolean = false) {
        viewModelScope.launch {
            startProgressGame()
            val savedGame = profileVM.saveGame(profileUi, playerList, uiState)
            profileUi.value = savedGame.first
            playerList.value = savedGame.second
            uiState.value = savedGame.third
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
        profileVM.showAlert(R.string.undo_last_round, getApplication<Application>().getString(R.string.are_you_sure),
            {undoLastRound()
            profileVM.dismissAlert()},
            {profileVM.dismissAlert()})
    }

    private fun undoLastRound(){
        val updatedPlayerList = playerList.value.map { existingPlayer ->
            val scoreList = existingPlayer.scoreList
            if (scoreList.isNotEmpty()) {
                val lastScore = scoreList.last()
                if (lastScore.type == 1 && lastScore.value == -120) {
                    existingPlayer.copy(scoreList = scoreList.dropLast(2) + Score(-120, 1),
                        scorePerRound = 0)
                } else {
                    existingPlayer.copy(scoreList = scoreList.dropLast(1),
                        scorePerRound = 0)
                }
            } else {
                existingPlayer
            }
        }


        playerList.value = updatedPlayerList
    }


}