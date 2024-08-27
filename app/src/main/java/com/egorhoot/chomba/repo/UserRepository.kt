package com.egorhoot.chomba.repo

import androidx.compose.runtime.MutableState
import com.egorhoot.chomba.GameUiState
import com.egorhoot.chomba.data.Game
import com.egorhoot.chomba.data.Language
import com.egorhoot.chomba.data.Player
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
    fun loadGames(profileUi: MutableState<ProfileScreenUiState>)
    fun deleteGame(id: String, profileUi: MutableState<ProfileScreenUiState>)
    fun saveVoiceRecLanguage(language: Language)
    fun loadVoiceRecLanguage(profileUi: MutableState<ProfileScreenUiState>)
}