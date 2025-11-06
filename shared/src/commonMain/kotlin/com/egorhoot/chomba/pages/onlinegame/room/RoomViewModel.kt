package com.egorhoot.chomba.pages.onlinegame.room

import com.egorhoot.chomba.data.OnLineGame
import com.egorhoot.chomba.pages.user.ProfileScreenUiState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.egorhoot.chomba.pages.PageState
import com.egorhoot.chomba.pages.onlinegame.OnLineGameUiState
import com.egorhoot.chomba.repo.OnLineGameRepository
import com.egorhoot.chomba.repo.UserRepository
import com.egorhoot.chomba.util.StringProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class RoomViewModel(
    private val userRepo: UserRepository,
    private val onLineGameRepo: OnLineGameRepository,
    val profileUi: MutableState<ProfileScreenUiState>,
    val pageState: MutableState<PageState>,
    val onLineGameUiState: MutableState<OnLineGameUiState>,
    private val scope: CoroutineScope,
    private val stringProvider: StringProvider,
    private val platformClipboard: (String) -> Unit = {},
    private val platformToast: (String) -> Unit = {}
) {

    val roomUiState = mutableStateOf(RoomUiState())

    val game: OnLineGame
        get() = onLineGameUiState.value.game

    val roomCode: String
        get() = onLineGameUiState.value.game.room.id

    init {
        browseRooms()
    }

    private fun dismissAlert() {
        profileUi.value = profileUi.value.copy(
            showAlert = false,
            alertTitleKey = "",
            alertMsgKey = "",
            alertMsgArgs = emptyList(),
            resolvedAlertTitle = "",
            resolvedAlertMessage = ""
        )
    }

    private fun showAlert(
        titleKey: String,
        messageKey: String,
        messageArgs: List<Any> = emptyList(),
        onConfirm: () -> Unit,
        onDismiss: () -> Unit = { dismissAlert() }
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

    private fun startProgress() {
        profileUi.value = profileUi.value.copy(
            inProgress = true,
            alertMsgKey = profileUi.value.saveMsgKey
        )
    }

    private fun stopProgress() {
        profileUi.value = profileUi.value.copy(inProgress = false)
    }

    fun homePage() {
        roomUiState.value = roomUiState.value.copy(page = 0)
        pageState.value = pageState.value.copy(currentPage = 0)
    }

    fun setRoomPage(page: Int) {
        roomUiState.value = roomUiState.value.copy(page = page)
    }

    fun createRoom() {
        scope.launch {
            startProgress()
            showAlert("creating_room", profileUi.value.saveMsgKey, emptyList(), { dismissAlert() }, { dismissAlert() })

            onLineGameRepo.createRoom(onLineGameUiState, profileUi) {
                stopProgress()
                if (profileUi.value.isSuccess) {
                    onLineGameUiState.value = onLineGameUiState.value.copy(topBarText = roomCode)
                    setRoomPage(1)
                }
            }
        }
    }

    fun joinRoom() {
        val code = roomUiState.value.roomCode
        if (onLineGameUiState.value.rooms.contains(code)) {
            startJoinRoom(code)
            return
        }
        scope.launch {
            startProgress()
            showAlert("getting_rooms", "getting_rooms", emptyList(), { dismissAlert() }, { dismissAlert() })
            onLineGameRepo.getAvailableRooms(onLineGameUiState, profileUi) {
                stopProgress()
                if (profileUi.value.isSuccess) startJoinRoom(code)
            }
        }
    }

    private fun startJoinRoom(code: String) {
        scope.launch {
            startProgress()
            showAlert("joining_room", "in_progress", emptyList(), { dismissAlert() }, { dismissAlert() })
            onLineGameRepo.joinRoom(code, onLineGameUiState, profileUi) {
                stopProgress()
                if (profileUi.value.isSuccess) {
                    onLineGameUiState.value = onLineGameUiState.value.copy(topBarText = roomCode)
                    setRoomPage(1)
                }
            }
        }
    }

    fun leaveGame() {
        scope.launch {
            startProgress()
            showAlert("leave_room", "are_you_sure", emptyList(), {
                onLeaveRoom()
                dismissAlert()
            }, {
                stopProgress()
                dismissAlert()
            })
        }
    }

    private fun onLeaveRoom() {
        scope.launch {
            onLineGameRepo.exitRoom(onLineGameUiState, profileUi) {
                stopProgress()
                homePage()
                if (profileUi.value.isSuccess) {
                    profileUi.value = profileUi.value.copy(alertMsgKey = "room_left")
                }
            }
        }
    }

    fun browseRooms() {
        scope.launch {
            startProgress()
            onLineGameRepo.getAvailableRooms(onLineGameUiState, profileUi) { stopProgress() }
        }
    }

    fun onRoomCodeChanged(code: String) {
        roomUiState.value = roomUiState.value.copy(roomCode = code)
    }

    fun copyRoomCodeToClipboard() {
        platformClipboard(roomCode)
        platformToast(stringProvider.getString("room_copied_to_clipboard"))
    }

    fun readyToPlay() {
        scope.launch {
            startProgress()
            onLineGameRepo.readyToPlay(onLineGameUiState, profileUi) { stopProgress() }
        }
    }

    fun isOwner(): Boolean = onLineGameRepo.isOwner(onLineGameUiState)
    fun isNonOwnerReady(): Boolean = onLineGameRepo.isNonOwnerReady(onLineGameUiState)
    fun isAllReady(): Boolean = onLineGameUiState.value.game.userList.isNotEmpty() &&
            onLineGameUiState.value.game.userList.all { it.ready }
}
