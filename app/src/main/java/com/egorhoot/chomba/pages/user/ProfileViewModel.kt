package com.egorhoot.chomba.pages.user

import android.graphics.Bitmap // Android specific, ok for app module ViewModel
import android.net.Uri // Android specific
import android.util.Log // Android specific
import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.Color // Android specific Jetpack Compose
import androidx.lifecycle.viewModelScope
import com.egorhoot.chomba.ChombaViewModel
import com.egorhoot.chomba.data.Game
import com.egorhoot.chomba.data.getTotalScore // Assuming this is in shared Player/Game
import com.egorhoot.chomba.repo.UserRepository
import com.egorhoot.chomba.util.StringProvider // Import KMP StringProvider
import com.egorhoot.chomba.utils.Encryptor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.core.graphics.createBitmap // Android specific
import com.egorhoot.chomba.pages.user.camera.CameraManager

@HiltViewModel
open class ProfileViewModel @Inject constructor(
    private val userRepo: UserRepository,
    private val encryptor: Encryptor,
    private val cameraManager: CameraManager,
    val profileUi: MutableState<ProfileScreenUiState>,
    val stringProvider: StringProvider // StringProvider injected
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
        profileUi.value = profileUi.value.copy(
            inProgress = true,
            saveMsgKey = "in_progress"
        )
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
            profileUi.value = profileUi.value.copy(
                isAuthenticated = false,
                displayName = "",
                userPicture = Uri.EMPTY,
                gameList = emptyList(),
                relatedUserList = emptyList(),
                currentGameIndex = null,
                currentGame = null,
                nickname = ""
            )
        }
    }

    private fun dismissAlert() {
        profileUi.value = profileUi.value.copy(
            showAlert = false,
            alertTitleKey = "",
            alertMsgKey = "",
            alertMsgArgs = emptyList(),
            resolvedAlertTitle = "", // Clear resolved string
            resolvedAlertMessage = "" // Clear resolved string
        )
    }

    private fun triggerAlert(
        titleKey: String,
        messageKey: String,
        messageArgs: List<Any> = emptyList(),
        onConfirm: () -> Unit,
        onDismiss: () -> Unit = { dismissAlert() } // Default dismiss action
    ) {
        val resolvedTitle = if (titleKey.isNotBlank()) stringProvider.getString(titleKey) else ""
        val resolvedMessage = if (messageKey.isNotBlank()) stringProvider.getString(messageKey, *messageArgs.toTypedArray()) else ""

        profileUi.value = profileUi.value.copy(
            showAlert = true,
            alertTitleKey = titleKey,
            alertMsgKey = messageKey,
            alertMsgArgs = messageArgs,
            resolvedAlertTitle = resolvedTitle,
            resolvedAlertMessage = resolvedMessage,
            alertAction = onConfirm,
            alertDismiss = onDismiss
        )
    }

    fun onSignOut(){
        triggerAlert(
            titleKey = "sign_out",
            messageKey = "are_you_sure",
            onConfirm = {
                signOut()
                dismissAlert()
            }
        )
    }

    fun onDeleteGame(gameId: String){
         triggerAlert(
            titleKey = "delete_game",
            messageKey = "are_you_sure",
            onConfirm = {
                deleteGameInternal(gameId)
                dismissAlert()
            }
        )
    }

    private fun deleteGameInternal(id: String){
        viewModelScope.launch {
            startProgressProfile()
            userRepo.deleteGame(id, profileUi)
        }.invokeOnCompletion {
            loadGames()
        }
    }

    fun setCurrentGame(id: String){
        if(profileUi.value.currentGameIndex == id){
            profileUi.value = profileUi.value.copy(currentGameIndex = null, currentGame = null)
        }else{
            val game = profileUi.value.gameList.find { it.id == id }
            profileUi.value = profileUi.value.copy(
                currentGameIndex = game?.id,
                currentGame = game
            )
        }
    }

    fun setTitleKey(key: String){
        profileUi.value = profileUi.value.copy(titleKey = key)
    }

    private fun isGameFinished(game: Game): Boolean {
        return game.playerList.any { it.getTotalScore() == 1000 }
    }

    fun isCurrentGameFinished(): Boolean {
        val game = profileUi.value.currentGame ?: return false
        return isGameFinished(game)
    }

    fun toggleSettings(){
        profileUi.value = profileUi.value.copy(
            currentScreen = if(profileUi.value.currentScreen == 1) 0 else 1
        )
    }

    fun toggleLeaderBoard(){
        profileUi.value = profileUi.value.copy(
            currentScreen = if(profileUi.value.currentScreen == 2) 0 else 2
        )
    }

    fun toggleEditGame(){
        if(profileUi.value.currentScreen == 3){
            setTitleKey("game_list")
            profileUi.value = profileUi.value.copy(currentScreen = 0)
        } else {
            profileUi.value.currentGame?.id?.let { gameId ->
                setCurrentGame(gameId)
                setTitleKey("edit_game")
                profileUi.value = profileUi.value.copy(currentScreen = 3)
            } ?: Log.w("ProfileViewModel", "ToggleEditGame called with null currentGame")
        }
    }

    fun saveUserNickName(nickname: String){
        if (userRepo.auth.currentUser != null) {
            viewModelScope.launch {
                userRepo.updateUserNickname(nickname)
                val currentUid = userRepo.auth.currentUser?.uid
                val updatedRelatedUsers = profileUi.value.relatedUserList.map { user ->
                    if (user.id == currentUid) user.copy(nickname = nickname) else user
                }
                profileUi.value = profileUi.value.copy(
                    nickname = nickname,
                    relatedUserList = updatedRelatedUsers
                )
            }
        }
    }

    fun getUserQRCode(color: Color, backColor: Color): Bitmap {
        val uid = userRepo.auth.currentUser?.uid ?: return createBitmap(1, 1)
        return encryptor.getUserQRCode(uid, 256, color, backColor)
    }

    fun requestCamera() {
        if (cameraManager.cameraPermissionDenied()) {
            Log.d("ProfileViewModel", "Camera: camera permission denied flag is set")
            profileUi.value = profileUi.value.copy(
                cameraPermissionDenied = true,
                cameraPermissionGranted = false
            )
        } else if (cameraManager.cameraPermissionGranted()) {
            Log.d("ProfileViewModel", "Camera: camera permission granted flag is set")
            profileUi.value = profileUi.value.copy(
                cameraPermissionDenied = false,
                cameraPermissionGranted = true
            )
        } else {
             Log.d("ProfileViewModel", "Camera: permissions neither denied nor granted explicitly by CameraManager state.")
        }
    }

    fun onPermissionDenied() {
        profileUi.value = profileUi.value.copy(
            cameraPermissionDenied = true,
            cameraPermissionGranted = false,
            scanQrCode = false
        )
        cameraManager.onPermissionDenied()
    }

    fun startScanner() {
        if (profileUi.value.inProgress) return
        profileUi.value = profileUi.value.copy(scanQrCode = true)
        requestCamera()
    }

    fun stopScanner() {
        profileUi.value = profileUi.value.copy(scanQrCode = false)
    }

    fun onCameraError() {
        profileUi.value = profileUi.value.copy(scanQrCode = false) // Stop scanning on error
        triggerAlert(
            titleKey = "title_error",
            messageKey = "unvalid_qr_code",
            onConfirm = { dismissAlert() }
        )
    }

    fun onRecognizeId(userUid: String) {
        val decryptedUid = encryptor.decrypt(userUid)

        startProgressProfile()
        stopScanner()

        if (decryptedUid.length < 25) { // Anonymous user merge
            // Initial alert while merging
            triggerAlert(
                titleKey = "in_progress",
                messageKey = "merging",
                onConfirm = { dismissAlert() }, // Or make it non-dismissable initially
                onDismiss = { dismissAlert() }
            )

            viewModelScope.launch {
                userRepo.mergeUser(decryptedUid) { success, mergedName ->
                    val finalTitleKey: String
                    val finalMsgKey: String
                    val finalMsgArgs: List<Any>

                    if (success) {
                        finalTitleKey = "success"
                        finalMsgKey = "all_games_of_x_now_yours"
                        finalMsgArgs = listOf(mergedName.takeIf { it.isNotBlank() } ?: "User")
                    } else {
                        finalTitleKey = "title_error"
                        finalMsgKey = "merge_failed"
                        finalMsgArgs = emptyList()
                    }
                    // Update the alert with the result
                    triggerAlert(
                        titleKey = finalTitleKey,
                        messageKey = finalMsgKey,
                        messageArgs = finalMsgArgs,
                        onConfirm = { dismissAlert() }
                        // onDismiss can remain as default or be explicitly set
                    )
                    stopProgressProfile()
                }
            }
        } else { // Registered user
            triggerAlert(
                titleKey = "title_error",
                messageKey = "you_cant_merge_registered_user",
                onConfirm = { dismissAlert() }
            )
            stopProgressProfile()
        }
    }

    fun isUserOwner(): Boolean {
        return userRepo.auth.currentUser?.uid == profileUi.value.currentGame?.ownerId
    }
}
