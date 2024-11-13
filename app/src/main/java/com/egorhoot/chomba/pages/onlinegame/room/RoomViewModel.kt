package com.egorhoot.chomba.pages.onlinegame.room

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.egorhoot.chomba.ChombaViewModel
import com.egorhoot.chomba.R
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
    val onLineGameUiState: MutableState<OnLineGameUiState>
) : ChombaViewModel(){

    val roomUiState = mutableStateOf(RoomUiState())

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

}