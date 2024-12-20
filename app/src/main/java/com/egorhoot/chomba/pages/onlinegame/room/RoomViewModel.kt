package com.egorhoot.chomba.pages.onlinegame.room

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.egorhoot.chomba.ChombaViewModel
import com.egorhoot.chomba.R
import com.egorhoot.chomba.data.OnLineGame
import com.egorhoot.chomba.pages.PageState
import com.egorhoot.chomba.pages.onlinegame.OnLineGameUiState
import com.egorhoot.chomba.pages.user.ProfileScreenUiState
import com.egorhoot.chomba.repo.OnLineGameRepository
import com.egorhoot.chomba.repo.UserRepository
import com.egorhoot.chomba.utils.IdConverter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoomViewModel @Inject constructor(
    val userRepo: UserRepository,
    val onLineGameRepo: OnLineGameRepository,
    private val idConverter: IdConverter,
    val profileUi: MutableState<ProfileScreenUiState>,
    val pageState: MutableState<PageState>,
    val onLineGameUiState: MutableState<OnLineGameUiState>,
    private val context: Context,
) : ChombaViewModel(){

    val roomUiState = mutableStateOf(RoomUiState())

    val game: OnLineGame
        get() = onLineGameUiState.value.game

    val roomCode: String
        get() = onLineGameUiState.value.game.room.id

    init{
        browseRooms()
    }

    private fun startProgress(){
        profileUi.value = profileUi.value.copy(inProgress = true,
            alertMsg = idConverter.getString(profileUi.value.saveMsg))
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
            showAlert(profileUi, R.string.creating_room, idConverter.getString(profileUi.value.saveMsg),
                {
                    dismissAlert(profileUi)
                },
                {
                    dismissAlert(profileUi)
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
            showAlert(profileUi, R.string.getting_rooms, idConverter.getString(profileUi.value.saveMsg),
                {
                    dismissAlert(profileUi)
                },
                {
                    dismissAlert(profileUi)
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
            showAlert(profileUi, R.string.joining_room, idConverter.getString(R.string.in_progress),
                {
                    dismissAlert(profileUi)
                },
                {
                    dismissAlert(profileUi)
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
            showAlert(profileUi, R.string.leave_room, idConverter.getString(R.string.are_you_sure),
                action =
                {
                    onLeaveRoom()
                    dismissAlert(profileUi)
                },
                {
                    stopProgress()
                    dismissAlert(profileUi)
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
                    profileUi.value = profileUi.value.copy(alertMsg = idConverter.getString(R.string.room_left))
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
            Toast.makeText(context, idConverter.getString(R.string.room_copied_to_clipboard), Toast.LENGTH_SHORT).show()
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