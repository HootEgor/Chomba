package com.example.chomba

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract.CommonDataKinds.Email
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.chomba.data.Player
import com.example.chomba.data.Score
import com.example.chomba.data.User
import com.example.chomba.data.getMissBarrel
import com.example.chomba.data.getTotalScore
import com.example.chomba.pages.user.ProfileScreenUiState
import com.example.chomba.ui.theme.ext.isValidEmail
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlin.math.round

class GameViewModel(application: Application): AndroidViewModel(application) {

    val playerList = mutableStateOf<List<Player>>(listOf())
    var uiState = mutableStateOf(GameUiState())
        private set

    var profileUi = mutableStateOf(ProfileScreenUiState())
        private set

    private val auth = FirebaseAuth.getInstance()
    init {
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            profileUi.value = profileUi.value.copy(isAuthenticated = true)
        }
    }

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

    fun setScorePerRoundD(player: Player){
        val updatedPlayerList = playerList.value.map { existingPlayer ->
            if (existingPlayer == player) {
                existingPlayer.copy(scorePerRound = existingPlayer.declaration)
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
            }

            if(uiState.value.declarer?.name == existingPlayer.name) {

                if(existingPlayer.scorePerRound >= existingPlayer.declaration) {
                    type = 1
                } else {
                    type = -1
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
                if(type == -1 && existingPlayer.getMissBarrel() < 2) {
                    type = -2
                }
                else if(type != 1)
                    type = -4

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
            setDeclarer(playerOnBarrel.name, 120)
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

    fun sendSignInLink(email: String) {
        auth.sendSignInLinkToEmail(email, buildActionCodeSettings())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.w("signIN", "sendSignInLinkToEmail:failure", task.exception)
                } else {
                    Log.w("signIN", "sendSignInLinkToEmail:failure", task.exception)
                }
            }
    }

    private fun buildActionCodeSettings(): ActionCodeSettings {
        return ActionCodeSettings.newBuilder()
            .setUrl("https://chomba.page.link/signup") // Используйте существующую ссылку
            .setHandleCodeInApp(true)
            .setAndroidPackageName("com.example.chomba", true, "12")
            .build()
    }

    fun completeSignInWithLink(email: String, code: String) {
        auth.signInWithEmailLink(email, code)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val newUser = User(email = email)

                    // Сохранение информации о пользователе в базу данных
                    // Например, используя Firebase Firestore:
                     val db = FirebaseFirestore.getInstance()
                    user?.let { db.collection("users").document(it.uid).set(newUser) }

                    // Вызов метода для автоматического входа в приложение
                    // Например, установка статуса аутентификации в вашей ViewModel:
                     profileUi.value = profileUi.value.copy(isAuthenticated = true)
                } else {
                    // Обработка ошибок
                    Log.w("signIN", "sendSignInLinkToEmail:failure", task.exception)
                }
            }
    }

    fun getSignInRequest(apiKey: String): BeginSignInRequest {

        return BeginSignInRequest.builder()
            .setPasswordRequestOptions(
                BeginSignInRequest.PasswordRequestOptions.builder()
                    .setSupported(true)
                    .build())
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(apiKey)
                    .setFilterByAuthorizedAccounts(false)
                    .build())
            .setAutoSelectEnabled(true)
            .build()

    }

    fun signInWithGoogleToken(googleIdToken: String) {
        val credential = GoogleAuthProvider.getCredential(googleIdToken, null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    profileUi.value = profileUi.value.copy(isAuthenticated = true)
                } else {
                    // Обработка ошибок
                }
            }
    }




}