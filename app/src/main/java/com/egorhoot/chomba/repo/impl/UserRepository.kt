package com.egorhoot.chomba.repo.impl

import android.R.string
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.MutableState
import com.egorhoot.chomba.GameUiState
import com.egorhoot.chomba.R
import com.egorhoot.chomba.data.Game
import com.egorhoot.chomba.data.Language
import com.egorhoot.chomba.data.LanguageId
import com.egorhoot.chomba.data.LeaderBoardPlayer
import com.egorhoot.chomba.data.Player
import com.egorhoot.chomba.data.Score
import com.egorhoot.chomba.data.User
import com.egorhoot.chomba.data.getTotalChombas
import com.egorhoot.chomba.data.getTotalScore
import com.egorhoot.chomba.data.isFinished
import com.egorhoot.chomba.data.isWinner
import com.egorhoot.chomba.pages.user.ProfileScreenUiState
import com.egorhoot.chomba.repo.UserRepository
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    override val auth: FirebaseAuth,
    private val db: FirebaseFirestore
): UserRepository {

    override fun sendSignInLink(email: String) {
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
            .setAndroidPackageName("com.egorhoot.chomba", true, "12")
            .build()
    }

    override fun completeSignInWithLink(email: String, code: String): Boolean {
        var isAuthenticated = false
        auth.signInWithEmailLink(email, code)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser

                    // Сохранение информации о пользователе в базу данных
                    // Например, используя Firebase Firestore:
                    val db = FirebaseFirestore.getInstance()
                    user?.let { db.collection("users").document(it.uid) }

                    // Вызов метода для автоматического входа в приложение
                    // Например, установка статуса аутентификации в вашей ViewModel:
                    isAuthenticated = true
                } else {
                    // Обработка ошибок
                    Log.w("signIN", "sendSignInLinkToEmail:failure", task.exception)
                }
            }

        return isAuthenticated
    }

    override fun getSignInRequest(apiKey: String): BeginSignInRequest {

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

    override fun signInWithGoogleToken(
        googleIdToken: String,
        profileUi: MutableState<ProfileScreenUiState>
    ) {
        val credential = GoogleAuthProvider.getCredential(googleIdToken, null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val userId = user?.uid ?: return@addOnCompleteListener

                    profileUi.value = profileUi.value.copy(
                        isAuthenticated = true,
                        displayName = user.displayName ?: "",
                        userPicture = user.photoUrl ?: Uri.EMPTY
                    )

                    val usersRef = db.collection("users").document(userId)

                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val userSnap = usersRef.get().await()
                            if (!userSnap.exists()) {
                                // Create new user document
                                val newUser = User(
                                    id = userId,
                                    name = user.displayName ?: "",
                                    nickname = user.displayName ?: "",
                                    userPicture = user.photoUrl.toString()
                                )
                                usersRef.set(newUser).await()
                            }

                            // Load games after ensuring user exists
                            loadGames(profileUi)
                        } catch (e: Exception) {
                            Log.e("signIn", "Failed to create or load user", e)
                        }
                    }
                } else {
                    Log.e("signIn", "Google sign-in failed", task.exception)
                }
            }
    }


    override fun saveGame(
        profileUi: MutableState<ProfileScreenUiState>,
        playerList: MutableState<List<Player>>,
        uiState: MutableState<GameUiState>
    ) {
        val userUid = auth.currentUser?.uid
        if (userUid == null) {
            uiState.value = uiState.value.copy(saveMsg = R.string.failed_you_are_not_authenticated)
            return
        }

        val gamesRef = db.collection("games")
        val id = profileUi.value.currentGameIndex ?: gamesRef.document().id
        profileUi.value = profileUi.value.copy(currentGameIndex = id)

        val date = System.currentTimeMillis()

        val gameData = Game(
            id = id,
            date = date,
            playerList = playerList.value,
            uiState = uiState.value,
            ownerId = userUid,
            playerListIds = playerList.value.map { it.userId },
            process = false // will set below
        )

        // Determine if game is finished
        val finished = gameData.isFinished()

        if (!finished) {
            // If not finished, set process = true
            val gameToSave = gameData.copy(process = true)

            gamesRef.document(id)
                .set(gameToSave, SetOptions.merge())
                .addOnSuccessListener {
                    uiState.value = uiState.value.copy(saveMsg = R.string.successfully_saved)
                }
                .addOnFailureListener {
                    uiState.value = uiState.value.copy(saveMsg = R.string.failed_to_save_game)
                }

        } else {
            // Game is finished - check existing game to see if process was true
            gamesRef.document(id).get()
                .addOnSuccessListener { document ->
                    val existingGame = document.toObject(Game::class.java)
                    if (existingGame?.process == true) {
                        CoroutineScope(Dispatchers.IO).launch {
                            // Update leaderboard players first
                            updateLeaderBoardPlayersForGame(gameData)
                        }

                        // After updating leaderboard, save game with process = false
                        val finishedGame = gameData.copy(process = false)
                        gamesRef.document(id)
                            .set(finishedGame, SetOptions.merge())
                            .addOnSuccessListener {
                                uiState.value = uiState.value.copy(saveMsg = R.string.successfully_saved)
                            }
                            .addOnFailureListener {
                                uiState.value = uiState.value.copy(saveMsg = R.string.failed_to_save_game)
                            }
                    } else {
                        // If process was not true before, just save with process = false
                        val finishedGame = gameData.copy(process = false)
                        gamesRef.document(id)
                            .set(finishedGame, SetOptions.merge())
                            .addOnSuccessListener {
                                uiState.value = uiState.value.copy(saveMsg = R.string.successfully_saved)
                            }
                            .addOnFailureListener {
                                uiState.value = uiState.value.copy(saveMsg = R.string.failed_to_save_game)
                            }
                    }
                }
                .addOnFailureListener {
                    uiState.value = uiState.value.copy(saveMsg = R.string.failed_to_save_game)
                }
        }
    }

    private fun updateLeaderBoardPlayersForGame(game: Game) {
        val leaderboardRef = db.collection("leaderboard")

        // Step 1: Load all leaderboard players involved in this game
        val playerIds = game.playerList.map { it.userId }.filter { it.isNotBlank() }

        if (playerIds.isEmpty()) {
            return
        }

        // Load leaderboard players documents for those IDs
        leaderboardRef.whereIn(FieldPath.documentId(), playerIds)
            .get()
            .addOnSuccessListener { snapshot ->

                // Map existing leaderboard players by userId for quick lookup
                val existingPlayers = snapshot.documents.associateBy({ it.id }, { doc ->
                    doc.toObject(LeaderBoardPlayer::class.java)
                })

                val updatedPlayers = mutableListOf<LeaderBoardPlayer>()

                // Iterate players in the game and update their leaderboard stats
                for (player in game.playerList) {
                    if (player.userId.isBlank()) continue

                    val oldData = existingPlayers[player.userId]

                    // Calculate updated stats
                    val isWinner = game.isWinner(player)
                    val playerTotalScore = player.getTotalScore()
                    val playerTotalChombas = player.getTotalChombas()

                    // Update wins and win streak
                    val newWins = (oldData?.wins ?: 0) + if (isWinner) 1 else 0

                    // For win streak, if last game was a win, increment, else reset
                    val newWinStreak = if (isWinner) (oldData?.winStreak ?: 0) + 1 else 0

                    // Update max win streak (stored in winStreak)
                    val maxWinStreak = maxOf(oldData?.winStreak ?: 0, newWinStreak)

                    // Aggregate totalScore and totalChombas
                    val newTotalScore = (oldData?.totalScore ?: 0) + playerTotalScore
                    val newTotalChombas = (oldData?.totalChombas ?: 0) + playerTotalChombas

                    // Merge score lists
                    val mergedScores = mutableListOf<Score>()
                    if (oldData != null) mergedScores.addAll(oldData.soreList)
                    mergedScores.addAll(player.scoreList)

                    // Merge colors (unique)
                    val oldColors = oldData?.colors ?: emptyList()
                    val newColors = (oldColors + player.color).distinct()

                    // Create updated LeaderBoardPlayer object
                    val updatedPlayer = LeaderBoardPlayer(
                        name = player.name,
                        userId = player.userId,
                        wins = newWins,
                        totalScore = newTotalScore,
                        winStreak = maxWinStreak,
                        totalChombas = newTotalChombas,
                        soreList = mergedScores,
                        colors = newColors
                    )

                    updatedPlayers.add(updatedPlayer)
                }

                // Step 3: Save all updated players back to leaderboard collection
                val batch = db.batch()

                for (player in updatedPlayers) {
                    val playerDoc = leaderboardRef.document(player.userId)
                    batch.set(playerDoc, player)
                }

                batch.commit()
                    .addOnFailureListener { e ->
                        Log.e("dataBase", "Failed to update leaderboard players", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("dataBase", "Failed to fetch leaderboard players", e)
            }
    }


    override fun editGame(profileUi: MutableState<ProfileScreenUiState>,
                          game: Game){
        val userUid = auth.currentUser?.uid
        if (userUid != null) {
            db.collection("games")
                .document(game.id)
                .set(game, SetOptions.merge())
                .addOnSuccessListener {
                    profileUi.value = profileUi.value.copy(saveMsg = R.string.successfully_saved)
                }
                .addOnFailureListener {
                    profileUi.value = profileUi.value.copy(saveMsg = R.string.failed_to_save_game)
                }
        }else{
            profileUi.value = profileUi.value.copy(saveMsg = R.string.failed_you_are_not_authenticated)
        }
    }

    fun addPlayerListIdsToAllGames() {
        val gamesRef = db.collection("games")

        gamesRef.get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    try {
                        val game = document.toObject(Game::class.java)
                        val playerIds = game.playerList.map { it.userId }.filter { it.isNotEmpty() }

                        // Update each game with new playerListIds field
                        gamesRef.document(game.id)
                            .update("playerListIds", playerIds)
                            .addOnSuccessListener {
                                Log.d("dataBase", "Updated playerListIds for game: ${game.id}")
                            }
                            .addOnFailureListener { e ->
                                Log.w("dataBase", "Failed to update playerListIds for game: ${game.id}", e)
                            }
                    } catch (e: Exception) {
                        Log.w("dataBase", "Failed to process game document ${document.id}", e)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.w("dataBase", "Failed to fetch games", e)
            }
    }


    override suspend fun loadGames(profileUi: MutableState<ProfileScreenUiState>) {
//        addPlayerListIdsToAllGames()
        val currentUserId = auth.currentUser?.uid ?: return

        // 1️⃣ Load related users first
        val relatedUsers = loadRelatedUserListSuspend()

        val gamesRef = db.collection("games")

        // 1️⃣ Query games where the current user is a player or owner
        val playerQuery = gamesRef
            .whereArrayContains("playerListIds", currentUserId)
            .orderBy("date", Query.Direction.DESCENDING)
            .get()
            .await()

        val ownerQuery = gamesRef
            .whereEqualTo("ownerId", currentUserId)
            .orderBy("date", Query.Direction.DESCENDING)
            .get()
            .await()

        // Merge results
        val result = (playerQuery.documents + ownerQuery.documents).distinctBy { it.id }

        // 2️⃣ Map games and mark editable if owned by current user
        val filteredGames = result.mapNotNull { doc ->
            doc.toObject(Game::class.java)?.copy(
                editable = doc.getString("ownerId") == currentUserId
            )
        }.sortedByDescending { it.date }

        // 3️⃣ Update UI
        withContext(Dispatchers.Main) {
            profileUi.value = profileUi.value.copy(
                gameList = filteredGames,
                saveMsg = if (filteredGames.isEmpty()) R.string.no_saved_games else 0,
                relatedUserList = relatedUsers,
                nickname = relatedUsers.find { it.id == currentUserId }?.nickname ?: auth.currentUser?.displayName ?: ""
            )
        }

//        createLeaderBoardForAllGames()

    }

    override suspend fun getLeaderBoardPlayers(
        relatedUserList: List<User>
    ): List<LeaderBoardPlayer> {
        if (relatedUserList.isEmpty()) return emptyList()

        val leaderboardRef = db.collection("leaderboard")
        val relatedUserIds = relatedUserList.map { it.id }
        val players = mutableListOf<LeaderBoardPlayer>()

        try {
            // Firestore doesn't allow "whereIn" with more than 10 values at once
            val chunks = relatedUserIds.chunked(10)

            for (chunk in chunks) {
                val querySnapshot = leaderboardRef
                    .whereIn("userId", chunk)
                    .get()
                    .await()

                for (doc in querySnapshot.documents) {
                    doc.toObject(LeaderBoardPlayer::class.java)?.let { player ->
                        // Find current nickname from relatedUserList by matching userId
                        val matchingUser = relatedUserList.find { it.id == player.userId }
                        if (matchingUser != null) {
                            // Create a new LeaderBoardPlayer with updated name (nickname)
                            val updatedPlayer = player.copy(name = matchingUser.nickname ?: player.name)
                            players.add(updatedPlayer)
                        } else {
                            // If no match, keep original player
                            players.add(player)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("dataBase", "Failed to load leaderboard players", e)
        }

        return players
    }


    suspend fun createLeaderBoardForAllGames() {
        val gamesRef = db.collection("games")
        val leaderboardRef = db.collection("leaderboard")

        // 1️⃣ Fetch all games from Firestore
        val result = gamesRef.get().await()
        val gameList = result.documents.mapNotNull { it.toObject(Game::class.java) }

        val playersName = mutableSetOf<String>()

        // 2️⃣ Collect all unique player names from finished games
        for (game in gameList) {
            if (!game.isFinished()) continue
            playersName.addAll(game.playerList.map { it.name })
        }

        val players = mutableListOf<LeaderBoardPlayer>()

        // 3️⃣ Calculate stats for each player
        for (playerName in playersName) {
            var wins = 0
            var totalScore = 0
            var winStreak = 0
            var maxWinStreak = 0
            var totalChombas = 0
            var userId = ""
            val scoreList = mutableListOf<Score>()
            val colors = mutableListOf<String>()

            for (game in gameList.reversed()) {
                if (!game.isFinished()) continue

                for (player in game.playerList) {
                    if (player.name == playerName) {
                        totalScore += player.getTotalScore()

                        if (game.isWinner(player)) {
                            wins++
                            winStreak++
                        } else {
                            winStreak = 0
                        }

                        maxWinStreak = maxOf(maxWinStreak, winStreak)
                        totalChombas += player.getTotalChombas()

                        scoreList.addAll(player.scoreList)
                        colors.add(player.color)
                        userId = player.userId
                    }
                }
            }

            players.add(
                LeaderBoardPlayer(
                    name = playerName,
                    userId = userId,
                    wins = wins,
                    totalScore = totalScore,
                    winStreak = maxWinStreak,
                    totalChombas = totalChombas,
                    soreList = scoreList,
                    colors = colors
                )
            )
        }

        // 4️⃣ Save leaderboard players to Firestore
        for (player in players.sortedByDescending { it.wins }) {
            leaderboardRef.document(player.userId.ifEmpty { player.name }).set(player).await()
        }
    }

    private suspend fun loadRelatedUserListSuspend(): List<User> {
        val currentUserId = auth.currentUser?.uid ?: return emptyList()
        val gamesRef = db.collection("games")
        val usersRef = db.collection("users")

        // 1️⃣ Get all games where current user played
        val gamesSnapshot = gamesRef
            .whereArrayContains("playerListIds", currentUserId) // Use your indexed field
            .get()
            .await()

        // 2️⃣ Collect unique userIds of players related to current user (excluding current user)
        val relatedUserIds = mutableSetOf<String>()
        for (doc in gamesSnapshot.documents) {
            val game = doc.toObject(Game::class.java) ?: continue
            game.playerList.forEach { player ->
                if (player.userId.isNotEmpty() && player.userId != currentUserId) {
                    relatedUserIds.add(player.userId)
                }
            }
        }

        // 3️⃣ Load user documents for all relatedUserIds
        val relatedUsers = mutableListOf<User>()
        if (relatedUserIds.isNotEmpty()) {
            // Firestore limitation: max 10 elements in whereIn queries, so split if needed
            val chunkedIds = relatedUserIds.chunked(10)
            for (chunk in chunkedIds) {
                val userSnapshot = usersRef.whereIn(FieldPath.documentId(), chunk).get().await()
                for (userDoc in userSnapshot.documents) {
                    userDoc.toObject(User::class.java)?.let { user ->
                        relatedUsers.add(user.copy(id = userDoc.id))
                    }
                }
            }
        }

        // 4️⃣ Add current user info at the end
        val currentUserSnap = usersRef.document(currentUserId).get().await()
        if (currentUserSnap.exists()) {
            currentUserSnap.toObject(User::class.java)?.let { user ->
                relatedUsers.add(user.copy(id = currentUserSnap.id))
            }
        }

        return relatedUsers
    }


    //delete users with given nickname
    fun deleteUsersWithNickname(nickname: String) {
        val usersRef = db.collection("users")
        usersRef.whereEqualTo("nickname", nickname).get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot.documents) {
                    document.reference.delete()
                        .addOnSuccessListener {
                            Log.d("dataBase", "User with nickname $nickname deleted successfully.")
                        }
                        .addOnFailureListener { e ->
                            Log.w("dataBase", "Failed to delete user with nickname $nickname", e)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.w("dataBase", "Failed to query users with nickname $nickname", e)
            }
    }

    fun updatePlayersInGames() {
        val gamesRef = db.collection("games")
        val usersRef = db.collection("users")


        // 1️⃣ Load all existing users into a map
        usersRef.get()
            .addOnSuccessListener { userResult ->
                val userMap = mutableMapOf<String, String>() // nickName -> userId
                for (userDoc in userResult) {
                    val user = userDoc.toObject(User::class.java)
                    userMap[user.nickname] = user.id.ifEmpty { userDoc.id }
                }

                // 2️⃣ Now fetch all games
                gamesRef.get()
                    .addOnSuccessListener { gameResult ->
                        for (gameDoc in gameResult) {
                            val gameData = gameDoc.toObject(Game::class.java)
                            val updatedPlayers = mutableListOf<Player>()

                            for (player in gameData.playerList) {
                                val playerName = player.name.trim()

                                // ✅ Check if nickname exists in our preloaded userMap
                                if (userMap.containsKey(playerName)) {
                                    player.userId = userMap[playerName]!!
                                } else {
                                    // ❌ No user found: create new user once and store it
                                    val newUserRef = usersRef.document()
                                    val newUser = User(id = newUserRef.id, nickname = playerName)
                                    newUserRef.set(newUser)
                                    userMap[playerName] = newUser.id
                                    player.userId = newUser.id
                                    Log.d("dataBase", "Created new user: $playerName (${newUser.id})")
                                }

                                updatedPlayers.add(player)
                            }

                            // 3️⃣ Update game with corrected player userIds
                            gameData.playerList = updatedPlayers
                            gamesRef.document(gameDoc.id).set(gameData)
                                .addOnSuccessListener {
                                    Log.d("dataBase", "Game ${gameDoc.id} updated successfully.")
                                }
                                .addOnFailureListener { e ->
                                    Log.w("dataBase", "Failed to update game ${gameDoc.id}", e)
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.w("dataBase", "Failed to retrieve games", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.w("dataBase", "Failed to load users", e)
            }
    }



    override fun deleteGame(id: String, profileUi: MutableState<ProfileScreenUiState>){
        val db = Firebase.firestore
        val userUid = auth.currentUser?.uid
        if (userUid != null) {
            db.collection("games")
                .document(id)
                .delete()
                .addOnSuccessListener {
                    profileUi.value = profileUi.value.copy(saveMsg = R.string.successfully_deleted)
                }
                .addOnFailureListener {
                    profileUi.value = profileUi.value.copy(saveMsg = R.string.failed_to_delete_game)
                }
        }else{
            profileUi.value = profileUi.value.copy(saveMsg = R.string.failed_you_are_not_authenticated)
        }
    }

    override fun saveVoiceRecLanguage(language: Language){
        val db = Firebase.firestore
        val userUid = auth.currentUser?.uid
        if (userUid != null) {
            db.collection("users").document(userUid)
                .collection("settings")
                .document("voiceRecLanguage")
                .set(LanguageId(language))
                .addOnSuccessListener {
                    Log.d("dataBase", "DocumentSnapshot successfully written!")
                }
                .addOnFailureListener { e ->
                    Log.w("dataBase", "Error writing document", e)
                }
        }
    }

    override fun loadVoiceRecLanguage(profileUi: MutableState<ProfileScreenUiState>){
        val db = Firebase.firestore
        val userUid = auth.currentUser?.uid
        var language = Language(R.drawable.flag_ua, R.string.tag_ua)
        if (userUid != null) {
            db.collection("users").document(userUid)
                .collection("settings")
                .document("voiceRecLanguage")
                .get()
                .addOnSuccessListener { document ->
                    try{
                        language = Language(document.data?.get("id").toString())
                        profileUi.value = profileUi.value.copy(selectedLanguage = language)
                        Log.w("dataBase", "loadVoiceRecLanguage: success")
                    }catch (e: Exception){
                        Log.w("dataBase", "loadVoiceRecLanguage:failure", e)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w("dataBase", "get failed with ", exception)
                }
        }
    }

    override suspend fun updateUserNickname(newNickname: String){
        val db = Firebase.firestore
        val userUid = auth.currentUser?.uid
        if (userUid != null) {
            try {
                val userRef = db.collection("users").document(userUid)
                userRef.update("nickname", newNickname).await()

            } catch (e: Exception) {
                Log.e("dataBase", "Failed to update nickname for user $userUid", e)
            }
        }
    }

    override suspend fun getUserByUid(uid: String, onResult: () -> Unit):User? {
        val db = Firebase.firestore
        val userUid = auth.currentUser?.uid
        if (userUid != null) {
            try {
                val userDoc = db.collection("users").document(uid).get().await()
                return userDoc.toObject(User::class.java)?.copy(id = userDoc.id)
            } catch (e: Exception) {
                Log.e("dataBase", "Failed to get user by UID $uid", e)
            }
        }

        return null
    }

    override fun getPlayersFromLastGame(playerList: MutableState<List<Player>>, onResult: () -> Unit) {
        val userUid = auth.currentUser?.uid ?: return

        db.collection("games")
            .whereArrayContains("playerListIds", userUid)
            .orderBy("date", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    try {
                        val gameData = result.documents[0].toObject(Game::class.java)
                        if (gameData != null) {
                            val playerL = gameData.playerList

                            // ✅ Sort players: winner first, maintaining circular order
                            val winner = playerL.maxByOrNull { it.getTotalScore() }
                            val winnerIndex = playerL.indexOf(winner)
                            val sortedList = playerL.subList(winnerIndex, playerL.size) +
                                    playerL.subList(0, winnerIndex)

                            // ✅ Create new list with just names and colors
                            val newPlayerList = sortedList.map { player ->
                                Player(name = player.name, color = player.color, userId = player.userId)
                            }

                            playerList.value = newPlayerList
                        }
                    } catch (e: Exception) {
                        Log.w("dataBase", "getPlayersFromLastGame:failure", e)
                    }
                }
                onResult()
            }
            .addOnFailureListener { exception ->
                Log.w("dataBase", "getPlayersFromLastGame:failure", exception)
                onResult()
            }
    }

    override suspend fun createMissingUsers(playerList: MutableState<List<Player>>) {
        val usersRef = db.collection("users")

        val updatedPlayers = playerList.value.toMutableList()

        for ((index, player) in updatedPlayers.withIndex()) {
            if (player.userId.isBlank()) {
                // Generate a new document ID for the user
                val newUserId = usersRef.document().id

                // Create new user object
                val newUser = User(
                    id = newUserId,
                    name = player.name,
                    nickname = player.name,
                )

                // Save the new user to Firestore
                try {
                    usersRef.document(newUserId).set(newUser).await()
                    // Set the generated userId in the player object
                    updatedPlayers[index] = player.copy(userId = newUserId)
                } catch (e: Exception) {
                    Log.e("createMissingUsers", "Failed to create user for ${player.name}", e)
                }
            }
        }

        // Update the playerList with new userIds
        playerList.value = updatedPlayers
    }

    suspend fun cleanLeaderboardDuplicates() {
        val leaderboardRef = db.collection("leaderboard")

        try {
            val snapshot = leaderboardRef.get().await()

            for (doc in snapshot.documents) {
                val player = doc.toObject(LeaderBoardPlayer::class.java) ?: continue

                val cleanedColors = player.colors.distinct()
                val cleanedScores = player.soreList.distinct()

                // Only update if something actually changed
                if (cleanedColors != player.colors || cleanedScores != player.soreList) {
                    leaderboardRef.document(doc.id).update(
                        mapOf(
                            "colors" to cleanedColors,
                            "soreList" to cleanedScores
                        )
                    ).await()
                    Log.d("cleanLeaderboard", "Cleaned duplicates for user ${doc.id}")
                }
            }

            Log.d("cleanLeaderboard", "Finished cleaning all leaderboard entries")
        } catch (e: Exception) {
            Log.e("cleanLeaderboard", "Error cleaning leaderboard duplicates", e)
        }
    }

    override suspend fun mergeUser(userUid: String, onResult: (Boolean, String) -> Unit){
        val currentUserId = auth.currentUser?.uid ?: return

        if (currentUserId == userUid) return // Don't merge into self

        val usersRef = db.collection("users")
        val gamesRef = db.collection("games")
        val leaderboardRef = db.collection("leaderboard")

        try {
            // 1️⃣ Merge Leaderboard Stats
            val oldLbSnap = leaderboardRef.document(userUid).get().await()
            var oldNickname= ""
            if (oldLbSnap.exists()) {
                val oldLb = oldLbSnap.toObject(LeaderBoardPlayer::class.java)
                if (oldLb != null) {
                    val currentLbSnap = leaderboardRef.document(currentUserId).get().await()
                    if (currentLbSnap.exists()) {
                        val currentLb = currentLbSnap.toObject(LeaderBoardPlayer::class.java)
                        if (currentLb != null) {
                            val mergedLb = currentLb.copy(
                                wins = currentLb.wins + oldLb.wins,
                                totalScore = currentLb.totalScore + oldLb.totalScore,
                                winStreak = maxOf(currentLb.winStreak, oldLb.winStreak),
                                totalChombas = currentLb.totalChombas + oldLb.totalChombas,
                                soreList = (currentLb.soreList + oldLb.soreList).distinct(),
                                colors = (currentLb.colors + oldLb.colors).distinct()
                            )
                            leaderboardRef.document(currentUserId).set(mergedLb).await()
                        }
                        oldNickname = oldLb.name
                    } else {
                        // If current user doesn't have leaderboard entry, just copy old
                        leaderboardRef.document(currentUserId).set(
                            oldLb.copy(
                                userId = currentUserId)
                        ).await()
                    }
                }
                leaderboardRef.document(userUid).delete().await()
            }

            // 2️⃣ Update Games (players + owner)
            val gamesSnap = gamesRef
                .whereArrayContains("playerListIds", userUid)
                .get()
                .await()

            val currentUserSnap = usersRef.document(currentUserId).get().await()
            val currentUser = currentUserSnap.toObject(User::class.java)

            for (doc in gamesSnap.documents) {
                val game = doc.toObject(Game::class.java) ?: continue

                val updatedPlayers = game.playerList.map { player ->
                    if (player.userId == userUid) {
                        player.copy(
                            userId = currentUserId,
                            name = currentUser?.nickname ?: currentUser?.name ?: player.name
                        )
                    } else player
                }
                val updatedPlayerIds = updatedPlayers.map { it.userId }
                val newOwnerId = if (game.ownerId == userUid) currentUserId else game.ownerId

                gamesRef.document(doc.id).update(
                    mapOf(
                        "playerList" to updatedPlayers,
                        "playerListIds" to updatedPlayerIds,
                        "ownerId" to newOwnerId
                    )
                ).await()
            }

            // 3️⃣ Delete Old User
            usersRef.document(userUid).delete().await()

            onResult(true, oldNickname)

        } catch (e: Exception) {
            Log.e("mergeUser", "Failed to merge user $userUid into $currentUserId", e)
            onResult(false, "")
        }
    }

}