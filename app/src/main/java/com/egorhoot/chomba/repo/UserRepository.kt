package com.egorhoot.chomba.repo

import android.R.string
import androidx.compose.runtime.MutableState
import com.egorhoot.chomba.GameUiState
import com.egorhoot.chomba.data.Game
import com.egorhoot.chomba.data.Language
import com.egorhoot.chomba.data.LeaderBoardPlayer
import com.egorhoot.chomba.data.Player
import com.egorhoot.chomba.data.User
import com.egorhoot.chomba.pages.user.ProfileScreenUiState
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.firebase.auth.FirebaseAuth

interface UserRepository {
    val auth : FirebaseAuth
    fun sendSignInLink(email: String)
    fun completeSignInWithLink(email: String, code: String): Boolean
    fun getSignInRequest(apiKey: String): BeginSignInRequest
    fun signInWithGoogleToken(googleIdToken: String, profileUi: MutableState<ProfileScreenUiState>)
    fun saveGame(profileUi: MutableState<ProfileScreenUiState>,
                         playerList: MutableState<List<Player>>,
                         uiState: MutableState<GameUiState>)
    fun editGame(profileUi: MutableState<ProfileScreenUiState>,
                         game: Game)
    suspend fun loadGames(profileUi: MutableState<ProfileScreenUiState>)
    suspend fun getLeaderBoardPlayers(relatedUserList: List<User>): List<LeaderBoardPlayer>
    fun deleteGame(id: String, profileUi: MutableState<ProfileScreenUiState>)
    fun saveVoiceRecLanguage(language: Language)
    suspend fun updateUserNickname(newNickname: String)
    suspend fun getUserByUid(uid: String, onResult: () -> Unit): User?
    fun loadVoiceRecLanguage(profileUi: MutableState<ProfileScreenUiState>)
    fun getPlayersFromLastGame(playerList: MutableState<List<Player>>, onResult: () -> Unit)
    suspend fun createMissingUsers(playerList: MutableState<List<Player>>)

    suspend fun mergeUser(userUid: String, onResult: (Boolean, String) -> Unit)
}