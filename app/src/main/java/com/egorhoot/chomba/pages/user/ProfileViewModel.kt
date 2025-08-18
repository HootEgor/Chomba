package com.egorhoot.chomba.pages.user

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewModelScope
import com.egorhoot.chomba.ChombaViewModel
import com.egorhoot.chomba.R
import com.egorhoot.chomba.data.Game
import com.egorhoot.chomba.data.getTotalScore
import com.egorhoot.chomba.repo.UserRepository
import com.egorhoot.chomba.utils.Encryptor
import com.egorhoot.chomba.utils.IdConverter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.core.graphics.createBitmap
import com.egorhoot.chomba.pages.user.camera.CameraManager

@HiltViewModel
open class ProfileViewModel @Inject constructor(
    private val userRepo: UserRepository,
    val idConverter: IdConverter,
    private val encryptor: Encryptor,
    private val cameraManager: CameraManager,
    val profileUi: MutableState<ProfileScreenUiState>
): ChombaViewModel() {

    init {
        if (userRepo.auth.currentUser != null) {
            viewModelScope.launch {
                profileUi.value = profileUi.value.copy(isAuthenticated = true,
                    displayName = userRepo.auth.currentUser?.displayName ?: "",
                    userPicture = userRepo.auth.currentUser?.photoUrl ?: Uri.EMPTY )
                userRepo.loadVoiceRecLanguage(profileUi)
                userRepo.loadGames(profileUi)
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
            profileUi.value = profileUi.value.copy(currentGameIndex = id,
                currentGame = game)
        }

    }

    fun setTitle(title: Int){
        profileUi.value = profileUi.value.copy(title = title)
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
            val game = profileUi.value.gameList.find { it.id == profileUi.value.currentGameIndex }
            if(game == null) return false
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
        }
    }

    fun toggleEditGame(){
        if(profileUi.value.currentScreen == 3){
            setTitle(R.string.game_list)
            profileUi.value = profileUi.value.copy(currentScreen = 0)
        }else{
            setCurrentGame(profileUi.value.currentGame!!.id)
            setTitle(R.string.edit_game)
            profileUi.value = profileUi.value.copy(currentScreen = 3)
        }
    }

    fun saveUserNickName(nickname: String){
        if (userRepo.auth.currentUser != null) {
            viewModelScope.launch {
                userRepo.updateUserNickname(nickname)
                val updatedRelatedUsers = profileUi.value.relatedUserList.map { user ->
                    if (user.id == userRepo.auth.currentUser?.uid) user.copy(nickname = nickname) else user
                }
                profileUi.value = profileUi.value.copy(
                    nickname = nickname,
                    relatedUserList = updatedRelatedUsers)
            }
        }
    }

    fun getUserQRCode(color: Color, backColor: Color): Bitmap {
        val uid = userRepo.auth.currentUser?.uid ?: return createBitmap(1, 1)
        return encryptor.getUserQRCode(uid, 256, color, backColor)
    }

    fun requestCamera() {
        if (cameraManager.cameraPermissionDenied()) {
            Log.d("PRG", "Camera: camera permission denied")
            profileUi.value = profileUi.value.copy(
                cameraPermissionDenied = true,
                cameraPermissionGranted = false
            )
            return
        }else if(cameraManager.cameraPermissionGranted()){
            Log.d("PRG", "Camera: camera permission granted")
            profileUi.value = profileUi.value.copy(
                cameraPermissionDenied = false,
                cameraPermissionGranted = true
            )
            return
        }
    }

    fun onPermissionDenied() {
        profileUi.value = profileUi.value.copy(
            cameraPermissionDenied = true,
            cameraPermissionGranted = false,
            scanQrCode = false,
        )
        cameraManager.onPermissionDenied()
    }

    fun startScanner() {
        if (profileUi.value.inProgress) {
            return
        }
//        onAlert(0,R.string.title_warning,R.string.not_implemented)
        profileUi.value = profileUi.value.copy(scanQrCode = true)
        requestCamera()
    }

    fun stopScanner() {
        profileUi.value = profileUi.value.copy(scanQrCode = false)
    }

    fun onCameraError() {
        profileUi.value = profileUi.value.copy(scanQrCode = false)
        val qrMsg = idConverter.getString(R.string.unvalid_qr_code)
        showAlert(profileUi, R.string.title_error, qrMsg,
            {dismissAlert(profileUi)}, {dismissAlert(profileUi)})
    }

    fun onRecognizeId(userUid: String) {

        val decryptedUid = encryptor.decrypt(userUid)

        startProgressProfile()
        stopScanner()
        if (decryptedUid.length < 25){
            viewModelScope.launch {
                var title = R.string.in_progress
                var message = idConverter.getString(R.string.merging)

                showAlert(profileUi, title, message,
                    {dismissAlert(profileUi)},
                    {dismissAlert(profileUi)})

                userRepo.mergeUser(decryptedUid) {
                        success, mergedName ->

                    title = if (success) {
                        R.string.success
                    } else {
                        R.string.title_error
                    }

                    message = if (success) {
                        idConverter.getString(R.string.all_games_of) + " $mergedName " +
                                idConverter.getString(R.string.now_your)
                    } else {
                        idConverter.getString(R.string.merge_failed)
                    }

                    profileUi.value = profileUi.value.copy(
                        alertTitle = title,
                        alertMsg = message
                    )
                }
                stopProgressProfile()

            }
        }else{
            val title = R.string.title_error
            val message = idConverter.getString(R.string.you_cant_merge_registered_user)
            showAlert(profileUi, title, message,
                {dismissAlert(profileUi)}, {dismissAlert(profileUi)})
            stopProgressProfile()
        }

    }

    fun isUserOwner(): Boolean{
        return userRepo.auth.currentUser?.uid == profileUi.value.currentGame?.ownerId
    }
}