package com.egorhoot.chomba.pages.onlinegame.room

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.egorhoot.chomba.ChombaViewModel
import com.egorhoot.chomba.data.OnLineGame
import com.egorhoot.chomba.pages.PageState
import com.egorhoot.chomba.pages.onlinegame.OnLineGameUiState
import com.egorhoot.chomba.pages.user.ProfileScreenUiState
import com.egorhoot.chomba.repo.OnLineGameRepository
import com.egorhoot.chomba.repo.UserRepository
import com.egorhoot.chomba.util.StringProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoomViewModel @Inject constructor(
    val userRepo: UserRepository,
    val onLineGameRepo: OnLineGameRepository,
    val profileUi: MutableState<ProfileScreenUiState>,
    val pageState: MutableState<PageState>,
    val onLineGameUiState: MutableState<OnLineGameUiState>,
    private val context: Context,
    private val stringProvider: StringProvider,
) : ChombaViewModel(){

    val roomUiState = mutableStateOf(RoomUiState())

    val game: OnLineGame
        get() = onLineGameUiState.value.game

    val roomCode: String
        get() = onLineGameUiState.value.game.room.id

    init{
        browseRooms()
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

    private fun showAlert(
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

    private fun startProgress(){
        profileUi.value = profileUi.value.copy(inProgress = true,
            alertMsgKey = profileUi.value.saveMsgKey)
    }

    private fun stopProgress(){
        profileUi.value = profileUi.value.copy(inProgress = false)
    }

    fun homePage(){
        roomUiState.value = roomUiState.value.copy(page = 0)
        pageState.value = pageState.value.copy(currentPage = 0)
    }

    fun setRoomPage(page: Int){
        roomUiState.value = roomUiState.value.copy(page = page)
    }

    fun createRoom(){
        viewModelScope.launch {
            startProgress()
            showAlert( "creating_room", profileUi.value.saveMsgKey, emptyList(),
                {
                    dismissAlert()
                },
                {
                    dismissAlert()
                }
            )

            onLineGameRepo.createRoom(onLineGameUiState, profileUi
            ) {
                stopProgress()
                if(profileUi.value.isSuccess){
                    onLineGameUiState.value = onLineGameUiState.value.copy(topBarText = onLineGameUiState.value.game.room.id)
                    setRoomPage(1)
                }
            }
        }
    }

    fun joinRoom(){
        val code = roomUiState.value.roomCode
        if(onLineGameUiState.value.rooms.contains(code)){
            startJoinRoom(code)
            return
        }
        viewModelScope.launch {
            startProgress()
            showAlert( "getting_rooms", "getting_rooms", emptyList(),
                {
                    dismissAlert()
                },
                {
                    dismissAlert()
                }
            )
            onLineGameRepo.getAvailableRooms(onLineGameUiState, profileUi){
                stopProgress()
                if(profileUi.value.isSuccess) {
                    startJoinRoom(code)
                }
            }
        }
    }

    private fun startJoinRoom(code: String){
        viewModelScope.launch {
            startProgress()
            showAlert( "joining_room", "in_progress", emptyList(),
                {
                    dismissAlert()
                },
                {
                    dismissAlert()
                }
            )
            onLineGameRepo.joinRoom(code, onLineGameUiState, profileUi){
                stopProgress()
                if(profileUi.value.isSuccess){
                    onLineGameUiState.value = onLineGameUiState.value.copy(topBarText = onLineGameUiState.value.game.room.id)
                    setRoomPage(1)
                }
            }

        }
    }

    fun leaveGame(){
        viewModelScope.launch {
            startProgress()
            showAlert( "leave_room", "are_you_sure", emptyList(),
                {
                    onLeaveRoom()
                    dismissAlert()
                },
                {
                    stopProgress()
                    dismissAlert()
                }
            )
        }
    }

    private fun onLeaveRoom(){
        viewModelScope.launch {
            onLineGameRepo.exitRoom(onLineGameUiState, profileUi
            ) {
                stopProgress()
                homePage()
                if(profileUi.value.isSuccess){
                    profileUi.value = profileUi.value.copy(alertMsgKey = "room_left")
                }
            }
        }
    }

    fun browseRooms(){
        viewModelScope.launch {
            startProgress()
            onLineGameRepo.getAvailableRooms(onLineGameUiState, profileUi){
                stopProgress()
//                if(profileUi.value.isSuccess){
//                    setRoomPage(3)
//                }
            }
        }
    }

    fun onRoomCodeChanged(code: String){
        roomUiState.value = roomUiState.value.copy(roomCode = code)
    }

    fun copyRoomCodeToClipboard() {
        viewModelScope.launch {
            // Get the ClipboardManager system service
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

            // Create a ClipData with the room code
            val clip = ClipData.newPlainText("Room Code", onLineGameUiState.value.game.room.id)

            // Set the ClipData to the clipboard
            clipboard.setPrimaryClip(clip)

            // Optionally, you might want to show a confirmation (e.g., a Toast)
            Toast.makeText(context, stringProvider.getString("room_copied_to_clipboard"), Toast.LENGTH_SHORT).show()
        }
    }

    fun readyToPlay() {
        viewModelScope.launch {
            startProgress()
            onLineGameRepo.readyToPlay(onLineGameUiState, profileUi
            ) {
                stopProgress()
            }
        }
    }

    fun isOwner(): Boolean {
        return onLineGameRepo.isOwner(onLineGameUiState)
    }

    fun isNonOwnerReady(): Boolean {
        return onLineGameRepo.isNonOwnerReady(onLineGameUiState)
    }

    fun isAllReady(): Boolean {
        if (onLineGameUiState.value.game.userList.isEmpty()) {
            return false
        }

        return onLineGameUiState.value.game.userList.all { it.ready }
    }

}