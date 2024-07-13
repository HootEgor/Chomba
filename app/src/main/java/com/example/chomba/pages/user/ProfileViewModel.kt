package com.example.chomba.pages.user

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.chomba.GameUiState
import com.example.chomba.R
import com.example.chomba.data.Game
import com.example.chomba.data.Player
import com.example.chomba.data.getTotalScore
import com.example.chomba.repo.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    var profileUi = mutableStateOf(ProfileScreenUiState())
        private set

    private val auth = FirebaseAuth.getInstance()

    val userRepo = UserRepository(auth)

    init {
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

    fun loadGames(){
        viewModelScope.launch {
            startProgressProfile()
            profileUi.value = userRepo.loadGames(profileUi)
        }.invokeOnCompletion {
            profileUi.value = profileUi.value.copy(currentGameIndex = null)
            stopProgressProfile()
        }

    }

    fun signInWithGoogleToken(googleIdToken: String) {
        profileUi.value = userRepo.signInWithGoogleToken(googleIdToken, profileUi)
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

    fun saveGame(profileUi: MutableState<ProfileScreenUiState>,
                 playerList: MutableState<List<Player>>,
                 uiState: MutableState<GameUiState>
    ): Triple<ProfileScreenUiState, List<Player>, GameUiState>{
        return userRepo.saveGame(profileUi, playerList, uiState)
    }

    fun deleteGame(id: String){
        viewModelScope.launch {
            startProgressProfile()
            profileUi.value = userRepo.deleteGame(id, profileUi)
        }.invokeOnCompletion {
            loadGames()
        }
    }

    fun setCurrentGame(id: String){
        if(profileUi.value.currentGameIndex == id){
            profileUi.value = profileUi.value.copy(currentGameIndex = null)
        }else{
            val game = profileUi.value.gameList.find { it.id == id }!!
            if(!isGameFinished(game)){
                profileUi.value = profileUi.value.copy(currentGameIndex = id)
            }
        }

    }

    private fun isGameFinished(game: Game): Boolean {
        for (player in game.playerList){
            if (player.getTotalScore() == 1000){
                return true
            }
        }
        return false
    }
}