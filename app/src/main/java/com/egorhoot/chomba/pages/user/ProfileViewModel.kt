package com.egorhoot.chomba.pages.user

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.egorhoot.chomba.ChombaViewModel
import com.egorhoot.chomba.GameUiState
import com.egorhoot.chomba.R
import com.egorhoot.chomba.data.Game
import com.egorhoot.chomba.data.Language
import com.egorhoot.chomba.data.getTotalScore
import com.egorhoot.chomba.pages.user.leaderboard.LeaderBoardViewModel
import com.egorhoot.chomba.repo.UserRepository
import com.egorhoot.chomba.utils.IdConverter
import dagger.hilt.android.internal.Contexts.getApplication
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@HiltViewModel
open class ProfileViewModel @Inject constructor(
    private val userRepo: UserRepository,
    private val idConverter: IdConverter,
    val leaderBoardViewModel: LeaderBoardViewModel,
    val profileUi: MutableState<ProfileScreenUiState>
): ChombaViewModel() {

    init {
        if (userRepo.auth.currentUser != null) {
            viewModelScope.launch {
                profileUi.value = profileUi.value.copy(isAuthenticated = true,
                    displayName = userRepo.auth.currentUser?.displayName ?: "",
                    userPicture = userRepo.auth.currentUser?.photoUrl ?: Uri.EMPTY )
                userRepo.loadVoiceRecLanguage(profileUi)
            }
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
            userRepo.loadGames(profileUi)
        }.invokeOnCompletion {
            profileUi.value = profileUi.value.copy(currentGameIndex = null)
            stopProgressProfile()
        }

    }

    fun signInWithGoogleToken(googleIdToken: String) {
        userRepo.signInWithGoogleToken(googleIdToken, profileUi)
    }

    private fun signOut() {
        viewModelScope.launch {
            userRepo.auth.signOut()
        }.invokeOnCompletion {
            profileUi.value = profileUi.value.copy(isAuthenticated = false,
                displayName = "",
                userPicture = Uri.EMPTY)
        }
    }

    fun onSignOut(){
        showAlert(profileUi,R.string.sign_out, idConverter.getString(R.string.are_you_sure),
            {signOut()
            dismissAlert(profileUi)},
            {dismissAlert(profileUi)})
    }

    fun onDeleteGame(id: String){
        showAlert(profileUi,R.string.delete_game, idConverter.getString(R.string.are_you_sure),
            {deleteGame(id)
            dismissAlert(profileUi)},
            {dismissAlert(profileUi)})
    }

    private fun deleteGame(id: String){
        viewModelScope.launch {
            startProgressProfile()
            userRepo.deleteGame(id, profileUi)
        }.invokeOnCompletion {
            loadGames()
        }
    }

    fun setCurrentGame(id: String){
        if(profileUi.value.currentGameIndex == id){
            profileUi.value = profileUi.value.copy(currentGameIndex = null)
        }else{
            val game = profileUi.value.gameList.find { it.id == id }!!
            profileUi.value = profileUi.value.copy(currentGameIndex = id)
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

    fun isCurrentGameFinished(): Boolean {
        if(profileUi.value.currentGameIndex != null){
            val game = profileUi.value.gameList.find { it.id == profileUi.value.currentGameIndex }!!
            return isGameFinished(game)
        }
        return false
    }

    fun toggleSettings(){
        if(profileUi.value.currentScreen == 1){
            profileUi.value = profileUi.value.copy(currentScreen = 0)
        }else{
            profileUi.value = profileUi.value.copy(currentScreen = 1)
        }
    }

    fun toggleLeaderBoard(){
        if(profileUi.value.currentScreen == 2){
            profileUi.value = profileUi.value.copy(currentScreen = 0)
        }else{
            profileUi.value = profileUi.value.copy(currentScreen = 2)
            leaderBoardViewModel.getLeaderBoardPlayers(profileUi.value.gameList)
        }
    }
}