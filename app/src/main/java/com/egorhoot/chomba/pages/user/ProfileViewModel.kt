package com.egorhoot.chomba.pages.user

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.egorhoot.chomba.GameUiState
import com.egorhoot.chomba.R
import com.egorhoot.chomba.data.Game
import com.egorhoot.chomba.data.Language
import com.egorhoot.chomba.data.LeaderBoardPlayer
import com.egorhoot.chomba.data.Player
import com.egorhoot.chomba.data.getTotalScore
import com.egorhoot.chomba.data.isWinner
import com.egorhoot.chomba.pages.user.leaderboard.LeaderBoardViewModel
import com.egorhoot.chomba.repo.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    var profileUi = mutableStateOf(ProfileScreenUiState())
        private set

    val leaderBoardViewModel = LeaderBoardViewModel(application)

    private val auth = FirebaseAuth.getInstance()

    val userRepo = UserRepository(auth)

    init {
        if (auth.currentUser != null) {
            viewModelScope.launch {
                profileUi.value = profileUi.value.copy(isAuthenticated = true,
                    displayName = auth.currentUser?.displayName ?: "",
                    userPicture = auth.currentUser?.photoUrl ?: Uri.EMPTY )
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
        profileUi.value = userRepo.signInWithGoogleToken(googleIdToken, profileUi)
    }

    private fun signOut() {
        viewModelScope.launch {
            auth.signOut()
        }.invokeOnCompletion {
            profileUi.value = profileUi.value.copy(isAuthenticated = false,
                displayName = "",
                userPicture = Uri.EMPTY)
        }
    }

    fun onSignOut(){
        showAlert(R.string.sign_out, getApplication<Application>().getString(R.string.are_you_sure),
            {signOut()
            dismissAlert()},
            {dismissAlert()})
    }

    fun saveGame(profileUi: MutableState<ProfileScreenUiState>,
                 playerList: MutableState<List<Player>>,
                 uiState: MutableState<GameUiState>
    ): Triple<ProfileScreenUiState, List<Player>, GameUiState>{
        return userRepo.saveGame(profileUi, playerList, uiState)
    }

    fun onDeleteGame(id: String){
        val context = getApplication<Application>()
        showAlert(R.string.delete_game, context.getString(R.string.are_you_sure),
            {deleteGame(id)
            dismissAlert()},
            {dismissAlert()})
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

    fun SelectSpeechRecLanguage(lang: Language){
        profileUi.value = profileUi.value.copy(selectedLanguage = lang)
    }

    fun showAlert(title: Int, msg: String, action: () -> Unit, dismiss: () -> Unit){
        profileUi.value = profileUi.value.copy(showAlert = true,
            alertTitle = title,
            alertMsg = msg,
            alertAction = action,
            alertDismiss = dismiss)
    }

    fun dismissAlert(){
        profileUi.value = profileUi.value.copy(showAlert = false)
    }
}