package com.egorhoot.chomba

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
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
import com.egorhoot.chomba.data.getChombaScore
import com.egorhoot.chomba.data.getMissBarrel
import com.egorhoot.chomba.data.getScoreSum
import com.egorhoot.chomba.data.getTotalScore
import com.egorhoot.chomba.pages.PageState
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
    val profileUi: MutableState<ProfileScreenUiState>,
    val pageState: MutableState<PageState>,
    private val context: Context,
): ChombaViewModel() {

    val playerList = mutableStateOf<List<Player>>(listOf())
    var uiState = mutableStateOf(GameUiState())
        private set

    fun isAuthorized(): Boolean {
        return userRepo.auth.currentUser != null
    }

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

    fun onlineGame(){
        setCurrentPage(4)
    }

    fun setCurrentPage(page: Int) {
        if(page == 0 && pageState.value.currentPage == 2){
            saveGame()
        }
        pageState.value = pageState.value.copy(currentPage = page)
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

    fun movePlayer(fromIndex: Int, toIndex: Int) {
        playerList.value = playerList.value.toMutableList().apply { add(toIndex, removeAt(fromIndex)) }
    }

    fun saveScorePerRound(player: Player, score: Int) {
        val updatedPlayerList = playerList.value.map { existingPlayer ->
            if (existingPlayer.name == player.name) {
                existingPlayer.copy(scorePerRound = score)
            } else {
                existingPlayer
            }
        }

        val updatedTakePlayerNameList = uiState.value.takePlayerNameList.toMutableList()
        if(updatedTakePlayerNameList.contains(player.name)) {
            updatedTakePlayerNameList.remove(player.name)
            updatedTakePlayerNameList.add(0, player.name)
        }
        else{
            updatedTakePlayerNameList.add(0, player.name)
        }

        uiState.value = uiState.value.copy(takePlayerNameList = updatedTakePlayerNameList)

        playerList.value = updatedPlayerList

        checkTotalScore()
    }

    fun setScorePerRound(player: Player) {
        val updatedPlayerList = playerList.value.map { existingPlayer ->
            if (existingPlayer.name == player.name) {
                if(existingPlayer.getChombaScore() > existingPlayer.declaration){
                    existingPlayer.copy(scorePerRound = 0)
                }
                else{
                    existingPlayer.copy(scorePerRound = if(existingPlayer.declaration -
                        existingPlayer.getChombaScore() > 120) 120 else existingPlayer.declaration - existingPlayer.getChombaScore())
                }
            } else {
                existingPlayer
            }
        }

        val updatedTakePlayerNameList = uiState.value.takePlayerNameList.toMutableList()
        if(updatedTakePlayerNameList.contains(player.name)) {
            updatedTakePlayerNameList.remove(player.name)
            updatedTakePlayerNameList.add(0, player.name)
        }
        else{
            updatedTakePlayerNameList.add(0, player.name)
        }

        uiState.value = uiState.value.copy(takePlayerNameList = updatedTakePlayerNameList)

        playerList.value = updatedPlayerList
        checkTotalScore()
    }

    fun checkTotalScore() {
        var totalScore = 120
        var updatedPlayerList = playerList.value

        // Subtract scorePerRound from totalScore for each player in the list
        for (player in uiState.value.takePlayerNameList) {
            val playerScore = updatedPlayerList.find { it.name == player }?.scorePerRound ?: 0
            totalScore -= playerScore

            // If the totalScore goes negative, cap the score of this player and stop further reduction
            if (totalScore < 0) {
                updatedPlayerList = updatedPlayerList.map { existingPlayer ->
                    if (existingPlayer.name == player) {
                        existingPlayer.copy(scorePerRound = existingPlayer.scorePerRound + totalScore)
                    } else {
                        existingPlayer
                    }
                }
                totalScore = 0
                break
            }
        }

        // Ensure the last player's score adjusts so that the total equals 120
        if (totalScore > 0) {
            val lastPlayer = uiState.value.takePlayerNameList.last()
            updatedPlayerList = updatedPlayerList.map { existingPlayer ->
                if (existingPlayer.name == lastPlayer) {
                    existingPlayer.copy(scorePerRound = existingPlayer.scorePerRound + totalScore)
                } else {
                    existingPlayer
                }
            }
        }

        // Update the playerList state
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
            existingPlayer.scorePerRound += existingPlayer.getChombaScore()
            var score = existingPlayer.scorePerRound
            if(score > 420){
                score = 420
            }
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

        equalizeScores(1000, 330)

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
        uiState.value = uiState.value.copy(distributorIndex = nextDistributorIndex(),
            takePlayerNameList = listOf())

        if(uiState.value.round % 3 == 0){
            saveGame()
        }
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

        if(uiState.value.round % 3 == 0){
            saveGame()
        }
    }

    fun makePenalty(player: Player){
        val updatedPlayerList = playerList.value.map { existingPlayer ->
            var score = 0

            if(existingPlayer.name == player.name) {
                score = 120
            }

            val newScore = Score(score, -1, uiState.value.round-1)

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
        else if (playersOnBarrel.size == 2){
            if(uiState.value.playerOnBarrel != null){
                var playerOnBarrel = playersOnBarrel[0]
                for (player in playersOnBarrel){
                    if (player.name == uiState.value.playerOnBarrel?.name){
                        makePenalty(player)
                    }else {
                        playerOnBarrel = player
                    }
                }
                return playerOnBarrel
            }
            else{
                for(player in playersOnBarrel){
                    makePenalty(player)
                }
                return null
            }
        }else{
            for(player in playersOnBarrel){
                makePenalty(player)
            }
            return null
//            var countList: List<Int> = listOf()
//            for (player in playersOnBarrel){
//                var count = 0
//                for(i in playersOnBarrel.size-1 downTo 1){
//                    if (player.scoreList[scoreListSize - i].type == 2 ||
//                        player.scoreList[scoreListSize - i].type == -2 ||
//                        player.getTotalScore(player.scoreList[scoreListSize - i].round-1) == 880){
//                       count++
//                    }
//                }
//                countList = countList + count
//            }
//
//            val minIndex = countList.indexOf(countList.minOrNull())
//            var playerOnBarrel = playersOnBarrel[0]
//            //count equal values in countList
//            if(countList.count(countList[minIndex]::equals) == playersOnBarrel.size){
//                for(player in playersOnBarrel){
//                    makePenalty(player)
//                }
//                return null
//            }else{
//                for(player in playersOnBarrel){
//                    if(playersOnBarrel.indexOf(player) == minIndex){
//                        playerOnBarrel = player
//                    }else{
//                        makePenalty(player)
//                    }
//                }
//            }
//
//            return playerOnBarrel
        }
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
                existingPlayer.copy(takenChombas = existingPlayer.takenChombas + CardSuit.values()[suit])
            } else {
                existingPlayer
            }
        }
        playerList.value = updatedPlayerList
    }

    fun undoChomba(player: Player, suit: Int) {
        val updatedPlayerList = playerList.value.map { existingPlayer ->
            if (existingPlayer.name == player.name) {
                existingPlayer.copy(takenChombas = existingPlayer.takenChombas.filter { it.ordinal != suit })
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

    private fun equalizeScores(sum: Int, equalizedScore: Int){
        if(playerList.value.find { it.getTotalScore() == sum } != null){
            return
        }
        val playersScoreSum = playerList.value.sumOf { it.getTotalScore() }
        if(playersScoreSum == sum){
            val updatedPlayerList = playerList.value.map { existingPlayer ->
                val score = equalizedScore - existingPlayer.getTotalScore()
                val newScore = Score(score, 1, uiState.value.round)
                existingPlayer.copy(scoreList = (existingPlayer.scoreList + newScore),
                    scorePerRound = 0)
            }
            playerList.value = updatedPlayerList
        }
    }

    fun getPlayersFromLastGame(){
        viewModelScope.launch {
            startProgressGame()
            userRepo.getPlayersFromLastGame(playerList){
                stopProgressGame()
            }
        }
    }

    fun sumScoreToast(){
        val sum = playerList.value.sumOf { it.getTotalScore() }
        var sumString = sum.toString()
        if (sum > 1000){
            sumString += " - " + (sum - 1000)
        }else if (sum < 1000){
            sumString += " + " + (1000 - sum)
        }
        Toast.makeText(context, sumString, Toast.LENGTH_SHORT).show()
    }


}