package com.egorhoot.chomba.pages.onlinegame

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.lifecycle.viewModelScope
import com.egorhoot.chomba.ChombaViewModel
import com.egorhoot.chomba.R
import com.egorhoot.chomba.pages.PageState
import com.egorhoot.chomba.pages.user.ProfileScreenUiState
import com.egorhoot.chomba.repo.OnLineGameRepository
import com.egorhoot.chomba.repo.UserRepository
import com.egorhoot.chomba.utils.IdConverter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnLineGameViewModel @Inject constructor(
    val userRepo: UserRepository,
    val onLineGameRepo: OnLineGameRepository,
    private val idConverter: IdConverter,
    val profileUi: MutableState<ProfileScreenUiState>,
    val onLineGameUiState: MutableState<OnLineGameUiState>,
    val pageState: MutableState<PageState>,
    private val context: Context,
) : ChombaViewModel(){

    init {
        viewModelScope.launch {
            onLineGameRepo.subscribeOnUpdates(onLineGameUiState, profileUi
            ) {
            }
        }
    }

    private fun startProgress(){
        profileUi.value = profileUi.value.copy(inProgress = true,
            alertMsg = idConverter.getString(profileUi.value.saveMsg))
    }

    private fun stopProgress(){
        profileUi.value = profileUi.value.copy(inProgress = false)
    }

    fun homePage(){
        pageState.value = pageState.value.copy(currentPage = 0)
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
        val isOwner = isOwner()
        return onLineGameUiState.value.game.userList.all { it.ready && !isOwner }
    }
}