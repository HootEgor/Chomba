package com.egorhoot.chomba.repo

import androidx.compose.runtime.MutableState
import com.egorhoot.chomba.pages.onlinegame.OnLineGameUiState
import com.egorhoot.chomba.pages.user.ProfileScreenUiState

interface OnLineGameRepository {

    fun isOwner(onLineGameUiState: MutableState<OnLineGameUiState>): Boolean
    suspend fun createRoom(onLineGameUiState: MutableState<OnLineGameUiState>,
                           profileUi: MutableState<ProfileScreenUiState>,
                           onResult: () -> Unit)
    suspend fun joinRoom(code: String,
                 onLineGameUiState: MutableState<OnLineGameUiState>,
                 profileUi: MutableState<ProfileScreenUiState>,
                         onResult: () -> Unit)
    suspend fun exitRoom(onLineGameUiState: MutableState<OnLineGameUiState>,
                         profileUi: MutableState<ProfileScreenUiState>,
                         onResult: () -> Unit)
    suspend fun getAvailableRooms(onLineGameUiState: MutableState<OnLineGameUiState>,
                          profileUi: MutableState<ProfileScreenUiState>,
                                  onResult: () -> Unit)
    suspend fun readyToPlay(onLineGameUiState: MutableState<OnLineGameUiState>,
                            profileUi: MutableState<ProfileScreenUiState>,
                            onResult: () -> Unit)
    suspend fun subscribeOnUpdates(onLineGameUiState: MutableState<OnLineGameUiState>,
                                   profileUi: MutableState<ProfileScreenUiState>,
                                   onResult: () -> Unit)
}