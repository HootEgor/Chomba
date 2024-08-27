package com.egorhoot.chomba.repo.impl

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.MutableState
import com.egorhoot.chomba.GameUiState
import com.egorhoot.chomba.R
import com.egorhoot.chomba.data.Game
import com.egorhoot.chomba.data.Language
import com.egorhoot.chomba.data.LanguageId
import com.egorhoot.chomba.data.Player
import com.egorhoot.chomba.data.User
import com.egorhoot.chomba.pages.user.ProfileScreenUiState
import com.egorhoot.chomba.repo.UserRepository
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(): UserRepository {
    override val auth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore

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
                    val newUser = User(email = email)

                    // Сохранение информации о пользователе в базу данных
                    // Например, используя Firebase Firestore:
                    val db = FirebaseFirestore.getInstance()
                    user?.let { db.collection("users").document(it.uid).set(newUser) }

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

    override fun signInWithGoogleToken(googleIdToken: String, profileUi: MutableState<ProfileScreenUiState>){
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

    override fun saveGame(profileUi: MutableState<ProfileScreenUiState>,
                          playerList: MutableState<List<Player>>,
                          uiState: MutableState<GameUiState>){
        var id = ""
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

    }

    override fun editGame(profileUi: MutableState<ProfileScreenUiState>,
                          game: Game){
        val userUid = auth.currentUser?.uid
        if (userUid != null) {
            db.collection("users").document(userUid)
                .collection("gameList")
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

    override fun loadGames(profileUi: MutableState<ProfileScreenUiState>){
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

    }

    override fun deleteGame(id: String, profileUi: MutableState<ProfileScreenUiState>){
        val db = Firebase.firestore
        val userUid = auth.currentUser?.uid
        if (userUid != null) {
            db.collection("users").document(userUid)
                .collection("gameList")
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

}