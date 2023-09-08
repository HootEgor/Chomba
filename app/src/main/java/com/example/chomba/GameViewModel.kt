package com.example.chomba

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.chomba.data.Game
import com.example.chomba.data.Player
import com.example.chomba.data.Score
import com.example.chomba.data.User
import com.example.chomba.data.getMissBarrel
import com.example.chomba.data.getTotalScore
import com.example.chomba.pages.user.ProfileScreenUiState
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
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
            profileUi.value = profileUi.value.copy(isAuthenticated = true,
                displayName = auth.currentUser?.displayName ?: "",
                userPicture = auth.currentUser?.photoUrl ?: Uri.EMPTY)
        }
    }

    private fun startProgressProfile(){
        profileUi.value = profileUi.value.copy(inProgress = true,
            saveMsg = R.string.in_progress)
    }

    private fun stopProgressProfile(){
        profileUi.value = profileUi.value.copy(inProgress = false)
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
                scorePerRound = 0, blind = false)

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
                    profileUi.value = profileUi.value.copy(isAuthenticated = true,
                        displayName = user?.displayName ?: "",
                        userPicture = user?.photoUrl ?: Uri.EMPTY)
                } else {
                    // Обработка ошибок
                }
            }
    }

    fun signOut() {
        viewModelScope.launch {
            auth.signOut()
        }.invokeOnCompletion {
            profileUi.value = profileUi.value.copy(isAuthenticated = false,
                displayName = "",
                userPicture = Uri.EMPTY)
        }
    }

    fun saveGame() {
        viewModelScope.launch {
            startProgressGame()
            var id = ""
            val db = Firebase.firestore
            val userUid = auth.currentUser?.uid

            if (userUid != null) {
                if(profileUi.value.currentGameIndex != null){
                    id = profileUi.value.currentGameIndex!!
                }
                else{
                    id = db.collection("users").document(userUid)
                        .collection("gameList")
                        .document().id
                }
                val date = System.currentTimeMillis()
                val gameData = Game(id, date ,playerList.value, uiState.value)
                db.collection("users").document(userUid)
                    .collection("gameList")
                    .document(id)
                    .set(gameData, SetOptions.merge())
                    .addOnSuccessListener {
                        uiState.value = uiState.value.copy(saveMsg = R.string.successfully_saved)
                        profileUi.value = profileUi.value.copy(currentGameIndex = id)
                    }
                    .addOnFailureListener {
                        uiState.value = uiState.value.copy(saveMsg = R.string.failed_to_save_game)
                    }
            }else{
                uiState.value = uiState.value.copy(saveMsg = R.string.failed_you_are_not_authenticated)
            }

        }.invokeOnCompletion {
            stopProgressGame()
        }

    }

    fun loadGames(){
        viewModelScope.launch {
            startProgressProfile()
            val db = Firebase.firestore
            val userUid = auth.currentUser?.uid
            if (userUid != null) {
                db.collection("users").document(userUid)
                    .collection("gameList")
                    .orderBy("date", Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener { result ->
                        profileUi.value = profileUi.value.copy(gameList = emptyList())
                        for (document in result) {
                            try{
                                val gameData = document.toObject(Game::class.java)
                                profileUi.value = profileUi.value.copy(gameList = profileUi.value.gameList + gameData)
                            }catch (e: Exception){
                                Log.w("dataBase", "loadGameList:failure", e)
                            }
                        }
                        profileUi.value = profileUi.value.copy(saveMsg = R.string.no_saved_games)
                    }
                    .addOnFailureListener { exception ->
                        profileUi.value = profileUi.value.copy(saveMsg = R.string.failed_to_load_games)
                        Log.w("dataBase", "loadGameList:failure", exception)
                    }
            }
        }.invokeOnCompletion {
            profileUi.value = profileUi.value.copy(currentGameIndex = null)
            stopProgressProfile()
        }

    }

    fun setCurrentGame(id: String){
        val game = profileUi.value.gameList.find { it.id == id }!!
        if(!isGameFinished(game)){
            profileUi.value = profileUi.value.copy(currentGameIndex = id)
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

    fun isGameFinished(game: Game): Boolean {
        for (player in game.playerList){
            if (player.getTotalScore() == 1000){
                return true
            }
        }
        return false
    }

    fun isCurrentGameFinished(): Boolean {
        for (player in playerList.value){
            if (player.getTotalScore() == 1000){
                return true
            }
        }
        return false
    }


}